package com.ctrip.framework.apollo.spring.property;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * 处理xml配置占位符，处理 Spring XML PlaceHolder ，解析成 StringValueDefinition 集合。
 * <p>例如:
 * <pre>
 *  &lt;bean class=&quot;com.ctrip.framework.apollo.demo.spring.xmlConfigDemo.bean.XmlBean&quot;&gt;
 *    &lt;property name=&quot;timeout&quot; value=&quot;${timeout:200}&quot;/&gt;
 *    &lt;property name=&quot;batch&quot; value=&quot;${batch:100}&quot;/&gt;
 *  &lt;/bean&gt;
 * </pre>
 */
public class SpringValueDefinitionProcessor implements BeanDefinitionRegistryPostProcessor {

  /**
   * SpringValueDefinition 集合
   * <p>
   * KEY：beanName VALUE：SpringValueDefinition 集合
   */
  private static final Map<BeanDefinitionRegistry, Multimap<String, SpringValueDefinition>> beanName2SpringValueDefinitions =
      Maps.newConcurrentMap();
  /**
   * 属性值处理的Bean工厂，BeanDefinitionRegistry列表
   */
  private static final Set<BeanDefinitionRegistry> PROPERTY_VALUES_PROCESSED_BEAN_FACTORIES = Sets
      .newConcurrentHashSet();
  /**
   * 配置工具类
   */
  private final ConfigUtil configUtil;
  /**
   * 占位符帮助类函数
   */
  private final PlaceholderHelper placeholderHelper;

  /**
   * 构建，并初始化基本属性
   */
  public SpringValueDefinitionProcessor() {
    configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {
    // 是否开启自动更新功能，因为 SpringValueDefinitionProcessor 就是为了这个功能编写的。
    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
      processPropertyValues(registry);
    }
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {

  }

  /**
   * 获取 SpringValueDefinition 集合
   *
   * @param registry Bean定义注册器
   * @return SpringValueDefinition 集合
   */
  public static Multimap<String, SpringValueDefinition> getBeanName2SpringValueDefinitions(
      BeanDefinitionRegistry registry) {
    // 存在直接返回，不存在创建，返回空
    Multimap<String, SpringValueDefinition> springValueDefinitions = beanName2SpringValueDefinitions
        .get(registry);
    if (springValueDefinitions == null) {
      springValueDefinitions = LinkedListMultimap.create();
    }

    return springValueDefinitions;
  }

  private void processPropertyValues(BeanDefinitionRegistry beanRegistry) {
    // 若已经初始化，直接返回
    if (!PROPERTY_VALUES_PROCESSED_BEAN_FACTORIES.add(beanRegistry)) {
      // already initialized
      return;
    }

    // 循环 BeanDefinition 集合
    if (!beanName2SpringValueDefinitions.containsKey(beanRegistry)) {
      beanName2SpringValueDefinitions
          .put(beanRegistry, LinkedListMultimap.<String, SpringValueDefinition>create());
    }

    Multimap<String, SpringValueDefinition> springValueDefinitions = beanName2SpringValueDefinitions
        .get(beanRegistry);
    // 循环 BeanDefinition 的 PropertyValue 数组
    String[] beanNames = beanRegistry.getBeanDefinitionNames();
    for (String beanName : beanNames) {
      BeanDefinition beanDefinition = beanRegistry.getBeanDefinition(beanName);
      // 循环 BeanDefinition 的 PropertyValue 数组
      MutablePropertyValues mutablePropertyValues = beanDefinition.getPropertyValues();
      List<PropertyValue> propertyValues = mutablePropertyValues.getPropertyValueList();
      for (PropertyValue propertyValue : propertyValues) {
        // 获得 `value` 属性。
        Object value = propertyValue.getValue();
        // 忽略非 Spring PlaceHolder 的 `value` 属性。
        if (!(value instanceof TypedStringValue)) {
          continue;
        }
        // 获得 `placeholder` 属性。
        String placeholder = ((TypedStringValue) value).getValue();
        // 提取 `keys` 属性们。
        Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeholder);

        if (keys.isEmpty()) {
          continue;
        }

        // 循环`keys`,创建对应的 SpringValueDefinition 对象,并添加到`beanName2SpringValueDefinitions`中。
        for (String key : keys) {
          springValueDefinitions
              .put(beanName, new SpringValueDefinition(key, placeholder, propertyValue.getName()));
        }
      }
    }
  }
}
