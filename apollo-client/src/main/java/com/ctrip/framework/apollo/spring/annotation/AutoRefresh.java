package com.ctrip.framework.apollo.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Used to enable or disable automatic update of field values in spring and springboot.<br/><br />
 * 
 * Want the annotation play a role, "Apollo. AutoUpdateInjectedSpringProperties = true" must be set.<br/> <br />
 * 
 * Support springboot &#064;configurationproperties annotation of the class.<br/><br />
 * 
 * Both &#064;Autorefresh and &#064;Refreshfield exist. &#064;Refreshfield has a higher priority <br/><br />
 * 
 * Default enabled automatic update 
 * 
 * @author wangbo(wangle_r@163.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Inherited
public @interface AutoRefresh {
  public static final boolean enabled = true;
  public static final boolean disabled = false;
  /**
   * enabled / disabled auto refresh, default enabled automatic update 
   */
  boolean value() default enabled;

  /**
   * Apollo namespace for the config, The class marked &#064;configurationproperties in springboot is used above 
   */
  String[] namespaces() default  {};

  /**
   * 
   * The keys interested by the listener, will only be notified if any of the interested keys is changed.
   * <br />
   * If neither of {@code interestedKeys} and {@code interestedKeyPrefixes} is specified then the {@code listener} will be notified when any key is changed.
   * <br />
   * , The class marked &#064;configurationproperties in springboot is used above 
   */
  String[] interestedKeys() default {};

  /**
   * The key prefixes that the listener is interested in, will be notified if and only if the changed keys start with anyone of the prefixes.
   * The prefixes will simply be used to determine whether the {@code listener} should be notified or not using {@code changedKey.startsWith(prefix)}.
   * e.g. "spring." means that {@code listener} is interested in keys that starts with "spring.", such as "spring.banner", "spring.jpa", etc.
   * and "application" means that {@code listener} is interested in keys that starts with "application", such as "applicationName", "application.port", etc.
   * <br />
   * If neither of {@code interestedKeys} and {@code interestedKeyPrefixes} is specified then the {@code listener} will be notified when whatever key is changed.
   * <br />
   * The class marked &#064;configurationproperties in springboot is used above 
   */
  String[] interestedKeyPrefixes() default {};
}
