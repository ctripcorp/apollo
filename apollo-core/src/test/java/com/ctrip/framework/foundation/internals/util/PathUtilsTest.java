package com.ctrip.framework.foundation.internals.util;

import static com.ctrip.framework.foundation.internals.util.PathUtilsTest.SystemPropertyKeys.PATH_KEY;
import static com.ctrip.framework.foundation.internals.util.PathUtilsTest.SystemPropertyValues.CUSTOM_VALUE;
import static com.ctrip.framework.foundation.internals.util.PathUtilsTest.SystemPropertyValues.DEFAULT_VALUE;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Test;

public class PathUtilsTest {

  @After
  public void tearDown() {
    System.clearProperty(PATH_KEY);
  }

  @Test
  public void testResolveWithDefaultValue() {
    Path path = PathUtils.resolve(PATH_KEY, "nothing", DEFAULT_VALUE, DEFAULT_VALUE);
    assertEquals(Paths.get(DEFAULT_VALUE), path);
  }

  @Test
  public void testResolveWithCustomValue() {
    System.setProperty(PATH_KEY, CUSTOM_VALUE);
    Path path = PathUtils.resolve(PATH_KEY, "nothing", DEFAULT_VALUE, DEFAULT_VALUE);
    assertEquals(Paths.get(CUSTOM_VALUE), path);
  }

  protected static class SystemPropertyKeys {
    static final String PATH_KEY = "system.property.key.2020.11.28";
  }

  protected static class SystemPropertyValues {
    static final String CUSTOM_VALUE = "/simple/custom/path";
    static final String DEFAULT_VALUE = "/simple/default/path";
  }
}