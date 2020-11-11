package com.ctrip.framework.apollo.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用此注解从Apollo注入json属性，支持与Spring的@Value相同的格式
 *
 * <p>使用示例:</p>
 * <pre class="code">
 * // 为SomeObject类型注入json属性值。.
 * // 假设SomeObject有两个属性，someString和someInt，那么Apollo中可能的配置是someJsonPropertyKey={"someString":"someValue", "someInt":10}。
 * &#064;ApolloJsonValue("${someJsonPropertyKey:someDefaultValue}")
 * private SomeObject someObject;
 * </pre>
 *
 * @author zhangzheng on 2018/3/6
 * @see org.springframework.beans.factory.annotation.Value
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface ApolloJsonValue {

  /**
   * 当前值表达式：例如："${someJsonPropertyKey:someDefaultValue}".
   */
  String value();
}

