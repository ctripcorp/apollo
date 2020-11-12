package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.core.ConfigConsts;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用此注释注册Apollo配置变更监听器
 *
 * <p>使用示例:</p>
 * <pre class="code">
 * // “someNamespace”和“anotherNamespace”名称空间上的配置变更侦听器将在任何key更改时得到通知
 * &#064;ApolloConfigChangeListener({"someNamespace","anotherNamespace"})
 * private void onChange(ConfigChangeEvent changeEvent) {
 *     //handle change event
 * }
 * <br />
 * // “someNamespace”和“anotherNamespace”名称空间上的配置变更侦听器仅在“someKey”或“anotherKey”更改时才会收到通知
 * &#064;ApolloConfigChangeListener(value = {"someNamespace","anotherNamespace"}, interestedKeys = {"someKey", "anotherKey"})
 * private void onChange(ConfigChangeEvent changeEvent) {
 *     //handle change event
 * }
 * </pre>
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ApolloConfigChangeListener {

  /**
   * 配置的Apollo命名空间，如果未指定，则默认为application
   */
  String[] value() default {ConfigConsts.NAMESPACE_APPLICATION};

  /**
   * 配置变更监听器感兴趣的key只在任何感兴趣的键发生更改时才会得到通知。
   * <p>如果{@code interestedKeys}和{@code interestedKeyPrefixes}都没有指定，那么当任何键被更改时，将通知{@code listener}
   */
  String[] interestedKeys() default {};

  /**
   * 当且仅当更改的key以任何前缀开头时，配置变更监听器感兴趣的密钥前缀才会被通知。
   * <p>前缀将简单地用于确定是否应该通知{@code listener}使用{@code changedKey.startsWith(prefix)}。
   * <p>例如: “spring.”表示{@code listener}对以“spring”开头的key感兴趣，例如"spring.banner",
   * "spring.jpa,“application”表示 {@code listener}对以“application”开头的键感兴趣，例如“applicationName”
   * ,"application.port"，等等。
   * <p>如果没有指定{@code interestedKeys}和{@code interestedKeyPrefixes}，则当任何键被更改时，{@code listener}都会收到通知
   */
  String[] interestedKeyPrefixes() default {};
}
