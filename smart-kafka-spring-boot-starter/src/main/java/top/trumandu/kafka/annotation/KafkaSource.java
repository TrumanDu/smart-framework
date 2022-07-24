package top.trumandu.kafka.annotation;

import java.lang.annotation.*;

/**
 * @author Truman.P.Du
 * @date 2020/01/03
 * @description
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KafkaSource {
    String name();
}
