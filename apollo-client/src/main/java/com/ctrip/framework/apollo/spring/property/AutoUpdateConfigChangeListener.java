package com.ctrip.framework.apollo.spring.property;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

/**
 * 自动更新配置监听器
 *
 * @author zhangzheng on 2018/3/6
 */
@Slf4j
public class AutoUpdateConfigChangeListener implements ConfigChangeListener {

  /**
   * {@link TypeConverter#convertIfNecessary(Object, Class, Field)} 是否带上 Field 参数，因为 Spring 3.2.0+
   * 才有该方法
   */
  private final boolean typeConverterHasConvertIfNecessaryWithFieldParameter;
  /**
   * 环境信息
   */
  private final Environment environment;
  /**
   * 配置bean的工厂
   */
  private final ConfigurableBeanFactory beanFactory;
  /**
   * 类型转换器
   */
  private final TypeConverter typeConverter;
  /**
   * 占位符帮助类
   */
  private final PlaceholderHelper placeholderHelper;
  /**
   * Spring的@Value注册表
   */
  private final SpringValueRegistry springValueRegistry;
  /**
   * gson对象
   */
  private final Gson gson;

  /**
   * 构建AutoUpdateConfigChangeListener，初始化属性
   *
   * @param environment 环境
   * @param beanFactory 可配置列表BeanFactory
   */
  public AutoUpdateConfigChangeListener(Environment environment,
      ConfigurableListableBeanFactory beanFactory) {
    this.typeConverterHasConvertIfNecessaryWithFieldParameter = testTypeConverterHasConvertIfNecessaryWithFieldParameter();
    this.beanFactory = beanFactory;
    this.typeConverter = this.beanFactory.getTypeConverter();
    this.environment = environment;
    this.placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
    this.springValueRegistry = SpringInjector.getInstance(SpringValueRegistry.class);
    this.gson = new Gson();
  }

  @Override
  public void onChange(ConfigChangeEvent changeEvent) {
    // 获得更新的 KEY 集合
    Set<String> keys = changeEvent.changedKeys();
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }
    // 循环 KEY 集合，更新 StringValue
    for (String key : keys) {
      // 1. 检查更改后的密钥是否有意义
      // 忽略，若不在 SpringValueRegistry 中
      Collection<SpringValue> targetValues = springValueRegistry.get(beanFactory, key);
      if (CollectionUtils.isEmpty(targetValues)) {
        continue;
      }

      // 循环，更新 SpringValue
      for (SpringValue val : targetValues) {
        updateSpringValue(val);
      }
    }
  }

  /**
   * 更新SpringValue的值
   *
   * @param springValue spring@Value的信息
   */
  private void updateSpringValue(SpringValue springValue) {
    try {
      // 解析值
      Object value = resolvePropertyValue(springValue);
      // 更新 StringValue
      springValue.update(value);

      log.info("Auto update apollo changed value successfully, new value: {}, {}", value,
          springValue);
    } catch (Throwable ex) {
      log.error("Auto update apollo changed value failed, {}", springValue.toString(), ex);
    }
  }

  /**
   * 解析属性值，从DefaultListableBeanFactory拷贝的逻辑
   *
   * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor,
   * java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
   */
  private Object resolvePropertyValue(SpringValue springValue) {
    // value永远不会为null，因为@Value和 @ApolloJsonValue 不允许这样做
    Object value = placeholderHelper
        .resolvePropertyValue(beanFactory, springValue.getBeanName(), springValue.getPlaceholder());
    // 如果值数据结构是 JSON 类型，则使用 Gson 解析成对应值的类型
    if (springValue.isJson()) {
      value = parseJsonValue((String) value, springValue.getGenericType());
    } else {
      // 如果类型为 Field
      if (springValue.isField()) {
        // org.springframework.beans.TypeConverter#convertIfNecessary(java.lang.Object, java.lang.Class, java.lang.reflect.Field) 可用于 Spring 3.2.0+
        if (typeConverterHasConvertIfNecessaryWithFieldParameter) {
          value = this.typeConverter
              .convertIfNecessary(value, springValue.getTargetType(), springValue.getField());
        } else {
          value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType());
        }
      } else {
        // 如果类型为 Method
        value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType(),
            springValue.getMethodParameter());
      }
    }

    return value;
  }

  /**
   * 解析JsonValue
   *
   * @param json       Json字符串
   * @param targetType 解析后的类型
   * @return 解析后的实体对象
   */
  private Object parseJsonValue(String json, Type targetType) {
    try {
      return gson.fromJson(json, targetType);
    } catch (Throwable ex) {
      log.error("Parsing json '{}' to type {} failed!", json, targetType, ex);
      throw ex;
    }
  }

  /**
   * 测试{@link TypeConverter#convertIfNecessary(Object, Class, Field)}支持字段参数
   *
   * @return true，支持，否则，false
   */
  private boolean testTypeConverterHasConvertIfNecessaryWithFieldParameter() {
    try {
      TypeConverter.class.getMethod("convertIfNecessary", Object.class, Class.class, Field.class);
    } catch (Throwable ex) {
      return false;
    }

    return true;
  }
}
