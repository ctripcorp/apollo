package com.ctrip.framework.apollo.spring.boot.extension;

import com.ctrip.framework.apollo.spring.boot.ApolloAutoConfiguration;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourcesProcessor;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @author wangbo(wangle_r@163.com)
 */
@Configuration
@ConditionalOnProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED)
@AutoConfigureAfter(ApolloAutoConfiguration.class)
public class ApolloRefreshExtensionAutoConfiguration {

  @Bean
  @ConditionalOnBean(ConfigPropertySourcesProcessor.class)
  public ApolloRefreshConfigurationPropertiesProcessorRegistry apolloRefreshConfigurationPropertiesProcessorRegistry() {
    return new ApolloRefreshConfigurationPropertiesProcessorRegistry();
  }
}
