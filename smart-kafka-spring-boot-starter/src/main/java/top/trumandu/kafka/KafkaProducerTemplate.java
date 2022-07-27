package top.trumandu.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import top.trumandu.kafka.utils.KafkaPropertiesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Truman.P.Du
 * @date 2022/07/26
 * @description
 */

public class KafkaProducerTemplate<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerTemplate.class);

    private final Map<String, Object> properties;
    private final KafkaTemplate<K, V> kafkaTemplate;

    private Map<String, Object> producerConfigs(Map<String, Object> properties) {
        return KafkaPropertiesHelper.convertToProducerProperties(properties);
    }

    private ProducerFactory<K, V> producerFactory(Map<String, Object> properties) {
        return new DefaultKafkaProducerFactory<>(producerConfigs(properties));
    }

    private KafkaTemplate<K, V> kafkaTemplate(Map<String, Object> producerProperties) {
        return new KafkaTemplate<>(producerFactory(producerProperties));
    }
    @SuppressWarnings("unused")
    public Map<String, Object> getProperties() {
        return properties;
    }
    @SuppressWarnings("unused")
    public KafkaTemplate<K, V> getKafkaTemplate() {
        return kafkaTemplate;
    }

    public KafkaProducerTemplate(Map<String, Object> kafkaConfigProperties) {
        this.properties = kafkaConfigProperties;
        if (properties.size() <= 0) {
            throw new RuntimeException("kafka producer config is null");
        }
        kafkaTemplate = kafkaTemplate(properties);
    }
    @SuppressWarnings("unused")
    public ListenableFuture<SendResult<K, V>> send(String topic, V data) {
        return kafkaTemplate.send(topic, data);
    }

    @SuppressWarnings("unused")
    public ListenableFuture<SendResult<K, V>> send(String topic, K key, V data) {
        return kafkaTemplate.send(topic, key, data);
    }

    @SuppressWarnings("unused")
    public ListenableFuture<SendResult<K, V>> send(String topic, Integer partition, K key, V data) {
        return kafkaTemplate.send(topic, partition, key, data);
    }

    @SuppressWarnings("unused")
    public ListenableFuture<SendResult<K, V>> send(String topic, Integer partition, Long timestamp, K key, V data) {
        return kafkaTemplate.send(topic, partition, timestamp, key, data);
    }


    public ListenableFuture<SendResult<K, V>> send(ProducerRecord<K, V> record) {
        return kafkaTemplate.send(record);
    }

    @SuppressWarnings({"unused","rawtypes","unchecked"})
    public void send(ProducerRecord<K, V> record, ListenableFutureCallback callback) {
        ListenableFuture<SendResult<K, V>> future = kafkaTemplate.send(record);
        future.addCallback(callback);
    }
    @SuppressWarnings("unused")
    public List<PartitionInfo> partitionsFor(String topic) {
        return kafkaTemplate.partitionsFor(topic);
    }
    @SuppressWarnings("unused")
    public ListenableFuture<SendResult<K, V>> send(Message<?> message) {
        return kafkaTemplate.send(message);
    }

    @SuppressWarnings("unused")
    public boolean sendSync(ProducerRecord<K, V> record) {
        ListenableFuture<SendResult<K, V>> future = send(record);
        try {
            future.get();
            return true;
        } catch (Exception e) {
            LOGGER.error("", e);
            return false;
        }
    }
    @SuppressWarnings("unused")
    public boolean sendSync(List<ProducerRecord<K, V>> records) {
        List<ListenableFuture<SendResult<K, V>>> futures = new ArrayList<>();
        records.forEach(record -> futures.add(kafkaTemplate.send(record)));
        for (ListenableFuture<SendResult<K, V>> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                LOGGER.error("", e);
                return false;
            }
        }
        return true;
    }
    @SuppressWarnings("unused")
    public List<Boolean> sendSyncBatch(List<ProducerRecord<K, V>> records) {
        List<ListenableFuture<SendResult<K, V>>> futures = new ArrayList<>();
        records.forEach(record -> futures.add(kafkaTemplate.send(record)));
        List<Boolean> results = new ArrayList<>(futures.size());
        for (ListenableFuture<SendResult<K, V>> future : futures) {
            try {
                future.get();
                results.add(true);
            } catch (Exception e) {
                LOGGER.error("", e);
                results.add(false);
            }
        }
        return results;
    }
    @SuppressWarnings("unused")
    public void flush() {
        kafkaTemplate.flush();
    }


}
