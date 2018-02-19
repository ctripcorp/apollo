package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.auto.SpringFieldValue;
import com.ctrip.framework.apollo.spring.auto.SpringMethodValue;
import com.ctrip.framework.apollo.spring.auto.SpringValue;
import com.ctrip.framework.apollo.spring.config.PropertySourcesProcessor;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

/**
 * Spring value processor of field or method which has @Value.
 *
 * @author github.com/zhegexiaohuozi  seimimaster@gmail.com
 * @since 2017/12/20.
 */
public class SpringValueProcessor extends ApolloBeanPostProcessor implements EnvironmentAware {

  private Pattern pattern = Pattern.compile("\\$\\{(.*?)(:(.*?))?}");
  private static Multimap<String, SpringValue> monitor = LinkedListMultimap.create();
  private Environment environment;
  private Logger logger = LoggerFactory.getLogger(SpringValueProcessor.class);

  public static Multimap<String, SpringValue> monitor() {
    return monitor;
  }


  @Override
  protected void processField(Object bean, Field field) {
    DisableAutoUpdate disableAutoUpdate = AnnotationUtils
        .getAnnotation(field, DisableAutoUpdate.class);
    if (disableAutoUpdate != null) {
      return;
    }
    Value value = field.getAnnotation(Value.class);
    if (value == null) {
      return;
    }
    Matcher matcher = pattern.matcher(value.value());
    Preconditions.checkArgument(matcher.matches(),
        String.format("the value annotation for field:%s is not correct," +
            "please use ${somekey} or ${someKey:someDefaultValue} pattern", field.getType()));
    String key = matcher.group(1);
    monitor.put(key, SpringFieldValue.create(key, bean, field));
    logger.info("Listening apollo key = {}", key);
  }

  @Override
  protected void processMethod(final Object bean, Method method) {
    DisableAutoUpdate disableAutoUpdate = AnnotationUtils
        .getAnnotation(method, DisableAutoUpdate.class);
    if (disableAutoUpdate != null) {
      return;
    }
    Value value = method.getAnnotation(Value.class);
    if (value == null) {
      return;
    }
    Matcher matcher = pattern.matcher(value.value());
    Preconditions.checkArgument(matcher.matches(),
        String.format("the apollo value annotation for field:%s is not correct," +
            "please use ${somekey} or ${someKey:someDefaultValue} pattern", method.getName()));
    String key = matcher.group(1);
    monitor.put(key, SpringMethodValue.create(key, bean, method));
    logger.info("Listening apollo key = {}", key);
  }

  @Override
  public void setEnvironment(Environment env) {
    this.environment = env;
    PropertySourcesProcessor.registerListener(new ConfigChangeListener() {
      @Override
      public void onChange(ConfigChangeEvent changeEvent) {
        Set<String> keys = changeEvent.changedKeys();
        if (CollectionUtils.isEmpty(keys)) {
          return;
        }
        for (String k : keys) {
          ConfigChange configChange = changeEvent.getChange(k);
          if (!Objects.equals(environment.getProperty(k), configChange.getNewValue())) {
            continue;
          }
          Collection<SpringValue> targetValues = SpringValueProcessor.monitor().get(k);
          if (targetValues == null || targetValues.isEmpty()) {
            continue;
          }
          for (SpringValue val : targetValues) {
            val.updateVal(environment.getProperty(k));
          }
        }
      }
    });
  }
}
