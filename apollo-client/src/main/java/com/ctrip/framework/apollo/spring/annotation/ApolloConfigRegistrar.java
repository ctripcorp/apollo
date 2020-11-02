package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.spring.spi.ApolloConfigRegistrarHelper;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware {

  private final ApolloConfigRegistrarHelper helper = ServiceBootstrap.loadPrimary(ApolloConfigRegistrarHelper.class);

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    helper.registerBeanDefinitions(importingClassMetadata, registry);
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.helper.setBeanFactory(beanFactory);
  }

}
