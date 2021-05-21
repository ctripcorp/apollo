package com.ctrip.framework.test.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.runner.Runner;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/5/21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface AloneWith {

  Class<? extends Runner> value();
}