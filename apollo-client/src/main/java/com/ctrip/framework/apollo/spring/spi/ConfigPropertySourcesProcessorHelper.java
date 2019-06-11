package com.ctrip.framework.apollo.spring.spi;

import com.ctrip.framework.apollo.core.spi.Ordered;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public interface ConfigPropertySourcesProcessorHelper extends Ordered {

  void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;
}
