package com.ctrip.framework.apollo.spring.boot.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.ctrip.framework.apollo.spring.boot.BeanFactoryMetadata;

import org.springframework.boot.context.properties.ConfigurationBeanFactoryMetadata;

/**
 * 
 * @author wangbo(wangle_r@163.com)
 *
 */
public class ApolloSpringBeanFactoryMetadata implements BeanFactoryMetadata {
  private ConfigurationBeanFactoryMetadata beanFactoryMetadata;
  
  public ApolloSpringBeanFactoryMetadata(ConfigurationBeanFactoryMetadata beanFactoryMetadata) {
    super();
    this.beanFactoryMetadata = beanFactoryMetadata;
  }

  @Override
  public <A extends Annotation> A findFactoryAnnotation(String beanName, Class<A> type) {
    return beanFactoryMetadata.findFactoryAnnotation(beanName, type);
  }

  public ConfigurationBeanFactoryMetadata getBeanFactoryMetadata() {
    return beanFactoryMetadata;
  }

  public void setBeanFactoryMetadata(ConfigurationBeanFactoryMetadata beanFactoryMetadata) {
    this.beanFactoryMetadata = beanFactoryMetadata;
  }

  public Method findFactoryMethod(String beanName) {
    return beanFactoryMetadata.findFactoryMethod(beanName);
  }
  
  
}
