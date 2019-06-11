package com.ctrip.framework.apollo.spring.spi;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class TestProcessorHelper extends DefaultConfigPropertySourcesProcessorHelper {

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {
    super.postProcessBeanDefinitionRegistry(registry);
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
