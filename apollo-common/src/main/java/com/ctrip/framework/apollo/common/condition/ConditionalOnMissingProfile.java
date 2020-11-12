package com.ctrip.framework.apollo.common.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Conditional;

/**
 * 只在指定的spring.profile.active处于非active状态时才匹配的{@link Conditional}.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnProfileCondition.class)
public @interface ConditionalOnMissingProfile {

  /**
   * 应处于非活动状态的配置文件
   *
   * @return spring.profile.active的值
   */
  String[] value() default {};
}
