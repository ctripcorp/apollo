package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.base.Preconditions;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import com.google.common.collect.Sets;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Apollo Annotation Processor for Spring Application
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloAnnotationProcessor extends ApolloProcessor implements BeanFactoryAware {

  /**
   * resolve the expression.
   */
  private ConfigurableBeanFactory configurableBeanFactory;

  @Override
  protected void processField(Object bean, String beanName, Field field) {
    this.processApolloConfig(bean, field);
  }

  @Override
  protected void processMethod(final Object bean, String beanName, final Method method) {
    this.processApolloConfigChangeListener(bean, method);
  }

  private void processApolloConfig(Object bean, Field field) {
    ApolloConfig annotation = AnnotationUtils.getAnnotation(field, ApolloConfig.class);
    if (annotation == null) {
      return;
    }

    Preconditions.checkArgument(Config.class.isAssignableFrom(field.getType()),
        "Invalid type: %s for field: %s, should be Config", field.getType(), field);

    final String namespace = annotation.value();
    final String resolvedNamespace = this.configurableBeanFactory.resolveEmbeddedValue(namespace);
    Config config = ConfigService.getConfig(resolvedNamespace);

    ReflectionUtils.makeAccessible(field);
    ReflectionUtils.setField(field, bean, config);
  }

  private void processApolloConfigChangeListener(final Object bean, final Method method) {
    ApolloConfigChangeListener annotation = AnnotationUtils
        .findAnnotation(method, ApolloConfigChangeListener.class);
    if (annotation == null) {
      return;
    }
    Class<?>[] parameterTypes = method.getParameterTypes();
    Preconditions.checkArgument(parameterTypes.length == 1,
        "Invalid number of parameters: %s for method: %s, should be 1", parameterTypes.length,
        method);
    Preconditions.checkArgument(ConfigChangeEvent.class.isAssignableFrom(parameterTypes[0]),
        "Invalid parameter type: %s for method: %s, should be ConfigChangeEvent", parameterTypes[0],
        method);

    ReflectionUtils.makeAccessible(method);
    String[] namespaces = annotation.value();
    String[] annotatedInterestedKeys = annotation.interestedKeys();
    String[] annotatedInterestedKeyPrefixes = annotation.interestedKeyPrefixes();
    ConfigChangeListener configChangeListener = new ConfigChangeListener() {
      @Override
      public void onChange(ConfigChangeEvent changeEvent) {
        ReflectionUtils.invokeMethod(method, bean, changeEvent);
      }
    };

    Set<String> interestedKeys = annotatedInterestedKeys.length > 0 ? Sets.newHashSet(annotatedInterestedKeys) : null;
    Set<String> interestedKeyPrefixes = annotatedInterestedKeyPrefixes.length > 0 ? Sets.newHashSet(annotatedInterestedKeyPrefixes) : null;

    for (String namespace : namespaces) {
      Config config = ConfigService.getConfig(this.configurableBeanFactory.resolveEmbeddedValue(namespace));

      if (interestedKeys == null && interestedKeyPrefixes == null) {
        config.addChangeListener(configChangeListener);
      } else {
        config.addChangeListener(configChangeListener, interestedKeys, interestedKeyPrefixes);
      }
    }
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
  }
}
