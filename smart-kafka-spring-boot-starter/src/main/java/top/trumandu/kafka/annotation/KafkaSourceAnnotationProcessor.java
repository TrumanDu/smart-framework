package top.trumandu.kafka.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import top.trumandu.kafka.KafkaConfigProperties;

/**
 * @author Truman.P.Du
 * @date 2020/01/03
 * @description
 */

@Component
public class KafkaSourceAnnotationProcessor implements BeanPostProcessor {

    private final KafkaConfigProperties kafkaConfigProperties;
    private final ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Autowired
    public KafkaSourceAnnotationProcessor(ConfigurableListableBeanFactory bf, KafkaConfigProperties properties) {
        configurableListableBeanFactory = bf;
        kafkaConfigProperties = properties;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        scanKafkaSourceAnnotation(bean);
        return bean;
    }
    @SuppressWarnings("NullableProblems")
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    protected void scanKafkaSourceAnnotation(Object bean) {
        Class<?> managedBeanClass = bean.getClass();
        ReflectionUtils.FieldCallback fcb = new KafkaSourceFieldCallback(configurableListableBeanFactory, bean, kafkaConfigProperties);
        ReflectionUtils.doWithFields(managedBeanClass, fcb);
    }
}
