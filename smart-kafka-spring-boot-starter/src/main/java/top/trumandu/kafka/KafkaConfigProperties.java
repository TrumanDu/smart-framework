package top.trumandu.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author Truman.P.Du
 * @date 2019/12/28
 * @description
 */
@ConfigurationProperties(prefix = "smart.framework.kafka")
public class KafkaConfigProperties {

    private Map<String, Object> producers;
    private Map<String, Object> consumers;


    public Map<String, Object> getProducers() {
        return producers;
    }

    public void setProducers(Map<String, Object> producers) {
        this.producers = producers;
    }

    public Map<String, Object> getConsumers() {
        return consumers;
    }

    public void setConsumers(Map<String, Object> consumers) {
        this.consumers = consumers;
    }
}
