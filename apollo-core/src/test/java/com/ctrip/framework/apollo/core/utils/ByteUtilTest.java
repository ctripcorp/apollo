package com.ctrip.framework.apollo.core.utils;

import com.ctrip.framework.apollo.core.utils.ByteUtil;
import org.junit.Assert;
import org.junit.Test;

public class ByteUtilTest {

  @Test
  public void testInt3() {
    Assert.assertEquals((byte)0, ByteUtil.int3(0));
    Assert.assertEquals((byte)0, ByteUtil.int3(1));
  }

  @Test
  public void testInt2() {
    Assert.assertEquals((byte)0, ByteUtil.int2(0));
    Assert.assertEquals((byte)0, ByteUtil.int2(1));
  }

  @Test
  public void testInt1() {
    Assert.assertEquals((byte)0, ByteUtil.int1(0));
    Assert.assertEquals((byte)0, ByteUtil.int1(1));
  }

  @Test
  public void testInt0() {
    Assert.assertEquals((byte)0, ByteUtil.int0(0));
    Assert.assertEquals((byte)1, ByteUtil.int0(1));
  }

  @Test
  public void testToHexString() {
    Assert.assertEquals("", ByteUtil.toHexString(new byte[] {}));
    Assert.assertEquals("98", ByteUtil.toHexString(new byte[] {(byte)-104}));
  }
}
