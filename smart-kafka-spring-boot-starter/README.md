# 1.Overview
## Feature
1. 支持原生spring-kafka使用方式
2. 支持配置多个kafka集群实例
3. 支持配置化生成Kafka Producer,Consumer
# 2.Dependency Management
在我们项目中引入以下依赖，即可使用     
```xml
 <dependency>
    <groupId>top.trumandu</groupId>
	<artifactId>smart-kafka-spring-boot-starter</artifactId>
    <version>0.1.12-SNAPSHOT</version>
</dependency>
```
# 3. How To Use

## 快速使用
### Producer
```yaml
smart.framework:
  kafka:
    producers:
      e4:
        bootstrap_servers: 192.168.1.101:9092
        acks: all
        retries: 3
        max_request_size: 10485760
      e11:
        bootstrap_servers: 192.168.2.101:9092
        acks: all
        retries: 3
        max_request_size: 10485760
```
代码如下：
```
@KafkaSource(name="e4")
KafkaProducerTemplate<String,String> kafkaProducerTemplate;

public void run(String... args) throws Exception {
      kafkaProducerTemplate.send("demo","trumantest");
}
```
### Consumer
```yaml
smart.framework:
  kafka:
    consumers:
      e4:
        bootstrap_servers: 192.168.1.101:9092
        key_deserializer: org.apache.kafka.common.serialization.StringDeserializer
        value_deserializer: org.apache.kafka.common.serialization.StringDeserializer
        group_id: Test_Group
        enable_auto_commit: false
        auto_offset_reset: latest
        max_partition_fetch_bytes: 10485760
        fetch_min_bytes: 2147483647
        fetch_max_wait_ms: 5000
        session_timeout_ms: 30000
        heartbeat_interval_ms: 9000
        max_poll_interval_ms: 300000
        max_poll_records: 500
        handler:
          concurrency: 1
          topicList: demo
          poolTimeout: 1000
          syncCommits: true
```
代码如下：
```
@KafkaSource(name = "e4")
KafkaConsumerTemplate<String, String> kafkaConsumerTemplate;
kafkaConsumerTemplate.run(new MessageListener<String, String>() {
            public void onMessage(ConsumerRecord<String, String> data) {
                System.out.println(data.toString());
            }
});
```

如何手动提交offset

配置为`enable_auto_commit: false`
```
@KafkaSource(name = "e4")
KafkaConsumerTemplate<String, String> kafkaConsumerTemplate;
kafkaConsumerTemplate.run(new ConsumerAwareMessageListener<String, String>() {
            @Override
            public void onMessage(ConsumerRecord<String, String> data, Consumer<?, ?> consumer) {
                System.out.println(data.toString());
               consumer.commitSync();
            }
});
```

更多MessageListener接口详见：
```java
public interface MessageListener<K, V> { 

    void onMessage(ConsumerRecord<K, V> data);

}

public interface AcknowledgingMessageListener<K, V> { 

    void onMessage(ConsumerRecord<K, V> data, Acknowledgment acknowledgment);

}

public interface ConsumerAwareMessageListener<K, V> extends MessageListener<K, V> { 

    void onMessage(ConsumerRecord<K, V> data, Consumer<?, ?> consumer);

}

public interface AcknowledgingConsumerAwareMessageListener<K, V> extends MessageListener<K, V> { 

    void onMessage(ConsumerRecord<K, V> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer);

}

public interface BatchMessageListener<K, V> { 

    void onMessage(List<ConsumerRecord<K, V>> data);

}

public interface BatchAcknowledgingMessageListener<K, V> { 

    void onMessage(List<ConsumerRecord<K, V>> data, Acknowledgment acknowledgment);

}

public interface BatchConsumerAwareMessageListener<K, V> extends BatchMessageListener<K, V> { 

    void onMessage(List<ConsumerRecord<K, V>> data, Consumer<?, ?> consumer);

}

public interface BatchAcknowledgingConsumerAwareMessageListener<K, V> extends BatchMessageListener<K, V> { 

    void onMessage(List<ConsumerRecord<K, V>> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer);

}
```
## spring-kafka使用
### Producer
```yaml
spring.kafka:
  bootstrap-servers: 192.168.1.101:9092
```
代码如下：
```
@Autowired
private KafkaTemplate<String, String> template;

public void run(String... args) throws Exception {
        this.template.send("demo", "foo1");
        this.template.send("demo", "foo2");
        this.template.send("demo", "foo3");
}
```

### Consumer
```yaml
spring.kafka:
  bootstrap-servers: 192.168.1.101:9092
  consumer:
     group-id: foo
     auto-offset-reset: earliest
     topic: demo
```
代码如下：
```
@KafkaListener(topics = "${spring.kafka.consumer.topic}")
public void listen(ConsumerRecord<?, ?> cr) throws Exception {
    logger.info(cr.toString());
}
```

更多使用详见[spring-kafka文档](https://docs.spring.io/spring-kafka/docs/2.4.0.RELEASE/reference/html/#reference)

## Declaring Config

`smart.framework.kafka.consumers`与`smart.framework.kafka.producers`下面的配置，支持所有原生kafka配置，只是
将`.`更换为`_`或者`-`。

例如`bootstrap.servers`配置为`bootstrap_servers`或者`bootstrap-servers`