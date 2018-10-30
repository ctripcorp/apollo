package com.ctrip.framework.apollo.spring;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionStoreException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.core.ConfigConsts;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class XmlConfigPlaceholderTest extends AbstractSpringIntegrationTest {
  private static final String TIMEOUT_PROPERTY = "timeout";
  private static final int DEFAULT_TIMEOUT = 100;
  private static final String BATCH_PROPERTY = "batch";
  private static final int DEFAULT_BATCH = 200;
  private static final String FX_APOLLO_NAMESPACE = "FX.apollo";

  @Test
  public void testPropertySourceWithNoNamespace() throws Exception {
    int someTimeout = 1000;
    int someBatch = 2000;

    Config config = mock(Config.class);
    when(config.getProperty(eq(TIMEOUT_PROPERTY), anyString())).thenReturn(String.valueOf(someTimeout));
    when(config.getProperty(eq(BATCH_PROPERTY), anyString())).thenReturn(String.valueOf(someBatch));

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, config);

    check("spring/XmlConfigPlaceholderTest1.xml", someTimeout, someBatch);
  }

  @Test
  public void testPropertySourceWithNoConfig() throws Exception {
    Config config = mock(Config.class);
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, config);
    check("spring/XmlConfigPlaceholderTest1.xml", DEFAULT_TIMEOUT, DEFAULT_BATCH);
  }

  @Test
  public void testApplicationPropertySource() throws Exception {
    int someTimeout = 1000;
    int someBatch = 2000;

    Config config = mock(Config.class);
    when(config.getProperty(eq(TIMEOUT_PROPERTY), anyString())).thenReturn(String.valueOf(someTimeout));
    when(config.getProperty(eq(BATCH_PROPERTY), anyString())).thenReturn(String.valueOf(someBatch));

    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, config);

    check("spring/XmlConfigPlaceholderTest2.xml", someTimeout, someBatch);
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

    check("spring/XmlConfigPlaceholderTest3.xml", someTimeout, someBatch);
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

    check("spring/XmlConfigPlaceholderTest3.xml", someTimeout, someBatch);
  }

  @Test
  public void testMultiplePropertySourcesWithSameProperties2() throws Exception {
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

    check("spring/XmlConfigPlaceholderTest6.xml", anotherTimeout, someBatch);
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

    check("spring/XmlConfigPlaceholderTest4.xml", anotherTimeout, someBatch);
  }

  @Test(expected = XmlBeanDefinitionStoreException.class)
  public void testWithInvalidWeight() throws Exception {
    check("spring/XmlConfigPlaceholderTest5.xml", DEFAULT_TIMEOUT, DEFAULT_BATCH);
  }

  private void check(String xmlLocation, int expectedTimeout, int expectedBatch) {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(xmlLocation);

    TestXmlBean bean = context.getBean(TestXmlBean.class);

    assertEquals(expectedTimeout, bean.getTimeout());
    assertEquals(expectedBatch, bean.getBatch());
  }

  public static class TestXmlBean {
    private int timeout;
    private int batch;

    public void setTimeout(int timeout) {
      this.timeout = timeout;
    }

    public int getTimeout() {
      return timeout;
    }

    public int getBatch() {
      return batch;
    }

    public void setBatch(int batch) {
      this.batch = batch;
    }
  }
}
