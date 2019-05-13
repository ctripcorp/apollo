package com.ctrip.framework.apollo.spring.boot.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ApolloRefreshConfigurationPropertiesBinder;
import org.springframework.boot.context.properties.ConfigurationBeanFactoryMetadata;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.validation.annotation.Validated;

import com.ctrip.framework.apollo.spring.annotation.AutoRefresh;
import com.ctrip.framework.apollo.spring.annotation.RefreshEnabled;
import com.ctrip.framework.apollo.spring.boot.AbstractRefreshConfigurationProperties;
import com.ctrip.framework.apollo.spring.util.ApolloRefreshUtil;

/**
 * {@link ConfigurationProperties} The specific implementation of automatic
 * update update implementation
 * 
 * @author wangbo(wangle_r@163.com)
 */
public class ApolloRefreshConfigurationProperties extends AbstractRefreshConfigurationProperties
    implements ApplicationContextAware, InitializingBean, EnvironmentAware {
  /**
   * The bean name of the configuration properties validator.
   */
  public static final String  VALIDATOR_BEAN_NAME = "configurationPropertiesValidator";
  /**
   * The bean name that this post-processor is registered with.
   */
  public static final String  BEAN_NAME           = ApolloRefreshConfigurationProperties.class.getName();

  private ApolloSpringBeanFactoryMetadata beanFactoryMetadata;
  
  private ApplicationContext applicationContext;
  
  private ApolloRefreshConfigurationPropertiesBinder binder;
  
  private Environment environment;

  @Override
  public void refreshBinding(Object bean, String beanName) {
    Object target = bean;
    if(bean instanceof String) {
      target = this.applicationContext.getBean(beanName);
    }
    Class<?> clazz = target.getClass();
    ConfigurationProperties annotation = ApolloRefreshUtil.findConfigurationPropertiesAnnotation(clazz, beanName, this.beanFactoryMetadata);
    if (annotation != null) {
      binding(target, beanName, annotation, 
          ApolloRefreshUtil.findAutoRefreshAnnotation(clazz, beanName, this.beanFactoryMetadata),
          ApolloRefreshUtil.findRefreshEnabledAnnotation(clazz, beanName, this.beanFactoryMetadata),
          null);
    }
    
  }
  
  @Override
  public void refreshBinding(Object bean, String beanName, Properties properties) {
    Object target = bean;
    if(bean instanceof String) {
      target = this.applicationContext.getBean(beanName);
    }
    Class<?> clazz = target.getClass();
    ConfigurationProperties annotation = ApolloRefreshUtil.findConfigurationPropertiesAnnotation(clazz, beanName, this.beanFactoryMetadata);
    if (annotation != null) {
      binding(target, beanName, annotation, 
          ApolloRefreshUtil.findAutoRefreshAnnotation(clazz, beanName, this.beanFactoryMetadata),
          ApolloRefreshUtil.findRefreshEnabledAnnotation(clazz, beanName, this.beanFactoryMetadata),
          properties);
    }
    
  }

  private void binding(Object bean, String beanName, ConfigurationProperties annotation, AutoRefresh autoRefresh,
                      RefreshEnabled refreshEnabled, Properties properties) {
    if (annotation == null || properties.isEmpty()) {
      return;
    }

    Validated validated = getAnnotation(bean, beanName, Validated.class);
    
    List<Annotation> annotations = new LinkedList<Annotation>();
    annotations.add(annotation);
    if(refreshEnabled != null) {
      annotations.add(refreshEnabled);
    }
    if(autoRefresh != null) {
      annotations.add(autoRefresh);
    }
    if(validated != null) {
      annotations.add(validated);
    }
    binding(bean, beanName, properties, annotations.toArray(new Annotation[annotations.size()]));
  }
  
  void binding(Object bean, String beanName, ConfigurationProperties annotation, Annotation[] annotations) {
    ResolvableType type = getBeanType(bean, beanName);
    Bindable<?> target = Bindable.of(type).withExistingValue(bean).withAnnotations(annotations);
    
    this.binder.bind(target, this.deducePropertySources(null));
  }

  private  void binding(Object bean, String beanName, Properties properties, Annotation[] annotations) {
    ResolvableType type = getBeanType(bean, beanName);
    Bindable<?> target = Bindable.of(type).withExistingValue(bean).withAnnotations(annotations);
    
    this.binder.bind(target, this.deducePropertySources(properties));
  }

  private PropertySources deducePropertySources(Properties properties) {
    if(properties != null) {
      return ((ConfigurableEnvironment) getPropertiesEnvironment(properties)).getPropertySources();
    }
    if (this.environment instanceof ConfigurableEnvironment) {
      return ((ConfigurableEnvironment) this.environment).getPropertySources();
    }
    
    return new MutablePropertySources();
  }

  private StandardEnvironment getPropertiesEnvironment(Properties properties) {
    StandardEnvironment environment = new StandardEnvironment();
    environment.getPropertySources().addFirst(new PropertiesPropertySource("APOLLO_UPDATE_PROPERTIES", properties));
    return environment;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.beanFactoryMetadata = new ApolloSpringBeanFactoryMetadata(this.applicationContext
        .getBean(ConfigurationBeanFactoryMetadata.BEAN_NAME, ConfigurationBeanFactoryMetadata.class));
    this.binder = new ApolloRefreshConfigurationPropertiesBinder(applicationContext, VALIDATOR_BEAN_NAME);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;

  }
  @Override
  public ApplicationContext getApplicationContext(){
   return this.applicationContext;
  }

  private ResolvableType getBeanType(Object bean, String beanName) {
    Method factoryMethod = this.beanFactoryMetadata.findFactoryMethod(beanName);
    if (factoryMethod != null) {
      return ResolvableType.forMethodReturnType(factoryMethod);
    }
    return ResolvableType.forClass(bean.getClass());
  }

  private <A extends Annotation> A getAnnotation(Object bean, String beanName, Class<A> type) {
    A annotation = this.beanFactoryMetadata.findFactoryAnnotation(beanName, type);
    if (annotation == null) {
      annotation = AnnotationUtils.findAnnotation(bean.getClass(), type);
    }
    return annotation;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
    
  }
}
