package com.ctrip.framework.apollo.spring.boot;

import com.ctrip.framework.apollo.spring.config.DynamicDefaultConfigManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yangdihang(ivanyangtt@zju.edu.cn)
 **/
@Configuration
public class ApolloDynamicConfiguration {

  @Bean
  public DynamicDefaultConfigManager dynamicDefaultConfigManager() {
    return new DynamicDefaultConfigManager();
  }
}
