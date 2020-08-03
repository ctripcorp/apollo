package com.ctrip.framework.apollo.common.controller;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.DispatcherType;

@Configuration
public class CharacterEncodingFilterConfiguration {

  @Bean
  public FilterRegistrationBean<CharacterEncodingFilter> encodingFilter() {
    FilterRegistrationBean<CharacterEncodingFilter> bean = new FilterRegistrationBean<CharacterEncodingFilter>();
    bean.setFilter(new CharacterEncodingFilter());
    bean.addInitParameter("encoding", "UTF-8");
    //FIXME: https://github.com/Netflix/eureka/issues/702
//    bean.addInitParameter("forceEncoding", "true");
    bean.setName("encodingFilter");
    bean.addUrlPatterns("/*");
    bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
    return bean;
  }
}
