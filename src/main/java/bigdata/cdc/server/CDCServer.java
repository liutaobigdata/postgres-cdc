package bigdata.cdc.server;

import bigdata.cdc.config.JdbcConfig;
import bigdata.cdc.config.SubscribeConfig;
import bigdata.cdc.model.Event;
import bigdata.cdc.utils.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class CDCServer {

    private Logger log = LoggerFactory.getLogger(CDCServer.class);

    private final String                /**/ serverId;
    private final JdbcConfig            /**/ jdbcConfig;
    private final String                /**/ slotName;

    private final Thread                /**/ startThread;
    private final Thread                /**/ receiveThread;


    private volatile boolean            /**/ started = false;
    private Connection                  /**/ connection;
    private PGConnection                /**/ rplConnection;


    private PGReplicationStream         /**/ stream;
    private ParseEvent parseEvent;
    private String decoding;
    private Notify notify;
    private List<String> tables;
    private String lsnFile;
    private KafkaProducer producer;
    private String topic;
    private volatile String id;

    public CDCServer(SubscribeConfig config, List tables, String decoding, Notify notify, String lsnFile, String kafkaHost, String topic, String jksPath) {
        this.serverId = config.getServerId();
        this.jdbcConfig = config.getJdbcConfig();
        this.slotName = this.jdbcConfig.getSlotName();
        this.decoding = decoding;
        this.notify = notify;
        this.tables = tables;
        this.lsnFile = lsnFile;
        this.topic = topic;
        parseEvent = new ParseEvent();
        producer = new KafkaProducer(KafkaConfig.producerProperties(kafkaHost, jksPath));
        try {

            createRplConn();
        } catch (SQLException e) {

            this.notify.sendDingTalk("The environment【" + System.getProperty("environment") + "】 initialize PG error[" + e.getLocalizedMessage() + "]");
            log.error(String.format("create pg connection error: [%s]", ExceptionUtil.getStackLog(e)));
        }

        this.startThread = new Thread(new StartTask(), "TunnelStartThread-" + this.slotName);
        this.receiveThread = new Thread(new ReceiveTask(), "TunnelReceiveThread-" + this.slotName);
    }

    private static void closeClosable(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                //
            }
        }
    }

    public void start() {
        this.startThread.start();
    }

    public void shutdown() {
        started = false;
        closeClosable(this.stream);
        closeClosable(this.connection);
    }

    private void createRplConn() throws SQLException {
        String url = this.jdbcConfig.getUrl();
        Properties props = new Properties();
        PGProperty.USER.set(props, this.jdbcConfig.getUsername());
        PGProperty.PASSWORD.set(props, this.jdbcConfig.getPassword());
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, this.jdbcConfig.getMinVersion());
        PGProperty.REPLICATION.set(props, this.jdbcConfig.getRplLevel());
        PGProperty.PREFER_QUERY_MODE.set(props, "simple");
        PGProperty.CONNECT_TIMEOUT.set(props, 60 * 60);
        this.connection = DriverManager.getConnection(url, props);
        this.rplConnection = this.connection.unwrap(PGConnection.class);
        System.out.println("Create 4 RplConnection success,slot:" + this.slotName);
    }


    private void createRplSlot() throws SQLException {

        try {
            this.rplConnection.getReplicationAPI()
                    .createReplicationSlot()
                    .logical()
                    .withSlotName(this.jdbcConfig.getSlotName())
                    .withOutputPlugin(this.decoding)
                    .make();


        } catch (SQLException e) {
            String msg = "ERROR: replication slot \"" + this.jdbcConfig.getSlotName() + "\" already exists";
            if (msg.equals(e.getMessage())) {
                return;
            }
            throw e;
        }
    }

    private void createRplStream() throws SQLException {
        ChainedLogicalStreamBuilder builder = this.rplConnection.getReplicationAPI()
                .replicationStream()
                .logical()
                .withSlotName(this.jdbcConfig.getSlotName())
                .withSlotOption("skip-empty-xacts", true)
                .withStatusInterval(60, TimeUnit.MILLISECONDS);
        String lsn = FileOperator.readLsnFile(this.lsnFile);
        if (lsn == null || lsn.equals("0")) {
            this.notify.sendDingTalk("The environment【" + System.getProperty("environment") + "】  from the last lsn consume");

        } else {

            builder.withStartPosition(LogSequenceNumber.valueOf(lsn));
            this.notify.sendDingTalk("The environment【" + System.getProperty("environment") + "】 from the given  lsn consume [" + lsn + "]");
        }


        this.stream = builder.start();

        System.out.println("GetRplStream success,slot:" + this.slotName);
    }

    /**
     * @throws Exception
     * @desc 实时读取pg WAL日志
     */
    private void receiveStream() throws Exception {
        try {

            ByteBuffer msg = stream.readPending();

            if (msg == null) {
                return;
            }
            int offset = msg.arrayOffset();
            byte[] source = msg.array();
            int length = source.length - offset;
            LogSequenceNumber lsn = stream.getLastReceiveLSN();

            String message = new String(source, offset, length);
            //feedback
            stream.setAppliedLSN(lsn);
            stream.setFlushedLSN(lsn);

            if (message != null && !parseEvent.isBegin(message) && !parseEvent.isCommit(message)) {


                Event event = parseEvent.parseEvent(message);

                if (event != null && containsTable(event.getTable())) {
                    event.getDataList().parallelStream().forEach(item -> {
                        if (item.getName().equalsIgnoreCase("id") || item.getName().contains("old-key") || item.getName().contains("new-tuple")) {
                            id = item.getValue();
                            return;
                        }
                    });
                    ProducerRecord record = new ProducerRecord(this.topic, id, message);
                    producer.send(record);
                }

            }
        } catch (Exception e) {
            this.notify.sendDingTalk("producer端异常" + e.getLocalizedMessage());
            throw e;
        }


    }


    private void recover() {

        try {
            closeClosable(stream);
            closeClosable(connection);

            while (true) {
                try {
                    createRplConn();
                    createRplSlot();
                    createRplStream();
                    break;
                } catch (Exception e) {
                    System.out.println("Recover Streaming Occurred Error" + e.getLocalizedMessage());
                    closeClosable(stream);
                    closeClosable(connection);
                }
            }
        } finally {

        }
    }

    private class StartTask implements Runnable {
        private Logger log = LoggerFactory.getLogger(StartTask.class);

        @Override
        public void run() {

            try {

                createRplSlot();
                createRplStream();
                started = true;
                receiveThread.start();
                System.out.println("Startup RplStream Success");
            } catch (Exception e) {
                log.error(String.format("createSlot and create Stream Error:[%s]", ExceptionUtil.getStackLog(e)));
                shutdown();
            }
        }


    }

    private class ReceiveTask implements Runnable {
        private Logger log = LoggerFactory.getLogger(ReceiveTask.class);

        @Override
        public void run() {
            while (started) {
                try {
                    receiveStream();
                } catch (Exception e) {
                    log.error(String.format("receive msg failure[%s]", ExceptionUtil.getStackLog(e)));
                    recover();
                }
            }
        }
    }

    private boolean containsTable(String table) {

        if (this.tables.contains(table)) {
            return true;
        }
        return false;
    }
}
