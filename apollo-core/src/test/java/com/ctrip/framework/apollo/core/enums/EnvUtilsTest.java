package com.ctrip.framework.apollo.core.enums;

import static org.junit.Assert.*;

import org.junit.Test;

public class EnvUtilsTest {

  @Test
  public void testTransformEnv() throws Exception {
    assertEquals(Env.DEV, EnvUtils.transformEnv(Env.DEV.name()));
    assertEquals(Env.FAT, EnvUtils.transformEnv(Env.FAT.name().toLowerCase()));
    assertEquals(Env.UAT, EnvUtils.transformEnv(" " + Env.UAT.name().toUpperCase() + ""));
    assertEquals(Env.UNKNOWN, EnvUtils.transformEnv("someInvalidEnv"));
  }

  @Test
  public void testFromString() throws Exception {
    assertEquals(Env.DEV, Env.fromString(Env.DEV.name()));
    assertEquals(Env.FAT, Env.fromString(Env.FAT.name().toLowerCase()));
    assertEquals(Env.UAT, Env.fromString(" " + Env.UAT.name().toUpperCase() + ""));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromInvalidString() throws Exception {
    Env.fromString("someInvalidEnv");
  }

  @Test
  public void getWellFormName() {
    assertEquals("ABC", EnvUtils.getWellFormName("aBc"));
    assertEquals("A1C", EnvUtils.getWellFormName("   a1C "));
    assertEquals("VVBC", EnvUtils.getWellFormName(" VvBc"));
  }

}
