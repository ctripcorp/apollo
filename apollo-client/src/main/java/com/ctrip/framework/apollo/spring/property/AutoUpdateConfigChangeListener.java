package com.ctrip.framework.apollo.spring.property;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.SpringValueProcessor;
import com.ctrip.framework.apollo.spring.processor.ValueMappingProcessor;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

/**
 * Create by zhangzheng on 2018/3/6
 */
public class AutoUpdateConfigChangeListener implements ConfigChangeListener{
  private static final Logger logger = LoggerFactory.getLogger(SpringValueProcessor.class);

  private final boolean typeConverterHasConvertIfNecessaryWithFieldParameter;
  private final Environment environment;
  private final ConfigurableBeanFactory beanFactory;
  private final TypeConverter typeConverter;
  private final PlaceholderHelper placeholderHelper;
  private final SpringValueRegistry springValueRegistry;
  private final ValueMappingProcessor valueMappingProcessor;
  private final Gson gson;

  public AutoUpdateConfigChangeListener(Environment environment, ConfigurableListableBeanFactory beanFactory){
    this.typeConverterHasConvertIfNecessaryWithFieldParameter = testTypeConverterHasConvertIfNecessaryWithFieldParameter();
    this.beanFactory = beanFactory;
    this.typeConverter = this.beanFactory.getTypeConverter();
    this.environment = environment;
    this.placeholderHelper = ApolloInjector.getInstance(PlaceholderHelper.class);
    this.springValueRegistry = ApolloInjector.getInstance(SpringValueRegistry.class);
    this.valueMappingProcessor = ApolloInjector.getInstance(ValueMappingProcessor.class);
    this.gson = new Gson();
  }

  @Override
  public void onChange(ConfigChangeEvent changeEvent) {
    Set<String> keys = changeEvent.changedKeys();
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }

    Set<SpringValue> valueMappingSet = new HashSet<>();
    for (String key : keys) {
      // 1. check whether the changed key is relevant
      Collection<SpringValue> targetValues = springValueRegistry.get(key);
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
        if (val.isValueMapping()) {
          // 3.1 collect valueMapping value at first, in case update repeatedly
          valueMappingSet.add(val);
        } else {
          updateSpringValue(val);
        }
      }
    }
    
    // 5. collect valueMapping value by Apollo namespace
    Collection<SpringValue> targetValues =
        springValueRegistry.getByNamespace(changeEvent.getNamespace());
    if (!CollectionUtils.isEmpty(targetValues)) {
      for (SpringValue val : targetValues) {
        if (val.isValueMapping()) {
          // 5.1 collect valueMapping value at first, in case update repeatedly
          valueMappingSet.add(val);
        }
      }
    }

    // 6. update valueMapping value
    for (SpringValue val : valueMappingSet) {
      if (valueMappingProcessor.isPropertyChanged(val.getValueMappingElement(), changeEvent,
          environment)) {
        updateSpringValue(val);
      }
    }
  }

  private void updateSpringValue(SpringValue springValue) {
    try {
      Object value;
      if (springValue.isValueMapping()) {
        value = valueMappingProcessor.updateProperty(springValue.getBean(),
            springValue.getValueMappingElement(), environment);
      } else {
        value = resolvePropertyValue(springValue);
        springValue.update(value);
      }

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
    // value will never be null, as @Value and @ApolloJsonValue will not allow that
    Object value = placeholderHelper
        .resolvePropertyValue(beanFactory, springValue.getBeanName(), springValue.getPlaceholder());

    if (springValue.isJson()) {
      value = parseJsonValue((String)value, springValue.getGenericType());
    } else {
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
    }

    return value;
  }

  private Object parseJsonValue(String json, Type targetType) {
    try {
      return gson.fromJson(json, targetType);
    } catch (Throwable ex) {
      logger.error("Parsing json '{}' to type {} failed!", json, targetType, ex);
      throw ex;
    }
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
