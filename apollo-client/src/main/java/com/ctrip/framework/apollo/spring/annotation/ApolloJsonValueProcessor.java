package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.spring.property.PlaceholderHelper;
import com.ctrip.framework.apollo.spring.property.SpringValue;
import com.ctrip.framework.apollo.spring.property.SpringValueRegistry;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 * {@link ApolloJsonValue} 注解处理器，有两个作用：
 *
 * <ol><li>注入 @ApolloJsonValue 注解的属性或方法，对应的值。</li>
 * <li>自动更新 Spring Placeholder Values 。</li>
 * </ol>
 *
 * @author zhangzheng on 2018/2/6
 */
@Slf4j
public class ApolloJsonValueProcessor extends ApolloProcessor implements BeanFactoryAware {

  private static final Gson GSON = new Gson();
  /**
   * 配置工具类
   */
  private final ConfigUtil configUtil;
  /**
   * 占位符帮助类
   */
  private final PlaceholderHelper placeholderHelper;
  /**
   * SpringValue 注册表
   */
  private final SpringValueRegistry springValueRegistry;
  /**
   * 配置Bean工厂
   */
  private ConfigurableBeanFactory beanFactory;

  /**
   * 构建ApolloJsonValueProcessor，初始化属性
   */
  public ApolloJsonValueProcessor() {
    configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
    springValueRegistry = SpringInjector.getInstance(SpringValueRegistry.class);
  }

  @Override
  protected void processField(Object bean, String beanName, Field field) {
    ApolloJsonValue apolloJsonValue = AnnotationUtils.getAnnotation(field, ApolloJsonValue.class);
    if (apolloJsonValue == null) {
      return;
    }
    // 获得 Placeholder 表达式
    String placeholder = apolloJsonValue.value();
    // 解析对应的值
    Object propertyValue = placeholderHelper
        .resolvePropertyValue(beanFactory, beanName, placeholder);

    // propertyValue will never be null, as @ApolloJsonValue will not allow that
    // 忽略，非 String 值
    if (!(propertyValue instanceof String)) {
      return;
    }

    // 设置到 Field 中
    boolean accessible = field.isAccessible();
    field.setAccessible(true);
    ReflectionUtils
        .setField(field, bean, parseJsonValue((String) propertyValue, field.getGenericType()));
    field.setAccessible(accessible);

    // 是否开启自动更新机制
    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
      // 提取 `keys` 属性集
      Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeholder);
      // 循环 `keys` ，创建对应的 SpringValue 对象，并添加到 `springValueRegistry` 中。
      for (String key : keys) {
        SpringValue springValue = new SpringValue(key, placeholder, bean, beanName, field, true);
        springValueRegistry.register(beanFactory, key, springValue);
        log.debug("Monitoring {}", springValue);
      }
    }
  }

  @Override
  protected void processMethod(Object bean, String beanName, Method method) {
    ApolloJsonValue apolloJsonValue = AnnotationUtils.getAnnotation(method, ApolloJsonValue.class);
    if (apolloJsonValue == null) {
      return;
    }
    // 获得 Placeholder 表达式
    String placeHolder = apolloJsonValue.value();

    Object propertyValue = placeholderHelper
        .resolvePropertyValue(beanFactory, beanName, placeHolder);

    // propertyValue will never be null, as @ApolloJsonValue will not allow that
    // 忽略，非 String 值
    if (!(propertyValue instanceof String)) {
      return;
    }

    Type[] types = method.getGenericParameterTypes();
    Preconditions.checkArgument(types.length == 1,
        "Ignore @Value setter {}.{}, expecting 1 parameter, actual {} parameters",
        bean.getClass().getName(), method.getName(), method.getParameterTypes().length);

    // 调用 Method ，设置值
    boolean accessible = method.isAccessible();
    method.setAccessible(true);
    ReflectionUtils.invokeMethod(method, bean, parseJsonValue((String) propertyValue, types[0]));
    method.setAccessible(accessible);

    // 是否开启自动更新机制
    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
      // 提取 `keys` 属性集
      Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeHolder);
      // 循环 `keys` ，创建对应的 SpringValue 对象，并添加到 `springValueRegistry` 中
      for (String key : keys) {
        SpringValue springValue = new SpringValue(key, apolloJsonValue.value(), bean, beanName,
            method, true);
        springValueRegistry.register(beanFactory, key, springValue);
        log.debug("Monitoring {}", springValue);
      }
    }
  }

  /**
   * 将JSON字符串解析为指定类型的对象
   *
   * @param json       JSON字符串
   * @param targetType 对应值的类型
   * @return 对应值类型的对象
   */
  private Object parseJsonValue(String json, Type targetType) {
    try {
      return GSON.fromJson(json, targetType);
    } catch (Throwable ex) {
      log.error("Parsing json '{}' to type {} failed!", json, targetType, ex);
      throw ex;
    }
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    // 设置bean工厂
    this.beanFactory = (ConfigurableBeanFactory) beanFactory;
  }
}
