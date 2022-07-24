package top.trumandu.kafka;


import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import top.trumandu.kafka.constant.Constants;
import top.trumandu.kafka.utils.KafkaPropertiesHelper;

import java.util.Map;

/**
 * @author Truman.P.Du
 * @date 2020/01/07
 */
public class KafkaConsumerTemplate<K, V> {
    private Map<String, Object> properties;
    private ContainerProperties containerProperties;
    private ConcurrentMessageListenerContainer<K, V> concurrentMessageListenerContainer;
    private ConsumerFactory<K, V> consumerFactory;

    public KafkaConsumerTemplate(Map<String, Object> properties) {
        this.properties = properties;
        containerProperties = buildDefaultContainerProperties();
    }

    public KafkaConsumerTemplate(Map<String, Object> properties, ContainerProperties containerProperties) {
        this.properties = properties;
        this.containerProperties = containerProperties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    private Map<String, Object> consumerConfigs() {
        return KafkaPropertiesHelper.convertToConsumerProperties(properties);
    }

    private ContainerProperties buildDefaultContainerProperties() {
        Object handler = properties.get(Constants.Consumer.HANDLER);
        if (handler == null) {
            throw new IllegalArgumentException("consumer config handle must not  null.");
        }
        Map<String, Object> handlerMap = (Map<String, Object>) handler;
        if (!handlerMap.containsKey(Constants.Consumer.TOPIC_LIST)) {
            throw new IllegalArgumentException("consumer config topicList must not  null.");
        }
        String topics = (String) handlerMap.get(Constants.Consumer.TOPIC_LIST);
        ContainerProperties containerProps = new ContainerProperties(topics.split(","));

        try {
            if (handlerMap.containsKey(Constants.Consumer.POOL_TIMEOUT)) {
                containerProps.setPollTimeout(KafkaPropertiesHelper.getLongFromMap(handlerMap, Constants.Consumer.POOL_TIMEOUT));
            }
            if (handlerMap.containsKey(Constants.Consumer.SYNC_COMMITS)) {
                containerProps.setSyncCommits(KafkaPropertiesHelper.getBooleanFromMap(handlerMap, Constants.Consumer.SYNC_COMMITS));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("consumer config error.", e);
        }
        //禁用spring-kafka管理offset
        if (!KafkaPropertiesHelper.judgeEnableAutoCommitFromMap(properties, Constants.Consumer.ENABLE_AUTO_COMMIT)) {
            containerProps.setAckMode(ContainerProperties.AckMode.MANUAL);
        }
        return containerProps;
    }


    private ConsumerFactory<K, V> buildDefaultConsumerFactory(Map<String, Object> consumerConfigs) {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs);
    }

    private ConcurrentMessageListenerContainer<K, V> createConcurrentContainer(ContainerProperties containerProps, ConsumerFactory<K, V> consumerFactory) {
        ConcurrentMessageListenerContainer<K, V> kvConcurrentMessageListenerContainer = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProps);
        kvConcurrentMessageListenerContainer.setConcurrency(getConcurrency());
        return kvConcurrentMessageListenerContainer;
    }

    private int getConcurrency() {
        int concurrency;
        if (!properties.containsKey(Constants.Consumer.HANDLER)) {
            concurrency = 1;
        } else {
            Map<String, Object> handlerMap = (Map<String, Object>) properties.get(Constants.Consumer.HANDLER);
            concurrency = (int) handlerMap.getOrDefault(Constants.Consumer.CONCURRENCY, 1);
        }
        return concurrency;
    }

    private void checkNullPoint(ConcurrentMessageListenerContainer<K, V> container) {
        if (container == null) {
            throw new IllegalArgumentException("consumer concurrentMessageListenerContainer is null.");
        }
    }

    private void startContainer(Object listener, ConsumerFactory<K, V> consumerFactory) {
        synchronized (this) {
            if (listener == null || containerProperties == null) {
                throw new IllegalArgumentException("consumer messageListener/containerProperties must not  null.");
            }

            if (concurrentMessageListenerContainer != null && concurrentMessageListenerContainer.isRunning()) {
                throw new RuntimeException("concurrentMessageListenerContainer is running.");
            }
            containerProperties.setMessageListener(listener);
            if (concurrentMessageListenerContainer == null) {
                this.consumerFactory = consumerFactory;
                concurrentMessageListenerContainer = createConcurrentContainer(containerProperties, consumerFactory);
            }
            concurrentMessageListenerContainer.start();
        }
    }


    public void start() {
        checkNullPoint(concurrentMessageListenerContainer);
        if (concurrentMessageListenerContainer.isRunning()) {
            throw new RuntimeException("concurrentMessageListenerContainer is  running.");
        }
        concurrentMessageListenerContainer.start();
    }

    public synchronized void restart() {
        checkNullPoint(concurrentMessageListenerContainer);
        concurrentMessageListenerContainer.stop();
        concurrentMessageListenerContainer.start();
    }

    public synchronized void stop() {
        checkNullPoint(concurrentMessageListenerContainer);
        if (!concurrentMessageListenerContainer.isRunning()) {
            throw new RuntimeException("concurrentMessageListenerContainer is not running.");
        }
        concurrentMessageListenerContainer.stop();
    }

    public synchronized void stop(Runnable callback) {
        checkNullPoint(concurrentMessageListenerContainer);
        if (!concurrentMessageListenerContainer.isRunning()) {
            throw new RuntimeException("concurrentMessageListenerContainer is not running.");
        }
        concurrentMessageListenerContainer.stop(callback);
    }

    public synchronized void pause() {
        checkNullPoint(concurrentMessageListenerContainer);
        if (concurrentMessageListenerContainer.isContainerPaused()) {
            throw new RuntimeException("concurrentMessageListenerContainer is  paused.");
        }
        concurrentMessageListenerContainer.pause();
    }

    public synchronized void resume() {
        checkNullPoint(concurrentMessageListenerContainer);
        if (!concurrentMessageListenerContainer.isContainerPaused()) {
            throw new RuntimeException("concurrentMessageListenerContainer status must be paused.");
        }
        concurrentMessageListenerContainer.resume();
    }

    public synchronized void reload(Map<String, Object> properties) {
        checkNullPoint(concurrentMessageListenerContainer);
        concurrentMessageListenerContainer.stop(() -> {
            Object messageListener = containerProperties.getMessageListener();
            this.properties = properties;
            containerProperties = buildDefaultContainerProperties();
            containerProperties.setMessageListener(messageListener);
            concurrentMessageListenerContainer = createConcurrentContainer(containerProperties, consumerFactory);
            concurrentMessageListenerContainer.start();
        });
    }

    /**
     * 启动消费服务，单条消息处理，支持多线程
     * 默认单线程，线程数由配置文件决定
     * 使用DefaultKafkaConsumerFactory
     *
     * @param messageListener 事件处理监听器
     */
    public void run(MessageListener<K, V> messageListener) {
        run(messageListener, buildDefaultConsumerFactory(consumerConfigs()));
    }

    public void run(MessageListener<K, V> messageListener, ConsumerFactory<K, V> consumerFactory) {
        startContainer(messageListener, consumerFactory);
    }

    /**
     * 启动消费服务，支持批量处理消息，支持多线程
     * 默认单线程，线程数由配置文件决定
     * 使用DefaultKafkaConsumerFactory
     *
     * @param batchMessageListener 事件处理监听器
     */
    public void run(BatchMessageListener<K, V> batchMessageListener) {
        run(batchMessageListener, buildDefaultConsumerFactory(consumerConfigs()));
    }

    /**
     * 启动消费服务，支持批量处理消息，支持多线程
     *
     * @param batchMessageListener 事件处理监听器
     * @param consumerFactory      自定义consumerFactory
     */
    public void run(BatchMessageListener<K, V> batchMessageListener, ConsumerFactory<K, V> consumerFactory) {
        startContainer(batchMessageListener, consumerFactory);
    }
}
