package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.core.ConfigConsts;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用此注解注入Apollo配置实例.
 *
 * <p>使用示例:</p>
 * <pre class="code">
 * // 为“someNamespace”注入配置
 * &#064;ApolloConfig("someNamespace")
 * private Config config;
 * </pre>
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface ApolloConfig {

  /**
   * 配置的Apollo名称空间，如果未指定，则默认为应用程序
   */
  String value() default ConfigConsts.NAMESPACE_APPLICATION;
}
