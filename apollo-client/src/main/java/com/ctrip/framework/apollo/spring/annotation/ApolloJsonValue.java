package com.ctrip.framework.apollo.spring.annotation;

import java.lang.annotation.*;

/**
 * Use this annotation to inject json property from Apollo, support the same format as Spring @Value.
 *
 * <p>Usage example:</p>
 * <pre class="code">
 * // Inject the json property value for type SomeObject.
 * // Suppose SomeObject has 2 properties, someString and someInt, then the possible config
 * // in Apollo is someJsonPropertyKey={"someString":"someValue", "someInt":10}.
 * &#064;ApolloJsonValue("${someJsonPropertyKey:someDefaultValue}")
 * private SomeObject someObject;
 * </pre>
 *
 * Create by zhangzheng on 2018/3/6
 *
 * @see org.springframework.beans.factory.annotation.Value
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface ApolloJsonValue {

  /**
   * The actual value expression: e.g. "${someJsonPropertyKey:someDefaultValue}".
   */
  String value();
}
