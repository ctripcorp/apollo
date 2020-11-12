package com.ctrip.framework.apollo.spring.util;

import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * Bean注册工具类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class BeanRegistrationUtil {

  /**
   * 如果指定的beanName未注册，就注册
   *
   * @param registry  注册器
   * @param beanName  bean的名称
   * @param beanClass bean的class对象
   * @return 注册成功，true,其它，false
   */
  public static boolean registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry,
      String beanName, Class<?> beanClass) {
    return registerBeanDefinitionIfNotExists(registry, beanName, beanClass, null);
  }

  /**
   * 注册Bean定义（如果不存在）
   *
   * @param registry            注册器
   * @param beanName            bean的名称
   * @param beanClass           bean的class对象
   * @param extraPropertyValues 额外属性值
   * @return 注册成功，true,其它，false
   */
  public static boolean registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry,
      String beanName, Class<?> beanClass, Map<String, Object> extraPropertyValues) {

    // BeanDefinition 实例是否在注册表中（是否注册），已经注册直接返回false
    if (registry.containsBeanDefinition(beanName)) {
      return false;
    }

    // 取得注册表中所有 BeanDefinition 实例的 beanName（标识）
    String[] candidates = registry.getBeanDefinitionNames();
    for (String candidate : candidates) {
      // 从注册中取得指定的 BeanDefinition 实例
      BeanDefinition beanDefinition = registry.getBeanDefinition(candidate);
      // 如果bean名称一致，返回false
      if (Objects.equals(beanDefinition.getBeanClassName(), beanClass.getName())) {
        return false;
      }
    }

    // 生成beanDefinition对象
    BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(beanClass)
        .getBeanDefinition();

    if (extraPropertyValues != null) {
      for (Map.Entry<String, Object> entry : extraPropertyValues.entrySet()) {
        beanDefinition.getPropertyValues().add(entry.getKey(), entry.getValue());
      }
    }
    // 往注册表中注册一个新的 BeanDefinition 实例
    registry.registerBeanDefinition(beanName, beanDefinition);
    return true;
  }


}
