package com.ctrip.framework.apollo.spring.config;

import com.ctrip.framework.apollo.spring.annotation.SpringValueProcessor;
import com.ctrip.framework.apollo.spring.property.SpringValueDefinitionProcessor;
import com.ctrip.framework.apollo.spring.annotation.ApolloJsonValueProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.ctrip.framework.apollo.spring.annotation.ApolloAnnotationProcessor;
import com.ctrip.framework.apollo.spring.util.BeanRegistrationUtil;

/**
 * Apollo Property Sources processor for Spring XML Based Application
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigPropertySourcesProcessor extends PropertySourcesProcessor
    implements BeanDefinitionRegistryPostProcessor {

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, PropertySourcesPlaceholderConfigurer.class.getName(),
        PropertySourcesPlaceholderConfigurer.class);
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApolloAnnotationProcessor.class.getName(),
        ApolloAnnotationProcessor.class);
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, SpringValueProcessor.class.getName(), SpringValueProcessor.class);
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApolloJsonValueProcessor.class.getName(),
        ApolloJsonValueProcessor.class);

    processSpringValueDefinition(registry);
  }

  /**
   * For Spring 3.x versions, the BeanDefinitionRegistryPostProcessor would not be
   * instantiated if it is added in postProcessBeanDefinitionRegistry phase, so we have to manually
   * call the postProcessBeanDefinitionRegistry method of SpringValueDefinitionProcessor here...
   */
  private void processSpringValueDefinition(BeanDefinitionRegistry registry) {
    SpringValueDefinitionProcessor springValueDefinitionProcessor = new SpringValueDefinitionProcessor();

    springValueDefinitionProcessor.postProcessBeanDefinitionRegistry(registry);
  }
}
