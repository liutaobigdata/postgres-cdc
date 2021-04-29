package bigdata.cdc.utils;


import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

import java.util.Properties;

public class KafkaConfig {

    public static Properties producerProperties(String bootStrap, String sslTruststoreLocation) {

        Properties props = new Properties();
        //设置接入点，请通过控制台获取对应Topic的接入点
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrap);
        //Kafka消息的序列化方式。
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        //请求的最长等待时间。
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);
        //设置客户端内部重试次数。
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "com.burgeon.bigdata.server.utils.IdPartation");
        //设置客户端内部重试间隔。
        props.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 3000);
        if (System.getProperty("environment").equalsIgnoreCase("test")) {
            //设置SSL根证书的路径，请记得将XXX修改为自己的路径
            //与sasl路径类似，该文件也不能被打包到jar中
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocation);
            //根证书store的密码，保持不变
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "KafkaOnsClient");
            //接入协议，目前支持使用SASL_SSL协议接入
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            //SASL鉴权方式，保持不变
            props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");

            //hostname校验改成空
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        }


        return props;
    }

}
