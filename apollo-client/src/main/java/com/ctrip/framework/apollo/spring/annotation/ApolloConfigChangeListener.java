package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.core.ConfigConsts;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to register Apollo ConfigChangeListener.
 *
 * <p>Usage example:</p>
 * <pre class="code">
 * //Listener on namespaces of "someNamespace" and "anotherNamespace"
 * &#064;ApolloConfigChangeListener({"someNamespace","anotherNamespace"})
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
   * Apollo namespace for the config, if not specified then default to application
   */
  String[] value() default {ConfigConsts.NAMESPACE_APPLICATION};
}
