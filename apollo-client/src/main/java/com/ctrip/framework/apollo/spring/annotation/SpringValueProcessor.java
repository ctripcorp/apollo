package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.spring.property.PlaceholderHelper;
import com.ctrip.framework.apollo.spring.property.SpringValue;
import com.ctrip.framework.apollo.spring.property.SpringValueDefinition;
import com.ctrip.framework.apollo.spring.property.SpringValueDefinitionProcessor;
import com.ctrip.framework.apollo.spring.property.SpringValueRegistry;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;

/**
 * Spring Value处理器
 * <p>处理：
 * <ol>
 *   <li>带有 @Value 注解的 Field 和 Method</li>
 *   <li>XML 配置的 Bean 的 PlaceHolder 们</li>
 * </ol>
 *
 * @author github.com/zhegexiaohuozi  seimimaster@gmail.com
 * @since 2017/12/20.
 */
@Slf4j
public class SpringValueProcessor extends ApolloProcessor implements BeanFactoryPostProcessor,
    BeanFactoryAware {

  /**
   * 配置工具类
   */
  private final ConfigUtil configUtil;
  /**
   * 占位符帮助类
   */
  private final PlaceholderHelper placeholderHelper;
  /**
   * Spring的@Value注册表对象
   */
  private final SpringValueRegistry springValueRegistry;
  /**
   * bean工厂
   */
  private BeanFactory beanFactory;
  /**
   * SpringValueDefinition 集合  KEY：beanName VALUE：SpringValueDefinition 集合
   */
  private Multimap<String, SpringValueDefinition> beanName2SpringValueDefinitions;

  /**
   * 构建SpringValueProcessor，初始化属性
   */
  public SpringValueProcessor() {
    configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
    springValueRegistry = SpringInjector.getInstance(SpringValueRegistry.class);
    beanName2SpringValueDefinitions = LinkedListMultimap.create();
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    // 是否开启自动更新机制
    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()
        && beanFactory instanceof BeanDefinitionRegistry) {
      // 获取SpringValueDefinition 集合
      beanName2SpringValueDefinitions = SpringValueDefinitionProcessor
          .getBeanName2SpringValueDefinitions((BeanDefinitionRegistry) beanFactory);
    }
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    // 是否开启自动更新机制cc
    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
      // 是否开启自动更新机制
      super.postProcessBeforeInitialization(bean, beanName);
      // 处理 XML 配置的 Bean 的 PlaceHolder列表
      processBeanPropertyValues(bean, beanName);
    }
    return bean;
  }


  @Override
  protected void processField(Object bean, String beanName, Field field) {
    // 注册在字段上的@Value对象
    Value value = field.getAnnotation(Value.class);
    if (value == null) {
      return;
    }

    // 提取 `keys` 属性集。
    Set<String> keys = placeholderHelper.extractPlaceholderKeys(value.value());
    if (keys.isEmpty()) {
      return;
    }

    // 循环 `keys` ，创建对应的 SpringValue 对象，并添加到 `springValueRegistry` 中。
    for (String key : keys) {
      SpringValue springValue = new SpringValue(key, value.value(), bean, beanName, field, false);
      springValueRegistry.register(beanFactory, key, springValue);
      log.debug("Monitoring {}", springValue);
    }
  }

  @Override
  protected void processMethod(Object bean, String beanName, Method method) {
    // 注册在方法上的@Value对象
    Value value = method.getAnnotation(Value.class);
    if (value == null) {
      return;
    }
    // 忽略 @Bean 注解的方法
    if (method.getAnnotation(Bean.class) != null) {
      return;
    }
    // 忽略非 setting 方法
    if (method.getParameterTypes().length != 1) {
      log.error("Ignore @Value setter {}.{}, expecting 1 parameter, actual {} parameters",
          bean.getClass().getName(), method.getName(), method.getParameterTypes().length);
      return;
    }

    // 提取 `keys` 属性集。
    Set<String> keys = placeholderHelper.extractPlaceholderKeys(value.value());

    if (keys.isEmpty()) {
      return;
    }

    // 循环 `keys` ，创建对应的 SpringValue 对象，并添加到 `springValueRegistry` 中。
    for (String key : keys) {
      SpringValue springValue = new SpringValue(key, value.value(), bean, beanName, method, false);
      springValueRegistry.register(beanFactory, key, springValue);
      log.info("Monitoring {}", springValue);
    }
  }

  /**
   * 处理 XML 配置的 Bean 的 PlaceHolder列表
   *
   * @param bean     指定的bean
   * @param beanName bean的名称
   */
  private void processBeanPropertyValues(Object bean, String beanName) {
    // 获得 SpringValueDefinition 数组
    Collection<SpringValueDefinition> propertySpringValues = beanName2SpringValueDefinitions
        .get(beanName);
    if (CollectionUtils.isEmpty(propertySpringValues)) {
      return;
    }

    // 循环 SpringValueDefinition 数组，创建对应的 SpringValue 对象，并添加到 `springValueRegistry` 中。
    for (SpringValueDefinition definition : propertySpringValues) {
      try {
        PropertyDescriptor pd = BeanUtils
            .getPropertyDescriptor(bean.getClass(), definition.getPropertyName());
        // 获得用于写入属性值的方法，为空跳过
        Method method = pd.getWriteMethod();
        if (method == null) {
          continue;
        }
        SpringValue springValue = new SpringValue(definition.getKey(), definition.getPlaceholder(),
            bean, beanName, method, false);
        springValueRegistry.register(beanFactory, definition.getKey(), springValue);
        log.debug("Monitoring {}", springValue);
      } catch (Throwable ex) {
        log.error("Failed to enable auto update feature for {}.{}", bean.getClass(),
            definition.getPropertyName());
      }
    }

    // clear
    // 移除 Bean 对应的 SpringValueDefinition 数组
    beanName2SpringValueDefinitions.removeAll(beanName);
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }
}
