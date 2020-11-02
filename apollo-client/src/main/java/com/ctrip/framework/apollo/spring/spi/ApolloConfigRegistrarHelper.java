package com.ctrip.framework.apollo.spring.spi;

import com.ctrip.framework.apollo.core.spi.Ordered;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;

public interface ApolloConfigRegistrarHelper extends Ordered, BeanFactoryAware {

  void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry);
}
