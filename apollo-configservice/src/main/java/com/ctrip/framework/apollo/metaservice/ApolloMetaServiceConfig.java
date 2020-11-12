package com.ctrip.framework.apollo.metaservice;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

/**
 * 元服务配置
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan(basePackageClasses = ApolloMetaServiceConfig.class)
public class ApolloMetaServiceConfig {

  /**
   * 防火墙Bean
   *
   * @return
   */
  @Bean
  public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
    return new DefaultHttpFirewall();
  }
}
