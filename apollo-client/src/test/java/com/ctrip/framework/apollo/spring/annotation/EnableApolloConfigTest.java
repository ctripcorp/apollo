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
    new AnnotationConfigApplicationContext(
        TestResolveExpressionWithDefaultValueConfiguration.class);
  }

  @Test
  public void testResolveExpressionFromSystemProperty() {
    mockConfig("application", mock(Config.class));

    final String resolvedNamespaceName = "yyy";
    System.setProperty("from.system.property", resolvedNamespaceName);
    mockConfig(resolvedNamespaceName, mock(Config.class));
    new AnnotationConfigApplicationContext(
        TestResolveExpressionFromSystemPropertyConfiguration.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnresolvable() {
    new AnnotationConfigApplicationContext(TestUnresolvableConfiguration.class);
  }

  @Configuration
  @EnableApolloConfig(value = {ConfigConsts.NAMESPACE_APPLICATION, "${simple.namespace:xxx}"})
  static class TestResolveExpressionWithDefaultValueConfiguration {

  }

  @Configuration
  @EnableApolloConfig(value = {ConfigConsts.NAMESPACE_APPLICATION, "${from.system.property}"})
  static class TestResolveExpressionFromSystemPropertyConfiguration {

  }

  @Configuration
  @EnableApolloConfig(value = "${unresolvable.property}")
  static class TestUnresolvableConfiguration {

  }

}
