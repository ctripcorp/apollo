package com.ctrip.framework.apollo.spring.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ctrip.framework.apollo.spring.auto.SpringValue;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.base.Preconditions;

/**
 * Apollo Annotation Processor for Spring Application
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloAnnotationProcessor extends ApolloProcessor {

  private Logger logger = LoggerFactory.getLogger(ApolloAnnotationProcessor.class);


  @Override
  protected void processField(Object bean, Field field) {
    ApolloConfig annotation = AnnotationUtils.getAnnotation(field, ApolloConfig.class);
    if (annotation == null) {
      return;
    }

    Preconditions.checkArgument(Config.class.isAssignableFrom(field.getType()),
        "Invalid type: %s for field: %s, should be Config", field.getType(), field);

    String namespace = annotation.value();
    Config config = ConfigService.getConfig(namespace);

    ReflectionUtils.makeAccessible(field);
    ReflectionUtils.setField(field, bean, config);

  }
  @Override
  protected void processMethod(final Object bean, final Method method) {

      ApolloConfigChangeListener annotation = AnnotationUtils.findAnnotation(method, ApolloConfigChangeListener.class);
      if (annotation == null) {
        return;
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
