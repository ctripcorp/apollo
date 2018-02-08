package com.ctrip.framework.apollo.spring.annotation;

import java.lang.annotation.*;

/**
 * Create by zhangzheng on 2018/2/8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface DisableAutoUpdate {
}
