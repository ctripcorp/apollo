package com.ctrip.framework.apollo.common.controller;

import javax.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * 字符编码过滤器配置
 */
@Configuration
public class CharacterEncodingFilterConfiguration {

  @Bean
  public FilterRegistrationBean encodingFilter() {
    FilterRegistrationBean bean = new FilterRegistrationBean();
    // 添加字符过滤器,
    bean.setFilter(new CharacterEncodingFilter());
    //  设置初始化参数,对filter传递参数，filter可以通filterConfig来获取
    bean.addInitParameter("encoding", "UTF-8");
    // FIXME: https://github.com/Netflix/eureka/issues/702
    //  bean.addInitParameter("forceEncoding", "true");
    // 过滤器的名称
    bean.setName("encodingFilter");
    //设置需过滤的路径
    bean.addUrlPatterns("/*");
    // 设置分发的类型
    bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
    return bean;
  }
}
