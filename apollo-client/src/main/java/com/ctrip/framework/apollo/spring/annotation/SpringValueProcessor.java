package com.ctrip.framework.apollo.spring.annotation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.property.PlaceholderHelper;
import com.ctrip.framework.apollo.spring.property.SpringValue;
import com.ctrip.framework.apollo.spring.property.SpringValueDefinition;
import com.ctrip.framework.apollo.spring.property.SpringValueDefinitionProcessor;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * Spring value processor of field or method which has @Value and xml config placeholders.
 *
 * @author github.com/zhegexiaohuozi  seimimaster@gmail.com
 * @since 2017/12/20.
 */
public class SpringValueProcessor implements BeanPostProcessor, PriorityOrdered, EnvironmentAware,
    BeanFactoryAware, BeanFactoryPostProcessor {

  private static final Logger logger = LoggerFactory.getLogger(SpringValueProcessor.class);

  private final Multimap<String, SpringValue> monitor = LinkedListMultimap.create();
  private final ConfigUtil configUtil;
  private final PlaceholderHelper placeholderHelper;
  private final ConfigPropertySourceFactory configPropertySourceFactory;
  private final boolean typeConverterHasConvertIfNecessaryWithFieldParameter;

  private Environment environment;
  private ConfigurableBeanFactory beanFactory;
  private TypeConverter typeConverter;

  private static Multimap<String, SpringValueDefinition> beanName2SpringValueDefinitions = LinkedListMultimap.create();

  public SpringValueProcessor() {
    configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    placeholderHelper = ApolloInjector.getInstance(PlaceholderHelper.class);
    configPropertySourceFactory = ApolloInjector.getInstance(ConfigPropertySourceFactory.class);
    typeConverterHasConvertIfNecessaryWithFieldParameter = testTypeConverterHasConvertIfNecessaryWithFieldParameter();
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = (ConfigurableBeanFactory) beanFactory;
    this.typeConverter = this.beanFactory.getTypeConverter();
  }

  @Override
  public void setEnvironment(Environment env) {
    this.environment = env;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
      beanName2SpringValueDefinitions = SpringValueDefinitionProcessor.getBeanName2SpringValueDefinitions();
      registerConfigChangeListener();
    }
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
      Class clazz = bean.getClass();
      processFields(bean, beanName, findAllField(clazz));
      processMethods(bean, beanName, findAllMethod(clazz));
      processBeanPropertyValues(bean, beanName);
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  private void processFields(Object bean, String beanName, List<Field> declaredFields) {
    for (Field field : declaredFields) {
      // register @Value on field
      Value value = field.getAnnotation(Value.class);
      if (value == null) {
        continue;
      }
      Set<String> keys = placeholderHelper.extractPlaceholderKeys(value.value());

      if (keys.isEmpty()) {
        continue;
      }

      for (String key : keys) {
        SpringValue springValue = new SpringValue(key, value.value(), bean, beanName, field);
        monitor.put(key, springValue);
        logger.debug("Monitoring {}", springValue);
      }
    }
  }

  private void processMethods(final Object bean, String beanName, List<Method> declaredMethods) {
    for (final Method method : declaredMethods) {
      //register @Value on method
      Value value = method.getAnnotation(Value.class);
      if (value == null) {
        continue;
      }
      //skip Configuration bean methods
      if (method.getAnnotation(Bean.class) != null) {
        continue;
      }
      if (method.getParameterTypes().length != 1) {
        logger.error("Ignore @Value setter {}.{}, expecting 1 parameter, actual {} parameters",
            bean.getClass().getName(), method.getName(), method.getParameterTypes().length);
        continue;
      }

      Set<String> keys = placeholderHelper.extractPlaceholderKeys(value.value());

      if (keys.isEmpty()) {
        continue;
      }

      for (String key : keys) {
        SpringValue springValue = new SpringValue(key, value.value(), bean, beanName, method);
        monitor.put(key, springValue);
        logger.debug("Monitoring {}", springValue);
      }
    }
  }

  private void processBeanPropertyValues(Object bean, String beanName) {
    Collection<SpringValueDefinition> propertySpringValues = beanName2SpringValueDefinitions
        .get(beanName);
    if (propertySpringValues == null || propertySpringValues.isEmpty()) {
      return;
    }

    for (SpringValueDefinition definition : propertySpringValues) {
      try {
        PropertyDescriptor pd = BeanUtils
            .getPropertyDescriptor(bean.getClass(), definition.getPropertyName());
        Method method = pd.getWriteMethod();
        if (method == null) {
          continue;
        }
        SpringValue springValue = new SpringValue(definition.getKey(), definition.getPlaceholder(),
            bean, beanName, method);
        monitor.put(definition.getKey(), springValue);
        logger.debug("Monitoring {}", springValue);
      } catch (Throwable ex) {
        logger.error("Failed to enable auto update feature for {}.{}", bean.getClass(),
            definition.getPropertyName());
      }
    }

    // clear
    beanName2SpringValueDefinitions.removeAll(beanName);
  }

  private List<Field> findAllField(Class clazz) {
    final List<Field> res = new LinkedList<>();
    ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
      @Override
      public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        res.add(field);
      }
    });
    return res;
  }

  private List<Method> findAllMethod(Class clazz) {
    final List<Method> res = new LinkedList<>();
    ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
      @Override
      public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        res.add(method);
      }
    });
    return res;
  }

  private void registerConfigChangeListener() {
    ConfigChangeListener changeListener = new ConfigChangeListener() {
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
            updateSpringValue(val);
          }
        }
      }
    };

    List<ConfigPropertySource> configPropertySources = configPropertySourceFactory.getAllConfigPropertySources();

    for (ConfigPropertySource configPropertySource : configPropertySources) {
      configPropertySource.addChangeListener(changeListener);
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

  @Override
  public int getOrder() {
    //make it as late as possible
    return Ordered.LOWEST_PRECEDENCE;
  }
}
