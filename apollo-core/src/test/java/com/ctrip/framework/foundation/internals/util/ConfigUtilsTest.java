package com.ctrip.framework.foundation.internals.util;

import static org.junit.Assert.*;

import static com.ctrip.framework.foundation.internals.util.ConfigUtilsTest.SystemPropertyKeys.KEY;
import static com.ctrip.framework.foundation.internals.util.ConfigUtilsTest.SystemPropertyValues.CUSTOM_VALUE;

import org.junit.After;
import org.junit.Test;

public class ConfigUtilsTest {

  @After
  public void tearDown() {
    System.clearProperty(KEY);
  }

  @Test
  public void testGetValue_null() {
    assertNull(ConfigUtils.getValue(KEY, "nothing"));
  }

  @Test
  public void testGetValue_custom() {
    System.setProperty(KEY, CUSTOM_VALUE);
    String value = ConfigUtils.getValue(KEY, "nothing");
    assertEquals(CUSTOM_VALUE, value);
  }

  protected static class SystemPropertyKeys {

    static final String KEY = "system.property.key.2020.11.28";
  }

  protected static class SystemPropertyValues {

    static final String CUSTOM_VALUE = "/simple/custom/path";
  }
}
