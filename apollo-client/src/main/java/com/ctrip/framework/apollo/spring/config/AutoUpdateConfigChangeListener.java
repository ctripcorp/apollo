package com.ctrip.framework.apollo.spring.config;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.SpringValueProcessor;
import com.ctrip.framework.apollo.spring.property.SpringValue;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

/**
 * Create by zhangzheng on 2018/3/6
 */
public class AutoUpdateConfigChangeListener implements ConfigChangeListener{

  public static final Multimap<String, SpringValue> monitor = LinkedListMultimap.create();

  private Environment environment;
  private ConfigurableBeanFactory beanFactory;
  private TypeConverter typeConverter;
  private static final Logger logger = LoggerFactory.getLogger(SpringValueProcessor.class);
  private final boolean typeConverterHasConvertIfNecessaryWithFieldParameter;
  private Gson gson = new Gson();

  public AutoUpdateConfigChangeListener(Environment environment, BeanFactory beanFactory){
    typeConverterHasConvertIfNecessaryWithFieldParameter = testTypeConverterHasConvertIfNecessaryWithFieldParameter();
    this.beanFactory = (ConfigurableBeanFactory) beanFactory;
    this.typeConverter = this.beanFactory.getTypeConverter();
    this.environment = environment;
  }


  @Override
  public void onChange(ConfigChangeEvent changeEvent) {
    Set<String> keys = changeEvent.changedKeys();
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }
    for (String key : keys) {
      // 1. check whether the changed key is relevant
      Collection<SpringValue> targetValues = monitor.get(key);
      if (targetValues == null || targetValues.isEmpty()) {
        continue;
      }

      // 2. check whether the value is really changed or not (since spring property sources have hierarchies)
      ConfigChange configChange = changeEvent.getChange(key);
      if (!Objects.equals(environment.getProperty(key), configChange.getNewValue())) {
        continue;
      }

      // 3. update the value
      for (SpringValue val : targetValues) {
        if(val.isJson()){
          updateJsonValue(val, environment.getProperty(key));
        }else{
          updateSpringValue(val);
        }
      }
    }
  }

  private void updateJsonValue(SpringValue springValue, String propertyValue){
    try {
      Type type = springValue.getGenericType();
      Object val = gson.fromJson(propertyValue, type);
      springValue.update(val);
      logger.debug("Auto update apollo changed value successfully, new value: {}, {}", val,
          springValue.toString());
    } catch (Throwable ex) {
      logger.error("Auto update apollo changed value failed, {}", springValue.toString(), ex);
    }
  }

  private void updateSpringValue(SpringValue springValue) {
    try {
      Object value = resolvePropertyValue(springValue);
      springValue.update(value);

      logger.debug("Auto update apollo changed value successfully, new value: {}, {}", value,
          springValue.toString());
    } catch (Throwable ex) {
      logger.error("Auto update apollo changed value failed, {}", springValue.toString(), ex);
    }
  }

  /**
   * Logic transplanted from DefaultListableBeanFactory
   * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
   */
  private Object resolvePropertyValue(SpringValue springValue) {
    String strVal = beanFactory.resolveEmbeddedValue(springValue.getPlaceholder());
    Object value;

    BeanDefinition bd = (beanFactory.containsBean(springValue.getBeanName()) ? beanFactory
        .getMergedBeanDefinition(springValue.getBeanName()) : null);
    value = evaluateBeanDefinitionString(strVal, bd);

    if (springValue.isField()) {
      // org.springframework.beans.TypeConverter#convertIfNecessary(java.lang.Object, java.lang.Class, java.lang.reflect.Field) is available from Spring 3.2.0+
      if (typeConverterHasConvertIfNecessaryWithFieldParameter) {
        value = this.typeConverter
            .convertIfNecessary(value, springValue.getTargetType(), springValue.getField());
      } else {
        value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType());
      }
    } else {
      value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType(),
          springValue.getMethodParameter());
    }

    return value;
  }

  private Object evaluateBeanDefinitionString(String value, BeanDefinition beanDefinition) {
    if (beanFactory.getBeanExpressionResolver() == null) {
      return value;
    }
    Scope scope = (beanDefinition != null ? beanFactory.getRegisteredScope(beanDefinition.getScope()) : null);
    return beanFactory.getBeanExpressionResolver().evaluate(value, new BeanExpressionContext(beanFactory, scope));
  }

  private boolean testTypeConverterHasConvertIfNecessaryWithFieldParameter() {
    try {
      TypeConverter.class.getMethod("convertIfNecessary", Object.class, Class.class, Field.class);
    } catch (Throwable ex) {
      return false;
    }

    return true;
  }


}
