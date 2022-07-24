package top.trumandu.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Truman.P.Du
 * @date 2020/01/15
 * @description
 */
@Configuration
@EnableConfigurationProperties(KafkaConfigProperties.class)
@ComponentScan("top.trumandu.kafka.annotation")
@ConditionalOnProperty(prefix = "smart.framework.kafka",value = "enabled")
public class KafkaAnnotationConfiguration {
}
