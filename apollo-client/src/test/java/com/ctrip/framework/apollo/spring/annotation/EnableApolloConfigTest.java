package com.ctrip.framework.apollo.spring.annotation;

import static org.mockito.Mockito.mock;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.spring.AbstractSpringIntegrationTest;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

public class EnableApolloConfigTest extends AbstractSpringIntegrationTest {

  @Test
  public void testResolveExpressionSimple() {
    mockConfig("application", mock(Config.class));
    mockConfig("xxx", mock(Config.class));
    new AnnotationConfigApplicationContext(TestResolveExpressionSimpleConfiguration.class);
  }

  // TODO, cannot resolve "${simple.namespace:xxx}" in @EnableApolloConfig
  // because have no embeddedValueResolvers in ConfigurableBeanFactory when use ImportBeanDefinitionRegistrar
  @Configuration
  @EnableApolloConfig(value = {ConfigConsts.NAMESPACE_APPLICATION, "${simple.namespace:xxx}"})
  static class TestResolveExpressionSimpleConfiguration {

  }


}
