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
 * Create by zhangzheng on 2018/2/9
 */
public abstract class ApolloBeanPostProcessor implements BeanPostProcessor, PriorityOrdered{

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    Class clazz = bean.getClass();
    for(Field field:findAllField(clazz)){
      processField(bean, field);
    }
    for(Method method:findAllMethod(clazz)){
      processMethod(bean, method);
    }
    return bean;
  }

  /**
   * implemented by subclass to handle class field
   * @param bean
   * @param field
   */
  protected void processField(Object bean, Field field){}

  /**
   * implemented by subclass the handle class method
   * @param bean
   * @param method
   */
  protected void processMethod(Object bean, Method method){}

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
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
}
