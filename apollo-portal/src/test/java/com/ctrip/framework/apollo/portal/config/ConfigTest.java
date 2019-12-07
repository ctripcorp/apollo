package com.ctrip.framework.apollo.portal.config;


import com.ctrip.framework.apollo.portal.AbstractUnitTest;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.mockito.Mockito.when;

public class ConfigTest extends AbstractUnitTest{

  @Mock
  private ConfigurableEnvironment environment;
  @InjectMocks
  private PortalConfig config;


  @Test
  public void testGetNotExistValue() {
    String testKey = "key";
    String testDefaultValue = "value";

    when(environment.getProperty(testKey, testDefaultValue)).thenReturn(testDefaultValue);

    Assert.assertEquals(testDefaultValue, config.getValue(testKey, testDefaultValue));
  }

  @Test
  public void testGetArrayProperty() {
    String testKey = "key";
    String testValue = "a,b,c";

    when(environment.getProperty(testKey)).thenReturn(testValue);

    String[] result = config.getArrayProperty(testKey, null);

    Assert.assertEquals(3, result.length);
    Assert.assertEquals("a", result[0]);
    Assert.assertEquals("b", result[1]);
    Assert.assertEquals("c", result[2]);
  }

  @Test
  public void testGetBooleanProperty() {
    String testKey = "key";
    String testValue = "true";

    when(environment.getProperty(testKey)).thenReturn(testValue);

    boolean result = config.getBooleanProperty(testKey, false);

    Assert.assertTrue(result);
  }

  @Test
  public void testGetIntProperty() {
    String testKey = "key";
    String testValue = "1024";

    when(environment.getProperty(testKey)).thenReturn(testValue);

    int result = config.getIntProperty(testKey, 0);

    Assert.assertEquals(1024, result);

  }


}
