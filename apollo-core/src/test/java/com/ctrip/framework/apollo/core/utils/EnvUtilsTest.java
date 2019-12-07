package com.ctrip.framework.apollo.core.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvUtilsTest {

  @Test
  public void getWellFormName() {
    assertEquals("ABC", EnvUtils.getWellFormName("aBc"));
    assertEquals("A1C", EnvUtils.getWellFormName("   a1C "));
    assertEquals("VVBC", EnvUtils.getWellFormName(" VvBc"));
  }
}
