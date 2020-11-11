package com.ctrip.framework.apollo.portal.spi.configuration;

import com.ctrip.framework.apollo.common.condition.ConditionalOnMissingProfile;
import com.ctrip.framework.apollo.portal.spi.ctrip.CtripMQService;
import com.ctrip.framework.apollo.portal.spi.defaultimpl.DefaultMQService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 队列配置
 */
@Configuration
public class MQConfiguration {

  /**
   * Ctrip队列配置
   */
  @Configuration
  @Profile("ctrip")
  public static class CtripMQConfiguration {

    /**
     * Ctrip队列服务
     *
     * @return Ctrip队列服务Bean
     */
    @Bean
    public CtripMQService mqService() {
      return new CtripMQService();
    }
  }

  /**
   * 默认的队列配置, spring.profiles.active != ctrip
   */
  @Configuration
  @ConditionalOnMissingProfile({"ctrip"})
  public static class DefaultMQConfiguration {

    /**
     * 默认的队列服务
     *
     * @return 默认的队列服务Bean
     */
    @Bean
    public DefaultMQService mqService() {
      return new DefaultMQService();
    }
  }

}
