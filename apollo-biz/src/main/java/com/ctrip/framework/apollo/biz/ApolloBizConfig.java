package com.ctrip.framework.apollo.biz;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * apollo-业务配置
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan(basePackageClasses = ApolloBizConfig.class)
public class ApolloBizConfig {

}
