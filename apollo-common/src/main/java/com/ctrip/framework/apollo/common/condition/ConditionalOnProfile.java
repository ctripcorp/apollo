package com.ctrip.framework.apollo.common.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * {@link Conditional} that only matches when the specified profiles are active.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnProfileCondition.class)
public @interface ConditionalOnProfile {

  /**
   * The profiles that should be active
   * @return
   */
  String[] value() default {};
}
