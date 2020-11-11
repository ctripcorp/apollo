package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.core.ConfigConsts;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * Apollo配置开关，使用Java配置时，使用此注解注册Apollo属性源.
 *
 * <p>配置示例:</p>
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableApolloConfig({"someNamespace","anotherNamespace"})
 * public class AppConfig {
 *
 * }
 * </pre>
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ApolloConfigRegistrar.class)
public @interface EnableApolloConfig {

  /**
   * Apollo名称空间将配置注入到Spring属性源中 Sources.
   */
  String[] value() default {ConfigConsts.NAMESPACE_APPLICATION};

  /**
   * Apollo配置的优先级顺序，默认为有序，最低的优先级，即Integer.MAX_值.
   * <p>如果在不同的apollo配置中存在同名属性，则顺序较小的apollo配置将获胜。</p>
   *
   * @return Apollo配置的优先级顺序
   */
  int order() default Ordered.LOWEST_PRECEDENCE;
}
