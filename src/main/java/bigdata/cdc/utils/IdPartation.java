package bigdata.cdc.utils;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

/**
 * @author tao.liu
 * @desc Partition rules
 */
public class IdPartation implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        int partationSize = cluster.partitionsForTopic(topic).size();//获取分区数量

        long id = Long.valueOf((String) key) % partationSize;

        return (int) id;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }

}
