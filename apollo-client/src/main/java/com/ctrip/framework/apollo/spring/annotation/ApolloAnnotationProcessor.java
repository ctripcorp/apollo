package com.ctrip.framework.apollo.spring.annotation;

import com.google.common.base.Preconditions;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Apollo Annotation Processor for Spring Application
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloAnnotationProcessor implements BeanPostProcessor {
  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    Class clazz = bean.getClass();
    processFields(bean, clazz.getDeclaredFields());
    processMethods(bean, clazz.getDeclaredMethods());
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  private void processFields(Object bean, Field[] declaredFields) {
    for (Field field : declaredFields) {
      ApolloConfig annotation = AnnotationUtils.getAnnotation(field, ApolloConfig.class);
      if (annotation == null) {
        continue;
      }

      Preconditions.checkArgument(Config.class.isAssignableFrom(field.getType()),
          "Invalid type: %s for field: %s, should be Config", field.getType(), field);

      String namespace = annotation.value();
      Config config = ConfigService.getConfig(namespace);

      ReflectionUtils.makeAccessible(field);
      ReflectionUtils.setField(field, bean, config);
    }
  }

  private void processMethods(final Object bean, Method[] declaredMethods) {
    for (final Method method : declaredMethods) {
      ApolloConfigChangeListener annotation = AnnotationUtils.findAnnotation(method, ApolloConfigChangeListener.class);
      if (annotation == null) {
        continue;
      }

      Class<?>[] parameterTypes = method.getParameterTypes();
      Preconditions.checkArgument(parameterTypes.length == 1,
          "Invalid number of parameters: %s for method: %s, should be 1", parameterTypes.length, method);
      Preconditions.checkArgument(ConfigChangeEvent.class.isAssignableFrom(parameterTypes[0]),
          "Invalid parameter type: %s for method: %s, should be ConfigChangeEvent", parameterTypes[0], method);

      ReflectionUtils.makeAccessible(method);
      String[] namespaces = annotation.value();
      for (String namespace : namespaces) {
        Config config = ConfigService.getConfig(namespace);

        config.addChangeListener(new ConfigChangeListener() {
          @Override
          public void onChange(ConfigChangeEvent changeEvent) {
            ReflectionUtils.invokeMethod(method, bean, changeEvent);
          }
        });
      }
    }
  }

}
