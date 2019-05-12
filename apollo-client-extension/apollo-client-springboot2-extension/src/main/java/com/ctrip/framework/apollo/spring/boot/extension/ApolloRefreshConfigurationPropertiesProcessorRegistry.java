package com.ctrip.framework.apollo.spring.boot.extension;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.ConfigurationBeanFactoryMetadata;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * registry {@link ApolloRefreshConfigurationPropertiesProcessor},
 * {@link ApolloRefreshConfigurationProperties}
 * 
 * @author wangbo(wangle_r@163.com)
 */
public class ApolloRefreshConfigurationPropertiesProcessorRegistry
    implements PriorityOrdered, BeanDefinitionRegistryPostProcessor {

  private int order = Ordered.HIGHEST_PRECEDENCE + 1;

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    registry(registry, ConfigurationBeanFactoryMetadata.BEAN_NAME, ConfigurationBeanFactoryMetadata.class);
    registry(registry, ApolloRefreshConfigurationProperties.BEAN_NAME, ApolloRefreshConfigurationProperties.class);
    registry(registry, ApolloRefreshConfigurationPropertiesProcessor.BEAN_NAME,
        ApolloRefreshConfigurationPropertiesProcessor.class);
  }

  private void registry(BeanDefinitionRegistry registry, String beanName, Class<?> clas) {
    if (!registry.containsBeanDefinition(beanName)) {
      BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(clas);
      registry.registerBeanDefinition(beanName, bean.getBeanDefinition());
    }

  }

  @Override
  public int getOrder() {
    return order;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

  }

}
