package com.ctrip.framework.apollo.spring.property;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;

/**
 * Spring @Value method info
 *
 * @author github.com/zhegexiaohuozi  seimimaster@gmail.com
 * @since 2018/2/6.
 */
public class SpringValue {

  private static final Logger logger = LoggerFactory
      .getLogger(SpringValue.class);

  private MethodParameter methodParameter;
  private Field field;
  private WeakReference<Object> beanRef;
  private String beanName;
  private String placeholder;
  private Class<?> targetType;
  private Type genericType;
  private boolean isJson;

  //record lastEmbeddedValue to check whether change
  private volatile Object lastEmbeddedValue;


  public SpringValue(String placeholder, Object bean, String beanName,
      Field field, boolean isJson, Object embeddedValue) {
    this.beanRef = new WeakReference<>(bean);
    this.beanName = beanName;
    this.field = field;
    this.placeholder = placeholder;
    this.targetType = field.getType();
    this.isJson = isJson;
    if (isJson) {
      this.genericType = field.getGenericType();
    }
    this.lastEmbeddedValue = embeddedValue;
  }

  public SpringValue(String placeholder, Object bean, String beanName,
      Method method, boolean isJson, Object embeddedValue) {
    this.beanRef = new WeakReference<>(bean);
    this.beanName = beanName;
    this.methodParameter = new MethodParameter(method, 0);
    this.placeholder = placeholder;
    Class<?>[] paramTps = method.getParameterTypes();
    this.targetType = paramTps[0];
    this.isJson = isJson;
    if (isJson) {
      this.genericType = method.getGenericParameterTypes()[0];
    }
    this.lastEmbeddedValue = embeddedValue;
  }

  /**
   * 是否有必要更新，会判断newOriginalPropertyVal是否和lastOriginalPropertyValue相等
   * <br> false:不更新
   * <br> true:更新成功
   *
   * @param newVal 新值
   * @param newResolveEmbeddedValue 未解析EL表达式的新原始值
   */
  public synchronized boolean updateIfNecessary(Object newVal
      , Object newResolveEmbeddedValue)
      throws IllegalAccessException, InvocationTargetException {
    if (!shouldUpdate(newResolveEmbeddedValue)) {
      logger.debug("not update newResolveEmbeddedValue:[{}]", newResolveEmbeddedValue);
      return false;
    }
    this.lastEmbeddedValue = newResolveEmbeddedValue;
    if (isField()) {
      injectField(newVal);
    } else {
      injectMethod(newVal);
    }
    return true;
  }

  /**
   * @param newResolveEmbeddedValue 未解析EL表达式的新原始值
   * @return true:新旧值不同，更新；false：新旧值相同，不更新
   */
  public boolean shouldUpdate(Object newResolveEmbeddedValue) {
    //比较原始值
    return !Objects.deepEquals(this.lastEmbeddedValue, newResolveEmbeddedValue);
  }

  private void injectField(Object newVal) throws IllegalAccessException {
    Object bean = beanRef.get();
    if (bean == null) {
      return;
    }
    boolean accessible = field.isAccessible();
    field.setAccessible(true);
    field.set(bean, newVal);
    field.setAccessible(accessible);
  }

  private void injectMethod(Object newVal)
      throws InvocationTargetException, IllegalAccessException {
    Object bean = beanRef.get();
    if (bean == null) {
      return;
    }
    methodParameter.getMethod().invoke(bean, newVal);
  }

  public String getBeanName() {
    return beanName;
  }

  public Class<?> getTargetType() {
    return targetType;
  }

  public String getPlaceholder() {
    return this.placeholder;
  }

  public MethodParameter getMethodParameter() {
    return methodParameter;
  }

  public boolean isField() {
    return this.field != null;
  }

  public Field getField() {
    return field;
  }

  public Type getGenericType() {
    return genericType;
  }

  public boolean isJson() {
    return isJson;
  }

  boolean isTargetBeanValid() {
    return beanRef.get() != null;
  }

  @Override
  public String toString() {
    Object bean = beanRef.get();
    if (bean == null) {
      return "";
    }
    if (isField()) {
      return String
          .format("key: %s, beanName: %s, field: %s.%s", placeholder, beanName,
              bean.getClass().getName(),
              field.getName());
    }
    return String
        .format("key: %s, beanName: %s, method: %s.%s", placeholder, beanName,
            bean.getClass().getName(),
            methodParameter.getMethod().getName());
  }
}
