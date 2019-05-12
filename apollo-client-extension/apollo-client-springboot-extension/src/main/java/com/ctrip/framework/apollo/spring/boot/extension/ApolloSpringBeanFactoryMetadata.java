package com.ctrip.framework.apollo.spring.boot.extension;

import java.lang.annotation.Annotation;

import com.ctrip.framework.apollo.spring.boot.BeanFactoryMetadata;

import org.springframework.boot.context.properties.ConfigurationBeanFactoryMetaData;

public class ApolloSpringBeanFactoryMetadata implements BeanFactoryMetadata {
  private ConfigurationBeanFactoryMetaData beanFactoryMetadata;
  
  public ApolloSpringBeanFactoryMetadata(ConfigurationBeanFactoryMetaData beanFactoryMetadata) {
    super();
    this.beanFactoryMetadata = beanFactoryMetadata;
  }

  @Override
  public <A extends Annotation> A findFactoryAnnotation(String beanName, Class<A> type) {
    return beanFactoryMetadata.findFactoryAnnotation(beanName, type);
  }

  public ConfigurationBeanFactoryMetaData getBeanFactoryMetadata() {
    return beanFactoryMetadata;
  }

  public void setBeanFactoryMetadata(ConfigurationBeanFactoryMetaData beanFactoryMetadata) {
    this.beanFactoryMetadata = beanFactoryMetadata;
  }

}
