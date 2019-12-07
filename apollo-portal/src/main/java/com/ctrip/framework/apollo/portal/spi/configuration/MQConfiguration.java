package com.ctrip.framework.apollo.portal.spi.configuration;

import com.ctrip.framework.apollo.common.condition.ConditionalOnMissingProfile;
import com.ctrip.framework.apollo.portal.spi.ctrip.CtripMQService;
import com.ctrip.framework.apollo.portal.spi.defaultimpl.DefaultMQService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class MQConfiguration {

  @Configuration
  @Profile("ctrip")
  public static class CtripMQConfiguration {

    @Bean
    public CtripMQService mqService() {
      return new CtripMQService();
    }
  }

  /**
   * spring.profiles.active != ctrip
   */
  @Configuration
  @ConditionalOnMissingProfile({"ctrip"})
  public static class DefaultMQConfiguration {

    @Bean
    public DefaultMQService mqService() {
      return new DefaultMQService();
    }
  }

}
