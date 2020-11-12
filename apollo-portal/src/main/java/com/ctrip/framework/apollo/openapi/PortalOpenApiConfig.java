package com.ctrip.framework.apollo.openapi;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 界面开放API配置
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan(basePackageClasses = PortalOpenApiConfig.class)
public class PortalOpenApiConfig {

}
