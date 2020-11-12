package com.ctrip.framework.apollo.adminservice;

import com.ctrip.framework.apollo.adminservice.filter.AdminServiceAuthenticationFilter;
import com.ctrip.framework.apollo.biz.config.BizConfig;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 管理服务自动配置（注册自定义的系统服务权限过滤器）
 */
@Configuration
public class AdminServiceAutoConfiguration {

  private final BizConfig bizConfig;

  public AdminServiceAutoConfiguration(final BizConfig bizConfig) {
    this.bizConfig = bizConfig;
  }

  /**
   * 注册自定义过滤器
   *
   * @return 自定义的系统服务权限过滤器
   */
  @Bean
  public FilterRegistrationBean<AdminServiceAuthenticationFilter> adminServiceAuthenticationFilter() {
    FilterRegistrationBean<AdminServiceAuthenticationFilter> filterRegistrationBean = new FilterRegistrationBean<>();
    // 注册自定义过滤器
    filterRegistrationBean.setFilter(new AdminServiceAuthenticationFilter(bizConfig));
    // 过滤路径
    filterRegistrationBean.addUrlPatterns("/apps/*");
    filterRegistrationBean.addUrlPatterns("/appnamespaces/*");
    filterRegistrationBean.addUrlPatterns("/instances/*");
    filterRegistrationBean.addUrlPatterns("/items/*");
    filterRegistrationBean.addUrlPatterns("/namespaces/*");
    filterRegistrationBean.addUrlPatterns("/releases/*");

    return filterRegistrationBean;
  }
}
