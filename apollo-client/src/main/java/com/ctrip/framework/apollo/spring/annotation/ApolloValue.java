package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.core.ConfigConsts;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.annotation.*;
import java.lang.reflect.Type;

/**
 * Create by zhangzheng on 2018/2/6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface ApolloValue {

    String value();

}
