package com.ctrip.framework.apollo.common.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Conditional;

/**
 * {@link Conditional} 只在指定的spring.profile.active处于active状态时匹配.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnProfileCondition.class)
public @interface ConditionalOnProfile {

  /**
   * spring.profile.active 应该为指定的值
   *
   * @return spring.profile.active的值
   */
  String[] value() default {};
}
