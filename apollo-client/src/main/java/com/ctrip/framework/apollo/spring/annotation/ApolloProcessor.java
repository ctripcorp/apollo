package com.ctrip.framework.apollo.spring.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

/**
 * Apollo 处理器抽象类,封装了在 Spring Bean 初始化之前，处理属性和方法。但是具体的处理，是两个抽象方法
 *
 * @author zhangzheng on 2018/2/6cc
 */
public abstract class ApolloProcessor implements BeanPostProcessor, PriorityOrdered {

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    Class clazz = bean.getClass();
    // 处理所有 Field
    for (Field field : findAllField(clazz)) {
      processField(bean, beanName, field);
    }
    // 处理所有的 Method
    for (Method method : findAllMethod(clazz)) {
      processMethod(bean, beanName, method);
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  /**
   * 处理字段，子类应该实现这个方法
   */
  protected abstract void processField(Object bean, String beanName, Field field);

  /**
   * 处理方法，子类应实现此方法
   */
  protected abstract void processMethod(Object bean, String beanName, Method method);


  @Override
  public int getOrder() {
    // 最低优先级
    return Ordered.LOWEST_PRECEDENCE;
  }

  /**
   * 获取指定类的所有字段
   *
   * @param clazz class对象
   * @return 指定类的所有字段
   */
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

  /**
   * 获取指定类的所有方法
   *
   * @param clazz class对象
   * @return 指定类的所有方法
   */
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
}
