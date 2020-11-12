package com.ctrip.framework.apollo.core.utils;


import java.util.ArrayList;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

  @Test
  public void testEqualsIgnoreCase() {
    Assert.assertFalse(org.apache.commons.lang.StringUtils.equalsIgnoreCase(",", "foo"));
    Assert.assertFalse(org.apache.commons.lang.StringUtils.equalsIgnoreCase(null, "??"));
    Assert.assertTrue(org.apache.commons.lang.StringUtils.equalsIgnoreCase(null, null));
    Assert.assertTrue(org.apache.commons.lang.StringUtils.equalsIgnoreCase("foo", "Foo"));
  }

  @Test
  public void testEquals() {
    Assert.assertFalse(Objects.equals(null, ""));
    Assert.assertTrue(Objects.equals(null, null));
    Assert.assertTrue(Objects.equals("3", "3"));
  }

  @Test
  public void testIsBlank() {
    Assert.assertFalse(org.apache.commons.lang.StringUtils.isBlank("\'"));
    Assert.assertTrue(org.apache.commons.lang.StringUtils.isBlank(""));
    Assert.assertTrue(org.apache.commons.lang.StringUtils.isBlank(null));
  }

  @Test
  public void testIsContainEmpty() {
    Assert.assertFalse(StringUtils.isContainEmpty(null));
    Assert.assertFalse(StringUtils.isContainEmpty(new String[]{}));
    Assert.assertFalse(StringUtils.isContainEmpty(new String[]{"1"}));
    Assert.assertTrue(StringUtils.isContainEmpty(new String[]{null}));
  }

  @Test
  public void testIsEmpty() {
    Assert.assertFalse(org.apache.commons.lang.StringUtils.isBlank("1"));
    Assert.assertTrue(org.apache.commons.lang.StringUtils.isBlank(null));
    Assert.assertTrue(org.apache.commons.lang.StringUtils.isBlank(""));
  }

  @Test
  public void testIsNumeric() {
    Assert.assertFalse(org.apache.commons.lang.StringUtils.isNumeric(null));
    Assert.assertFalse(org.apache.commons.lang.StringUtils.isNumeric("\'"));
    Assert.assertTrue(org.apache.commons.lang.StringUtils.isNumeric("1"));
  }

  @Test
  public void testJoin() {
    Assert.assertEquals("", org.apache.commons.lang.StringUtils.join(new ArrayList(), "1a 2b 3c"));

    ArrayList collection = new ArrayList();
    collection.add(null);
    Assert.assertEquals("", org.apache.commons.lang.StringUtils.join(collection, "1a 2b 3c"));

    collection = new ArrayList();
    collection.add(-2_147_483_648);
    Assert.assertEquals("-2147483648", org.apache.commons.lang.StringUtils.join(collection, "1a 2b 3c"));
  }

  @Test
  public void testStartsWithIgnoreCase() {
    Assert.assertFalse(org.apache.commons.lang.StringUtils.startsWithIgnoreCase("A1B2C3", "BAZ"));
    Assert.assertFalse(org.apache.commons.lang.StringUtils.startsWithIgnoreCase(",", "BAZ"));
    Assert.assertTrue(org.apache.commons.lang.StringUtils.startsWithIgnoreCase("bar", "BAR"));
  }

  @Test
  public void testStartsWith() {
    Assert.assertFalse(org.apache.commons.lang.StringUtils.startsWith("1234", "1a 2b 3c"));
    Assert.assertTrue(org.apache.commons.lang.StringUtils.startsWith("1a 2b 3c", "1a 2b 3c"));
    Assert.assertTrue(org.apache.commons.lang.StringUtils.startsWith(null, null));
  }

  @Test
  public void testTrim() {
    Assert.assertEquals("1234", org.apache.commons.lang.StringUtils.trim("1234"));
    Assert.assertNull(org.apache.commons.lang.StringUtils.trim(null));
  }

  @Test
  public void testTrimToEmpty() {
    Assert.assertEquals("1234", org.apache.commons.lang.StringUtils.trimToEmpty("1234"));
    Assert.assertEquals("", org.apache.commons.lang.StringUtils.trimToEmpty(null));
  }

  @Test
  public void trimToNull() {
    Assert.assertNull(org.apache.commons.lang.StringUtils.trimToNull(null));
    Assert.assertEquals("foo", org.apache.commons.lang.StringUtils.trimToNull("foo"));
  }
}
