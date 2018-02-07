package com.ctrip.framework.apollo.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ctrip.framework.apollo.internals.ConfigRepository;
import com.ctrip.framework.apollo.internals.DefaultConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloValue;
import com.ctrip.framework.apollo.spring.annotation.ApolloValueProcessor;
import com.ctrip.framework.apollo.spring.annotation.SpringValueProcessor;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;

import java.util.List;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class JavaConfigPlaceholderTest extends AbstractSpringIntegrationTest {
  private static final String TIMEOUT_PROPERTY = "timeout";
  private static final int DEFAULT_TIMEOUT = 100;
  private static final String BATCH_PROPERTY = "batch";
  private static final int DEFAULT_BATCH = 200;
  private static final String FX_APOLLO_NAMESPACE = "FX.apollo";
  private static final String JSON_PROPERTY = "jsonProperty";

  @Test
  public void testPropertySourceWithNoNamespace() throws Exception {
    int someTimeout = 1000;
    int someBatch = 2000;

    Config config = mock(Config.class);
    when(config.getProperty(eq(TIMEOUT_PROPERTY), anyString())).thenReturn(String.valueOf(someTimeout));
    when(config.getProperty(eq(BATCH_PROPERTY), anyString())).thenReturn(String.valueOf(someBatch));

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, config);

    check(someTimeout, someBatch, AppConfig1.class);
  }

  @Test
  public void testPropertySourceWithNoConfig() throws Exception {
    Config config = mock(Config.class);
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, config);
    check(DEFAULT_TIMEOUT, DEFAULT_BATCH, AppConfig1.class);
  }

  @Test
  public void testApplicationPropertySource() throws Exception {
    int someTimeout = 1000;
    int someBatch = 2000;

    Config config = mock(Config.class);
    when(config.getProperty(eq(TIMEOUT_PROPERTY), anyString())).thenReturn(String.valueOf(someTimeout));
    when(config.getProperty(eq(BATCH_PROPERTY), anyString())).thenReturn(String.valueOf(someBatch));

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, config);

    check(someTimeout, someBatch, AppConfig2.class);
  }

  @Test
  public void testMultiplePropertySources() throws Exception {
    int someTimeout = 1000;
    int someBatch = 2000;

    Config application = mock(Config.class);
    when(application.getProperty(eq(TIMEOUT_PROPERTY), anyString())).thenReturn(String.valueOf(someTimeout));
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, application);

    Config fxApollo = mock(Config.class);
    when(application.getProperty(eq(BATCH_PROPERTY), anyString())).thenReturn(String.valueOf(someBatch));
    mockConfig(FX_APOLLO_NAMESPACE, fxApollo);

    check(someTimeout, someBatch, AppConfig3.class);
  }

  @Test
  public void testMultiplePropertySourcesWithSameProperties() throws Exception {
    int someTimeout = 1000;
    int anotherTimeout = someTimeout + 1;
    int someBatch = 2000;

    Config application = mock(Config.class);
    when(application.getProperty(eq(TIMEOUT_PROPERTY), anyString())).thenReturn(String.valueOf(someTimeout));
    when(application.getProperty(eq(BATCH_PROPERTY), anyString())).thenReturn(String.valueOf(someBatch));
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, application);

    Config fxApollo = mock(Config.class);
    when(fxApollo.getProperty(eq(TIMEOUT_PROPERTY), anyString())).thenReturn(String.valueOf(anotherTimeout));
    mockConfig(FX_APOLLO_NAMESPACE, fxApollo);

    check(someTimeout, someBatch, AppConfig3.class);
  }

  @Test
  public void testMultiplePropertySourcesWithSamePropertiesWithWeight() throws Exception {
    int someTimeout = 1000;
    int anotherTimeout = someTimeout + 1;
    int someBatch = 2000;

    Config application = mock(Config.class);
    when(application.getProperty(eq(TIMEOUT_PROPERTY), anyString())).thenReturn(String.valueOf(someTimeout));
    when(application.getProperty(eq(BATCH_PROPERTY), anyString())).thenReturn(String.valueOf(someBatch));
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, application);

    Config fxApollo = mock(Config.class);
    when(fxApollo.getProperty(eq(TIMEOUT_PROPERTY), anyString())).thenReturn(String.valueOf(anotherTimeout));
    mockConfig(FX_APOLLO_NAMESPACE, fxApollo);

    check(anotherTimeout, someBatch, AppConfig2.class, AppConfig4.class);
  }

  @Test
  public void testApplicationPropertySourceWithValueInjectedAsParameter() throws Exception {
    int someTimeout = 1000;
    int someBatch = 2000;

    Config config = mock(Config.class);
    when(config.getProperty(eq(TIMEOUT_PROPERTY), anyString())).thenReturn(String.valueOf(someTimeout));
    when(config.getProperty(eq(BATCH_PROPERTY), anyString())).thenReturn(String.valueOf(someBatch));

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, config);

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig5.class);

    TestJavaConfigBean2 bean = context.getBean(TestJavaConfigBean2.class);

    assertEquals(someTimeout, bean.getTimeout());
    assertEquals(someBatch, bean.getBatch());
  }

  @Test
  public void testJsonDeserialization(){
    String someJson = "[{\"a\":\"astring\", \"b\":10},{\"a\":\"astring2\", \"b\":20}]";

    Config config = mock(Config.class);
    when(config.getProperty(eq(JSON_PROPERTY),anyString())).thenReturn(String.valueOf(someJson));

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, config);

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig6.class);

    TestJavaConfigBean3 testJavaConfigBean3 = context.getBean(TestJavaConfigBean3.class);
    assertEquals(2, testJavaConfigBean3.getJsonBeanList().size());
    assertEquals("astring", testJavaConfigBean3.getJsonBeanList().get(0).a);
  }

  @Test
  public void testSpringValueAutoUpdate() throws InterruptedException {
    SpringValueProcessor.setAutoUpdate(true);

    String timeoutKey = "timeout";
    String timeoutValue = "500";
    Properties someProperties = new Properties();
    someProperties.putAll(ImmutableMap.of(timeoutKey, timeoutValue));
    ConfigRepository configRepository = mock(ConfigRepository.class);
    when(configRepository.getConfig()).thenReturn(someProperties);
    Config config = new DefaultConfig(ConfigConsts.NAMESPACE_APPLICATION, configRepository);
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, config);
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig1.class);

    TestJavaConfigBean testJavaConfigBean = context.getBean(TestJavaConfigBean.class);
    assertEquals(500, testJavaConfigBean.getTimeout());

    String timeoutNewValue = "1000";
    Properties newProperties = new Properties();
    newProperties.putAll(ImmutableMap.of(timeoutKey, timeoutNewValue));
    ((DefaultConfig)config).onRepositoryChange(ConfigConsts.NAMESPACE_APPLICATION, newProperties);

    Thread.sleep(500);//更新是在异步的线程中
    assertEquals(1000, testJavaConfigBean.getTimeout());

  }

  @Test
  public void testApolloValueAutoUpdate() throws InterruptedException {
    ApolloValueProcessor.setAutoUpdate(true);

    String jsonPropertyKey = "jsonProperty";
    String jsonPropertyValue = "[{\"a\":\"astring\", \"b\":10},{\"a\":\"astring2\", \"b\":20}]";
    Properties someProperties = new Properties();
    someProperties.putAll(ImmutableMap.of(jsonPropertyKey, jsonPropertyValue));
    ConfigRepository configRepository = mock(ConfigRepository.class);
    when(configRepository.getConfig()).thenReturn(someProperties);
    Config config = new DefaultConfig(ConfigConsts.NAMESPACE_APPLICATION, configRepository);
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, config);
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig6.class);

    TestJavaConfigBean3 testJavaConfigBean = context.getBean(TestJavaConfigBean3.class);
    assertEquals("astring", testJavaConfigBean.getJsonBeanList().get(0).a);

    String jsonPropertyNewValue = "[{\"a\":\"newString\", \"b\":10},{\"a\":\"astring2\", \"b\":20}]";
    Properties newProperties = new Properties();
    newProperties.putAll(ImmutableMap.of(jsonPropertyKey, jsonPropertyNewValue));
    ((DefaultConfig)config).onRepositoryChange(ConfigConsts.NAMESPACE_APPLICATION, newProperties);

    Thread.sleep(500);//更新是在异步的线程中
    assertEquals("newString", testJavaConfigBean.getJsonBeanList().get(0).a);

  }


  private void check(int expectedTimeout, int expectedBatch, Class<?>... annotatedClasses) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(annotatedClasses);

    TestJavaConfigBean bean = context.getBean(TestJavaConfigBean.class);

    assertEquals(expectedTimeout, bean.getTimeout());
    assertEquals(expectedBatch, bean.getBatch());
  }

  @Configuration
  @EnableApolloConfig
  static class AppConfig1 {
    @Bean
    TestJavaConfigBean testJavaConfigBean() {
      return new TestJavaConfigBean();
    }
  }

  @Configuration
  @EnableApolloConfig("application")
  static class AppConfig2 {
    @Bean
    TestJavaConfigBean testJavaConfigBean() {
      return new TestJavaConfigBean();
    }
  }

  @Configuration
  @EnableApolloConfig({"application", "FX.apollo"})
  static class AppConfig3 {
    @Bean
    TestJavaConfigBean testJavaConfigBean() {
      return new TestJavaConfigBean();
    }
  }

  @Configuration
  @EnableApolloConfig(value = "FX.apollo", order = 10)
  static class AppConfig4 {
  }

  @Configuration
  @EnableApolloConfig
  static class AppConfig5 {
    @Bean
    TestJavaConfigBean2 testJavaConfigBean2(@Value("${timeout:100}") int timeout, @Value("${batch:200}") int batch) {
      TestJavaConfigBean2 bean = new TestJavaConfigBean2();

      bean.setTimeout(timeout);
      bean.setBatch(batch);

      return bean;
    }
  }

  @Configuration
  @EnableApolloConfig
  static class AppConfig6 {
    @Bean
    TestJavaConfigBean3 testJavaConfigBean3() {
      return new TestJavaConfigBean3();
    }
  }

  @Component
  static class TestJavaConfigBean {
    @Value("${timeout:100}")
    private int timeout;
    private int batch;

    @Value("${batch:200}")
    public void setBatch(int batch) {
      this.batch = batch;
    }

    public int getTimeout() {
      return timeout;
    }

    public int getBatch() {
      return batch;
    }
  }

  static class TestJavaConfigBean2 {
    private int timeout;
    private int batch;

    public int getTimeout() {
      return timeout;
    }

    public void setTimeout(int timeout) {
      this.timeout = timeout;
    }

    public int getBatch() {
      return batch;
    }

    public void setBatch(int batch) {
      this.batch = batch;
    }
  }

  static class TestJavaConfigBean3{

    @ApolloValue("${jsonProperty}")
    private List<JsonBean> jsonBeanList;

    public List<JsonBean> getJsonBeanList() {
      return jsonBeanList;
    }

    public void setJsonBeanList(List<JsonBean> jsonBeanList) {
      this.jsonBeanList = jsonBeanList;
    }

  }

  static class JsonBean{
    String a;
    int b;

    public String getA() {
      return a;
    }

    public void setA(String a) {
      this.a = a;
    }

    public int getB() {
      return b;
    }

    public void setB(int b) {
      this.b = b;
    }
  }
}
