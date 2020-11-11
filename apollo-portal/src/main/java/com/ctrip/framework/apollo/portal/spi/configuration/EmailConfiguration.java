package com.ctrip.framework.apollo.portal.spi.configuration;


import com.ctrip.framework.apollo.common.condition.ConditionalOnMissingProfile;
import com.ctrip.framework.apollo.portal.spi.EmailService;
import com.ctrip.framework.apollo.portal.spi.ctrip.CtripEmailRequestBuilder;
import com.ctrip.framework.apollo.portal.spi.ctrip.CtripEmailService;
import com.ctrip.framework.apollo.portal.spi.defaultimpl.DefaultEmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 邮箱配置
 */
@Configuration
public class EmailConfiguration {

  /**
   * Ctrip邮箱配置，spring.profiles.active = ctrip
   */
  @Configuration
  @Profile("ctrip")
  public static class CtripEmailConfiguration {

    /**
     * 注册邮箱服务beann
     *
     * @return CtripEmailService
     */
    @Bean
    public EmailService ctripEmailService() {
      return new CtripEmailService();
    }

    /**
     * 注册邮箱请求构建器bean
     *
     * @return CtripEmailRequestBuilder
     */
    @Bean
    public CtripEmailRequestBuilder emailRequestBuilder() {
      return new CtripEmailRequestBuilder();
    }
  }

  /**
   * 默认的邮箱配置， spring.profiles.active != ctrip
   */
  @Configuration
  @ConditionalOnMissingProfile({"ctrip"})
  public static class DefaultEmailConfiguration {

    /**
     * 注册默认的邮箱服务bean
     *
     * @return DefaultEmailService
     */
    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    public EmailService defaultEmailService() {
      return new DefaultEmailService();
    }
  }


}

