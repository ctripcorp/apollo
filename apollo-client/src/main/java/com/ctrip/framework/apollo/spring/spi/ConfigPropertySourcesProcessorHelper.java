package com.ctrip.framework.apollo.spring.spi;

import com.ctrip.framework.apollo.core.spi.Ordered;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * 配置属性源处理帮助器
 */
public interface ConfigPropertySourcesProcessorHelper extends Ordered {

  /**
   * 注册BeanDefinition处理
   *
   * @param registry BeanDefinition注册器
   * @throws BeansException
   */
  void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;
}
