package com.ctrip.framework.apollo.demo.spring.config;

import com.ctrip.framework.apollo.demo.spring.bean.NormalBean;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Configuration
@EnableApolloConfig(value = "application", order = 10)
public class AppConfig {
  @Bean
  public NormalBean normalBean(@Value("${batch:100}") int batch) {
    NormalBean bean = new NormalBean();
    bean.setBatch(batch);
    return bean;
  }
}
