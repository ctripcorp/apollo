package com.ctrip.framework.apollo.portal.spi.configuration;

import com.ctrip.framework.apollo.openapi.filter.ConsumerAuthenticationFilter;
import com.ctrip.framework.apollo.openapi.util.ConsumerAuditUtil;
import com.ctrip.framework.apollo.openapi.util.ConsumerAuthUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 认证过滤器配置
 */
@Configuration
public class AuthFilterConfiguration {

  /**
   * 开放api认证过滤器
   *
   * @param consumerAuthUtil  消费者认证工具类
   * @param consumerAuditUtil 消费者审计工具类
   * @return
   */
  @Bean
  public FilterRegistrationBean openApiAuthenticationFilter(ConsumerAuthUtil consumerAuthUtil,
      ConsumerAuditUtil consumerAuditUtil) {
    FilterRegistrationBean openApiFilter = new FilterRegistrationBean();

    openApiFilter.setFilter(new ConsumerAuthenticationFilter(consumerAuthUtil, consumerAuditUtil));
    // 匹配 /openapi/* 路径
    openApiFilter.addUrlPatterns("/openapi/*");
    return openApiFilter;
  }
}