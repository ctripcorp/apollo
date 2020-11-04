package com.ctrip.framework.apollo.spring.annotation;

import static org.mockito.Mockito.mock;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.spring.AbstractSpringIntegrationTest;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

public class ApolloConfigTest extends AbstractSpringIntegrationTest {

  @Test
  public void testResolveExpressionSimple() {
    mockConfig("application", mock(Config.class));
    mockConfig("xxx", mock(Config.class));
    mockConfig("yyy", mock(Config.class));
    new AnnotationConfigApplicationContext(TestResolveExpressionSimpleConfiguration.class);
  }

  @Configuration
  @EnableApolloConfig(value = {"xxx", "yyy"})
  protected static class TestResolveExpressionSimpleConfiguration {

    @ApolloConfig(value = "${simple.namespace:xxx}")
    private Config xxx;

    @ApolloConfig(value = "${simple.namespace:yyy}")
    private Config yyy;
  }
}
