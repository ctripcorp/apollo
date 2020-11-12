package com.ctrip.framework.apollo.spring.spi;

import com.ctrip.framework.apollo.core.spi.Ordered;
import com.ctrip.framework.apollo.spring.annotation.ApolloAnnotationProcessor;
import com.ctrip.framework.apollo.spring.annotation.ApolloJsonValueProcessor;
import com.ctrip.framework.apollo.spring.annotation.SpringValueProcessor;
import com.ctrip.framework.apollo.spring.property.SpringValueDefinitionProcessor;
import com.ctrip.framework.apollo.spring.util.BeanRegistrationUtil;
import com.google.common.collect.Maps;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * 默认配置PropertiesSources处理帮助类，用于处理 Spring XML 的配置
 */
public class DefaultConfigPropertySourcesProcessorHelper implements
    ConfigPropertySourcesProcessorHelper {

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {
    Map<String, Object> propertySourcesPlaceholderPropertyValues = Maps.newHashMap();
    // 确保默认PropertySourcesPlaceholderConfigurer的优先级高于PropertyPlaceholderConfigurer
    propertySourcesPlaceholderPropertyValues.put("order", 0);

    // 注册 PropertySourcesPlaceholderConfigurer 到 BeanDefinitionRegistry 中，替换 PlaceHolder 为对应的属性值
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
        PropertySourcesPlaceholderConfigurer.class.getName(),
        PropertySourcesPlaceholderConfigurer.class, propertySourcesPlaceholderPropertyValues);

    // 注册 ApolloAnnotationProcessor 到 BeanDefinitionRegistry 中，因为 XML 配置的 Bean 对象，也可能存在 @ApolloConfig 和 @ApolloConfigChangeListener 注解。
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
        ApolloAnnotationProcessor.class.getName(), ApolloAnnotationProcessor.class);
    // 注册 SpringValueProcessor 到 BeanDefinitionRegistry 中，用于 PlaceHolder 自动更新机制
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
        SpringValueProcessor.class.getName(), SpringValueProcessor.class);
    // 注册 ApolloJsonValueProcessor 到 BeanDefinitionRegistry 中，因为 XML 配置的 Bean 对象，也可能存在 @ApolloJsonValue 注解。
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
        ApolloJsonValueProcessor.class.getName(), ApolloJsonValueProcessor.class);
    // 处理 XML 配置的 Spring PlaceHolder
    processSpringValueDefinition(registry);
  }

  /**
   * 在 Spring 3.x 版本中，BeanDefinitionRegistryPostProcessor ( SpringValueDefinitionProcessor 实现了该接口
   * )无法被实例化，在 postProcessBeanDefinitionRegistry 阶段，因此，我们不得不手动创建 SpringValueDefinitionProcessor
   * 对象，并调用其 #postProcessBeanDefinitionRegistry(BeanDefinitionRegistry) 方法。
   */
  private void processSpringValueDefinition(BeanDefinitionRegistry registry) {
    // 创建 SpringValueDefinitionProcessor 对象
    SpringValueDefinitionProcessor springValueDefinitionProcessor = new SpringValueDefinitionProcessor();
    // 处理 XML 配置的 Spring PlaceHolder
    springValueDefinitionProcessor.postProcessBeanDefinitionRegistry(registry);
  }

  @Override
  public int getOrder() {
    // 最小的优先级
    return Ordered.LOWEST_PRECEDENCE;
  }
}
