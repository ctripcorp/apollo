package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 用于Spring应用的Apollo注释处理器
 * <p>
 * 处理 @ApolloConfig 和 @ApolloConfigChangeListener 注解处理器的初始化
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloAnnotationProcessor extends ApolloProcessor {

  @Override
  protected void processField(Object bean, String beanName, Field field) {
    // 处理 @ApolloConfig 注解，创建( 获得 )对应的 Config 对象，设置到注解的 Field 中
    ApolloConfig annotation = AnnotationUtils.getAnnotation(field, ApolloConfig.class);
    if (annotation == null) {
      return;
    }

    Preconditions.checkArgument(Config.class.isAssignableFrom(field.getType()),
        "Invalid type: %s for field: %s, should be Config", field.getType(), field);
    // 创建 Config 对象
    String namespace = annotation.value();
    Config config = ConfigService.getConfig(namespace);

    // 设置 Config 对象，到对应的 Field
    ReflectionUtils.makeAccessible(field);
    ReflectionUtils.setField(field, bean, config);
  }

  @Override
  protected void processMethod(final Object bean, String beanName, final Method method) {
    //处理 @ApolloConfigChangeListener 注解，创建回调注解方法的 ConfigChangeListener 对象，并向指定 Namespace 们的 Config 对象们，注册该监听器。
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

    // 创建 ConfigChangeListener 监听器。该监听器会调用被注解的方法。cc
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

    // 感兴趣的key集合
    Set<String> interestedKeys = annotatedInterestedKeys.length > 0 ?
        Sets.newHashSet(annotatedInterestedKeys) : null;
    // 感兴趣的key集合前缀
    Set<String> interestedKeyPrefixes = annotatedInterestedKeyPrefixes.length > 0 ?
        Sets.newHashSet(annotatedInterestedKeyPrefixes) : null;

    // 向指定 Namespace 的 Config 对象们，注册该监听器
    for (String namespace : namespaces) {
      Config config = ConfigService.getConfig(namespace);

      if (interestedKeys == null && interestedKeyPrefixes == null) {
        config.addChangeListener(configChangeListener);
      } else {
        config.addChangeListener(configChangeListener, interestedKeys, interestedKeyPrefixes);
      }
    }
  }
}
