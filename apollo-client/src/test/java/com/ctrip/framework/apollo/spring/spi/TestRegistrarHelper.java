package com.ctrip.framework.apollo.spring.spi;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;

public class TestRegistrarHelper extends DefaultApolloConfigRegistrarHelper {

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
      BeanDefinitionRegistry registry) {
    super.registerBeanDefinitions(importingClassMetadata, registry);
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
