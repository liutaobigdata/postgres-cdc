package bigdata.cdc;

import bigdata.cdc.config.*;
import bigdata.cdc.constants.Constants;
import bigdata.cdc.server.CDCServer;
import bigdata.cdc.utils.Notify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * @author tao.liu
 * @date 2020/12
 */
public class Bootstrap {

    private static final ConfigDef                   /**/ TUNNEL_CONFIG  /**/ = new ConfigDef();

    public static void main(String[] args) {

        Map<String, String> cmdArgs = toMap(args);
        initTunnelConfig(cmdArgs);

        List<String> tables = getTables(TUNNEL_CONFIG.getTableFile());

        Properties config = config();

        String decoding = config.getProperty(Constants.DECODING, "");


        String slotName = config.getProperty(Constants.SLOT_NAME, "");

        String pgUrl = config.getProperty(Constants.PG_URL, "");
        String pgUser = config.getProperty(Constants.PG_USER, "");
        String pgPassword = config.getProperty(Constants.PG_PASSWORD, "");

        String dingTalkToken = config.getProperty(Constants.TINGTOKEN, "");

        String lsnFile = config.getProperty(Constants.LSNFILE, "").trim();
        String jksPath = null;
        if (config.containsKey(Constants.JKS_PATH)) {
            jksPath = config.getProperty(Constants.JKS_PATH, "").trim();
        }

        PgConfig pgConfig = new PgConfig().url(pgUrl).user(pgUser).password(pgPassword);
        Notify notify = new Notify(dingTalkToken);
        String kafkaHost = config.getProperty(Constants.KAFKA_HOST, "").trim();
        String topic = config.getProperty(Constants.KAFKA_TOPIC, "").trim();
        startSubscribe(pgConfig, tables, slotName, decoding, notify, lsnFile, kafkaHost, topic, jksPath);

        System.out.println("CDCServer Started at" + TUNNEL_CONFIG.getProcessId());


    }

    public static ConfigDef getTunnelConfig() {
        return TUNNEL_CONFIG;
    }

    /**
     * 初始化Tunnel 配置
     * <pre>
     *     -d domain
     *     -a app id
     *     -u use apollo
     *     -c config file
     *     -y use yukon
     * </pre>
     *
     * @param cfg 参数
     */
    private static void initTunnelConfig(Map<String, String> cfg) {
        TUNNEL_CONFIG.setProcessId(getPid());
        TUNNEL_CONFIG.setAppId(cfg.getOrDefault("-a", Constants.APP_ID));
        TUNNEL_CONFIG.setConfigFile(cfg.get("-c"));
        TUNNEL_CONFIG.setTableFile(cfg.get("-t"));
    }

    private static Map<String, String> toMap(String[] args) {
        Map<String, String> cfg = new LinkedHashMap<>();
        if (args == null || args.length == 0) {
            return cfg;
        }
        for (int i = 0; i < args.length; i += 2) {
            try {
                cfg.put(args[i], args[i + 1]);
            } catch (Exception e) {
                //
            }
        }
        return cfg;
    }

    private static int getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(name.split("@")[0]);
    }

    private static void startSubscribe(PgConfig pgConfig, List<String> tables, String slotName, String decoding, Notify notify, String lsnFile, String kafkaHost, String topic, String jksPath) {

        //解析配置文件中的subscribes
        CDCServer newServer = null;
        try {
            SubscribeConfig subscribeConfig = toTunnelConfig(pgConfig, slotName);
            subscribeConfig.setServerId(generateServerId("127.0.0.1", getPid(), slotName));
            newServer = new CDCServer(subscribeConfig, tables, decoding, notify, lsnFile, kafkaHost, topic, jksPath);
            newServer.start();
        } catch (Exception e) {
            if (newServer != null) {
                newServer.shutdown();
            }
        }

    }

    /**
     * @param pgConfig
     * @param slotName
     * @return
     * @desc The  Entrance  of Subscribe from pg WAL-Log
     */
    private static SubscribeConfig toTunnelConfig(PgConfig pgConfig, String slotName) {


        //获取Pg数据库的连接信息
        //JDBC 连接Pg
        JdbcConfig jdbcConfig = getJdbcConfig(slotName, pgConfig);

        SubscribeConfig subscribeConfig = new SubscribeConfig();
        subscribeConfig.setJdbcConfig(jdbcConfig);
        subscribeConfig.setServerId(generateServerId(pgConfig.getUrl(), 3433, jdbcConfig.getSlotName()));

        return subscribeConfig;
    }

    /**
     * generate CONFIG_NAME new serverId
     *
     * @param host host
     * @param port port
     * @param slot slot
     * @return serverId
     */
    private static String generateServerId(String host, int port, String slot) {
        return slot + "@" + host + ":" + port;
    }

    /**
     * @param slotName
     * @param pgConnConf
     * @return
     * @desc pg jdbc config
     */
    private static JdbcConfig getJdbcConfig(String slotName, PgConfig pgConnConf) {
        JdbcConfig jdbcConfig = new JdbcConfig();
        jdbcConfig.setSlotName(slotName);
        jdbcConfig.setUrl(pgConnConf.getUrl());
        jdbcConfig.setUsername(pgConnConf.getUser());
        jdbcConfig.setPassword(pgConnConf.getPassword());
        return jdbcConfig;
    }

    /**
     * @return
     * @desc Read the External  config file as Properties
     */
    private static Properties config() {
        Properties prop = new Properties();
        try {
            File file = new File(TUNNEL_CONFIG.getConfigFile());

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] kV = line.split("=");
                prop.put(kV[0].trim(), kV[1] != null ? kV[1].trim() : "");
            }
        } catch (Exception e) {
            System.out.println("读取配置文件异常" + e.getMessage());
        }
        return prop;
    }

    public static List<String> getTables(String tableFile) {
        List<String> list = new ArrayList<>();
        try {
            File file = new File(tableFile);
            InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file), "utf-8");// 考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                list.add(lineTxt.trim());
            }
            bufferedReader.close();
            read.close();
        } catch (Exception e) {
            System.out.println("读取tables异常[" + e.getMessage());
        }
        return list;
    }
}
