package com.ctrip.framework.apollo.common;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Apollo 组件配置
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan(basePackageClasses = ApolloCommonConfig.class)
public class ApolloCommonConfig {

}
