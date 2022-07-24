package top.trumandu.kafka.utils;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import top.trumandu.kafka.constant.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Truman.P.Du
 * @date 2020/01/02
 * @description
 */
public class KafkaPropertiesHelper {

    public static Map<String, Object> convertToProducerProperties(Map<String, Object> mapConfig) {
        Map<String, Object> props = convertParams(mapConfig);
        if (!props.containsKey(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)) {
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        }
        if (!props.containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)) {
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        }
        return props;
    }

    public static Map<String, Object> convertToConsumerProperties(Map<String, Object> mapConfig) {
        Map<String, Object> consumerProps = convertParams(mapConfig);
        if (!consumerProps.containsKey(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)) {
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        }
        if (!consumerProps.containsKey(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)) {
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        }
        consumerProps.remove(Constants.Consumer.HANDLER);
        return consumerProps;
    }

    private static Map<String, Object> convertParams(Map<String, Object> mapConfig) {
        Map<String, Object> props = new HashMap<>(mapConfig.size());
        for (Map.Entry<String, Object> entry : mapConfig.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String newKey = key.replace("_", ".").replace("-", ".").toLowerCase();
            props.put(newKey, value);
        }
        return props;
    }

    public static long getLongFromMap(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Integer) {
            return (Integer) obj;
        } else {
            return Long.parseLong((String) obj);
        }
    }


    public static boolean judgeEnableAutoCommitFromMap(Map<String, Object> map, String key) {
        Map<String, Object> newMap = convertParams(map);
        return getBooleanFromMap(newMap, key);
    }

    public static boolean getBooleanFromMap(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else {
            return Boolean.parseBoolean((String) obj);
        }
    }
}
