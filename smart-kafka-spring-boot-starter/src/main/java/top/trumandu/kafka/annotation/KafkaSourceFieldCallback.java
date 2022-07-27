package top.trumandu.kafka.annotation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;
import top.trumandu.kafka.KafkaConfigProperties;
import top.trumandu.kafka.KafkaConsumerTemplate;
import top.trumandu.kafka.KafkaProducerTemplate;
import top.trumandu.kafka.constant.Constants;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author Truman.P.Du
 * @date 2020/01/03
 * @description
 */
public class KafkaSourceFieldCallback implements ReflectionUtils.FieldCallback {

    private final static Logger logger = LoggerFactory.getLogger(KafkaSourceFieldCallback.class);
    private final static int AUTOWIRE_MODE = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

    private static ConfigurableListableBeanFactory configurableListableBeanFactory;
    private final Object bean;
    private final KafkaConfigProperties kafkaConfigProperties;

    public KafkaSourceFieldCallback(final ConfigurableListableBeanFactory bf, final Object bean, final KafkaConfigProperties kafkaConfigProperties) {
        configurableListableBeanFactory = bf;
        this.bean = bean;
        this.kafkaConfigProperties = kafkaConfigProperties;
    }

    @Override
    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        if (!field.isAnnotationPresent(KafkaSource.class)) {
            return;
        }
        ReflectionUtils.makeAccessible(field);

        String annotationName = field.getDeclaredAnnotation(KafkaSource.class).name();
        final Class<?> generic = field.getType();
        String fieldClassName = generic.getName();
        Object beanInstance = getBeanInstance(annotationName, fieldClassName, generic);
        field.set(bean, beanInstance);
    }


    private Map<String, Object> getProducerProps() {
        return kafkaConfigProperties.getProducers();
    }

    private Map<String, Object> getConsumerProps() {
        return kafkaConfigProperties.getConsumers();
    }

    @SuppressWarnings("all")
    public final Object getBeanInstance(final String annotationName, final String fieldClassName, Class generic) {
        Object objInstance = null;
        String beanName = annotationName + Constants.BEAN_NAME_SPLIT + fieldClassName;
        if (!configurableListableBeanFactory.containsBean(beanName)) {
            logger.info("Creating new  bean named '{}'.", beanName);
            if (generic.getName().equals(KafkaProducerTemplate.class.getName())) {
                KafkaProducerTemplate kafkaProducerTemplate;
                try {
                    Map<String, Object> producerProps = (Map<String, Object>) getProducerProps().get(annotationName);
                    if (producerProps == null) {
                        throw new RuntimeException("KafkaSource name '" + annotationName + "' not exist in config file.");
                    }
                    kafkaProducerTemplate = new KafkaProducerTemplate<>(producerProps);
                } catch (final Exception e) {
                    logger.error("Bean named '{}' create error.", beanName, e);
                    throw new RuntimeException(e);
                }

                objInstance = configurableListableBeanFactory.initializeBean(kafkaProducerTemplate, beanName);

            }

            if (generic.getName().equals(KafkaConsumerTemplate.class.getName())) {
                KafkaConsumerTemplate kafkaConsumerTemplate;
                try {
                    Map<String, Object> consumerProps = (Map<String, Object>) getConsumerProps().get(annotationName);
                    if (consumerProps == null) {
                        throw new RuntimeException("KafkaSource name '" + annotationName + "' not exist in config file.");
                    }
                    kafkaConsumerTemplate = new KafkaConsumerTemplate<>(consumerProps);
                } catch (final Exception e) {
                    logger.error("Bean named '{}' create error.", beanName, e);
                    throw new RuntimeException(e);
                }

                objInstance = configurableListableBeanFactory.initializeBean(kafkaConsumerTemplate, beanName);
            }

            if(objInstance==null){
                throw new RuntimeException("@KafkaSource just allow in class 'KafkaConsumerTemplate.class' or 'KafkaProducerTemplate.class'.");
            }

            configurableListableBeanFactory.autowireBeanProperties(objInstance, AUTOWIRE_MODE, true);
            configurableListableBeanFactory.registerSingleton(beanName, objInstance);
            logger.info("Bean named '{}' created successfully.", beanName);
        } else {
            objInstance = configurableListableBeanFactory.getBean(beanName);
            logger.info("Bean named '{}' already exist used as current bean reference.", beanName);
        }
        return objInstance;
    }
}
