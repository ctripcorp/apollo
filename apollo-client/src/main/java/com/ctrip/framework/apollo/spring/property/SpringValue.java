package com.ctrip.framework.apollo.spring.property;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.core.MethodParameter;

/**
 * Spring @Value方法信息
 *
 * @author github.com/zhegexiaohuozi  seimimaster@gmail.com
 * @since 2018/2/6.
 */
@NoArgsConstructor
public class SpringValue {

  /**
   * 方法参数
   */
  @Getter
  private MethodParameter methodParameter;
  /**
   * 方法字段
   */
  @Getter
  private Field field;
  /**
   * bean对象弱引用
   */
  private WeakReference<Object> beanRef;
  /**
   * bean名称
   */
  @Getter
  private String beanName;
  /**
   * key，即在Config中的属性key
   * <p>
   * 如：<property name="timeout" value="${timeout:200}"></property> ，key为"timeout"
   */
  private String key;
  /**
   * 占位符，
   * <p>
   * 如：<property name="timeout" value="${timeout:200}"></property> ，placeholder为"${timeout:200}"
   */
  @Getter
  private String placeholder;
  /**
   * 目标类型
   */
  @Getter
  private Class<?> targetType;
  /**
   * 泛型类型
   */
  @Getter
  private Type genericType;
  /**
   * 是否为JSON
   */
  @Getter
  private boolean isJson;

  /**
   * 构建对像
   *
   * @param key         属性key
   * @param placeholder 占位符
   * @param bean        bean对象
   * @param beanName    bean名称
   * @param field       字段参数
   * @param isJson      是否为json
   */
  public SpringValue(String key, String placeholder, Object bean, String beanName, Field field,
      boolean isJson) {
    this.beanRef = new WeakReference<>(bean);
    this.beanName = beanName;
    this.field = field;
    this.key = key;
    this.placeholder = placeholder;
    this.targetType = field.getType();
    this.isJson = isJson;
    if (isJson) {
      this.genericType = field.getGenericType();
    }
  }

  /**
   * 构建对像
   *
   * @param key         属性key
   * @param placeholder 占位符
   * @param bean        bean对象
   * @param beanName    bean名称
   * @param method      方法参数
   * @param isJson      是否为json
   */
  public SpringValue(String key, String placeholder, Object bean, String beanName, Method method,
      boolean isJson) {
    this.beanRef = new WeakReference<>(bean);
    this.beanName = beanName;
    this.methodParameter = new MethodParameter(method, 0);
    this.key = key;
    this.placeholder = placeholder;
    Class<?>[] paramTps = method.getParameterTypes();
    this.targetType = paramTps[0];
    this.isJson = isJson;
    if (isJson) {
      this.genericType = method.getGenericParameterTypes()[0];
    }
  }

  /**
   * 更新值
   *
   * @param newVal 新值
   * @throws InvocationTargetException 如果基础方法抛出异常，抛出
   * @throws IllegalAccessException    如果这个{@code Method}对象正在实施Java语言访问控制，并且底层方法不可访问,抛出
   */
  public void update(Object newVal) throws IllegalAccessException, InvocationTargetException {
    // 根据字段或方法来设置值
    if (isField()) {
      injectField(newVal);
    } else {
      injectMethod(newVal);
    }
  }

  /**
   * 注入方法
   *
   * @param newVal 新的值
   * @throws InvocationTargetException 如果基础方法抛出异常，抛出
   * @throws IllegalAccessException    如果这个{@code Method}对象正在实施Java语言访问控制，并且底层方法不可访问,抛出
   */
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

  /**
   * 注入方法
   *
   * @param newVal 新的值
   * @throws InvocationTargetException 如果基础方法抛出异常，抛出
   * @throws IllegalAccessException    如果这个{@code Method}对象正在实施Java语言访问控制，并且底层方法不可访问,抛出
   */
  private void injectMethod(Object newVal)
      throws InvocationTargetException, IllegalAccessException {
    // bean为空跳过
    Object bean = beanRef.get();
    if (bean == null) {
      return;
    }
    methodParameter.getMethod().invoke(bean, newVal);
  }

  /**
   * 是否存在方法字段
   *
   * @return true, 存在，否则，false
   */
  public boolean isField() {
    return this.field != null;
  }

  /**
   * 目标Bean是否有效
   *
   * @return true, 有效，否则，false
   */
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
          .format("key: %s, beanName: %s, field: %s.%s", key, beanName, bean.getClass().getName(),
              field.getName());
    }
    return String
        .format("key: %s, beanName: %s, method: %s.%s", key, beanName, bean.getClass().getName(),
            methodParameter.getMethod().getName());
  }
}
