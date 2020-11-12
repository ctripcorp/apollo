package com.ctrip.framework.apollo.spring.spi;

import com.ctrip.framework.apollo.core.spi.Ordered;
import com.ctrip.framework.apollo.spring.annotation.ApolloAnnotationProcessor;
import com.ctrip.framework.apollo.spring.annotation.ApolloJsonValueProcessor;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.SpringValueProcessor;
import com.ctrip.framework.apollo.spring.config.PropertySourcesProcessor;
import com.ctrip.framework.apollo.spring.property.SpringValueDefinitionProcessor;
import com.ctrip.framework.apollo.spring.util.BeanRegistrationUtil;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 默认的Apollo Spring Java Config 注册器帮助类
 */
public class DefaultApolloConfigRegistrarHelper implements ApolloConfigRegistrarHelper {

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
      BeanDefinitionRegistry registry) {
    // 解析 @EnableApolloConfig 注解
    AnnotationAttributes attributes = AnnotationAttributes
        .fromMap(
            importingClassMetadata.getAnnotationAttributes(EnableApolloConfig.class.getName()));
    String[] namespaces = attributes.getStringArray("value");
    int order = attributes.getNumber("order");
    // 添加到 PropertySourcesProcessor 中
    PropertySourcesProcessor.addNamespaces(Lists.newArrayList(namespaces), order);

    Map<String, Object> propertySourcesPlaceholderPropertyValues = new HashMap<>();
    // 确保默认PropertySourcesPlaceholderConfigurer的优先级高于PropertyPlaceholderConfigurer
    propertySourcesPlaceholderPropertyValues.put("order", 0);

    // 注册 PropertySourcesPlaceholderConfigurer 到 BeanDefinitionRegistry 中，替换 PlaceHolder 为对应的属性值
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
        PropertySourcesPlaceholderConfigurer.class.getName(),
        PropertySourcesPlaceholderConfigurer.class, propertySourcesPlaceholderPropertyValues);
    //【差异】注册 PropertySourcesProcessor 到 BeanDefinitionRegistry 中，因为可能存在 XML 配置的 Bean ，用于 PlaceHolder 自动更新机制
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
        PropertySourcesProcessor.class.getName(), PropertySourcesProcessor.class);
    // 注册 ApolloAnnotationProcessor 到 BeanDefinitionRegistry 中，解析 @ApolloConfig 和 @ApolloConfigChangeListener 注解。
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
        ApolloAnnotationProcessor.class.getName(), ApolloAnnotationProcessor.class);
    // 注册 SpringValueProcessor 到 BeanDefinitionRegistry 中，用于 PlaceHolder 自动更新机制
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
        SpringValueProcessor.class.getName(), SpringValueProcessor.class);
    //【差异】注册 SpringValueDefinitionProcessor 到 BeanDefinitionRegistry 中，因为可能存在 XML 配置的 Bean ，用于 PlaceHolder 自动更新机制
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
        SpringValueDefinitionProcessor.class.getName(), SpringValueDefinitionProcessor.class);
    // 注册 ApolloJsonValueProcessor 到 BeanDefinitionRegistry 中，解析 @ApolloJsonValue 注解。
    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
        ApolloJsonValueProcessor.class.getName(), ApolloJsonValueProcessor.class);
  }

  @Override
  public int getOrder() {
    // 默认最小的优先级
    return Ordered.LOWEST_PRECEDENCE;
  }
}
