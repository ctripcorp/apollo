package com.ctrip.framework.apollo.spring.util;

import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class BeanRegistrationUtil {

  public static boolean registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, String beanName,
                                                          Class<?> beanClass) {
    return registerBeanDefinitionIfNotExists(registry, beanName, beanClass, null,null);
  }

  public static boolean registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, String beanName,
                                                          Class<?> beanClass, Object[] constructorValues) {
    return registerBeanDefinitionIfNotExists(registry, beanName, beanClass, constructorValues,null);
  }

  public static boolean registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, String beanName,
                                                          Class<?> beanClass, Map<String, Object> extraPropertyValues) {
    return registerBeanDefinitionIfNotExists(registry, beanName, beanClass, null,extraPropertyValues);
  }

  public static boolean registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, String beanName,
                                                          Class<?> beanClass, Object[] constructorValues, Map<String, Object> extraPropertyValues) {
    if (registry.containsBeanDefinition(beanName)) {
      return false;
    }
    String[] candidates = registry.getBeanDefinitionNames();
    for (String candidate : candidates) {
      BeanDefinition beanDefinition = registry.getBeanDefinition(candidate);
      if (Objects.equals(beanDefinition.getBeanClassName(), beanClass.getName())) {
        return false;
      }
    }

    BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
    if(constructorValues!=null && constructorValues.length>0){
      for (Object item : constructorValues) {
        definitionBuilder.addConstructorArgValue(item);
      }
    }
    BeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
    if (extraPropertyValues != null) {
      for (Map.Entry<String, Object> entry : extraPropertyValues.entrySet()) {
        beanDefinition.getPropertyValues().add(entry.getKey(), entry.getValue());
      }
    }
    registry.registerBeanDefinition(beanName, beanDefinition);
    return true;
  }

}