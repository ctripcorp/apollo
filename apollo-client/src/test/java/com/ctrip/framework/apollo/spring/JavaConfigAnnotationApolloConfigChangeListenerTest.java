package com.ctrip.framework.apollo.spring;

import static org.mockito.Mockito.mock;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import java.util.Properties;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

public class JavaConfigAnnotationApolloConfigChangeListenerTest extends
    AbstractSpringIntegrationTest {

  @Test
  public void testResolveExpressionSimple() {
    Config applicationConfig = mock(Config.class);
    mockConfig("application", applicationConfig);
    new AnnotationConfigApplicationContext(TestResolveExpressionSimpleConfiguration.class);
  }

  /**
   * resolve namespace's name from system property.
   */
  @Test
  public void testResolveExpressionFromSystemProperty() {
    Config applicationConfig = mock(Config.class);
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);

    final String namespaceName = "magicRedis";
    System.setProperty("redis.namespace", namespaceName);
    Config redisConfig = mock(Config.class);
    mockConfig(namespaceName, redisConfig);
    new AnnotationConfigApplicationContext(
        TestResolveExpressionFromSystemPropertyConfiguration.class);
  }

  /**
   * resolve namespace from config. ${mysql.namespace} will be resolved by config from namespace
   * application.
   */
  @Test
  public void testResolveExpressionFromApplicationNamespace() {
    Config applicationConfig = mock(Config.class);
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, applicationConfig);

    final String namespaceKey = "mysql.namespace";
    final String namespaceName = "magicMysqlNamespaceApplication";

    Properties properties = new Properties();
    properties.setProperty(namespaceKey, namespaceName);
    this.prepareConfig(ConfigConsts.NAMESPACE_APPLICATION, properties);

    Config mysqlConfig = mock(Config.class);
    mockConfig(namespaceName, mysqlConfig);

    new AnnotationConfigApplicationContext(
        TestResolveExpressionFromApplicationNamespaceConfiguration.class);
  }

  @Configuration
  @EnableApolloConfig
  static class TestResolveExpressionSimpleConfiguration {

    @ApolloConfigChangeListener("${simple.application:application}")
    private void onChange(ConfigChangeEvent event) {
    }
  }

  @Configuration
  @EnableApolloConfig
  static class TestResolveExpressionFromSystemPropertyConfiguration {

    @ApolloConfigChangeListener(value = {ConfigConsts.NAMESPACE_APPLICATION,
        "${redis.namespace}"})
    private void onChange(ConfigChangeEvent event) {
    }
  }

  @Configuration
  @EnableApolloConfig
  static class TestResolveExpressionFromApplicationNamespaceConfiguration {

    @ApolloConfigChangeListener(value = {ConfigConsts.NAMESPACE_APPLICATION,
        "${mysql.namespace}"})
    private void onChange(ConfigChangeEvent event) {
    }
  }
}
