package com.ctrip.framework.apollo.spring.boot.extension;

import java.lang.annotation.Annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;

/**
 * 
 * {@link ConfigurationProperties} Automatic update listener implementation
 * 
 * @author wangbo(wangle_r@163.com)
 */
public class ApolloRefreshConfigurationConfigChangeListener implements ConfigChangeListener {
  private static Logger logger = LoggerFactory.getLogger(ApolloRefreshConfigurationConfigChangeListener.class);
  private Object bean;
  private String beanName;
  private ConfigurationProperties annotation;

  private ApplicationContext applicationContext;
  private Annotation[] annotations;
  private ApolloRefreshConfigurationProperties apolloRefreshConfigurationProperties;

  public ApolloRefreshConfigurationConfigChangeListener(Object bean, String beanName,
      ConfigurationProperties annotation, Annotation[] annotations, ApplicationContext applicationContext) {
    super();
    this.bean = bean;
    this.beanName = beanName;
    this.annotation = annotation;
    this.annotations = annotations;
    this.applicationContext = applicationContext;
  }

  @Override
  public void onChange(ConfigChangeEvent changeEvent) {
    if (this.apolloRefreshConfigurationProperties == null) {
      this.apolloRefreshConfigurationProperties = (ApolloRefreshConfigurationProperties) applicationContext
          .getBean(ApolloRefreshConfigurationProperties.class);
    }
    logger.info("Apollo config changeEvent namespace: {},  {} oldValue {}", changeEvent.getNamespace(), beanName, bean);
    this.apolloRefreshConfigurationProperties.binding(bean, beanName, annotation, annotations);
    logger.info("Apollo config changeEvent : {} newValue {}", beanName, bean);

  }
}
