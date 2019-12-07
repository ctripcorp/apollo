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

@Configuration
public class EmailConfiguration {

  /**
   * spring.profiles.active = ctrip
   */
  @Configuration
  @Profile("ctrip")
  public static class CtripEmailConfiguration {

    @Bean
    public EmailService ctripEmailService() {
      return new CtripEmailService();
    }

    @Bean
    public CtripEmailRequestBuilder emailRequestBuilder() {
      return new CtripEmailRequestBuilder();
    }
  }

  /**
   * spring.profiles.active != ctrip
   */
  @Configuration
  @ConditionalOnMissingProfile({"ctrip"})
  public static class DefaultEmailConfiguration {
    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    public EmailService defaultEmailService() {
      return new DefaultEmailService();
    }
  }



}

