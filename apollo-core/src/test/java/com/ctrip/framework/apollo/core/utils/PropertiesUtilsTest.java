package com.ctrip.framework.apollo.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.LinkedHashMap;
import java.util.Properties;
import org.junit.Test;


public class PropertiesUtilsTest {

  @Test
  public void testPropertiesToString(){
    try {
      Properties properties = new Properties();
      properties.setProperty("test", "test value");
      String actual = PropertiesUtil.toString(properties);
      String expected="test=test value\n";
      assertEquals(expected,actual);
    }
    catch (Exception e){
      fail("Test Properties to String");
    }

  }

  @Test
  public void testLinkeHashMapToString(){
    try {
      LinkedHashMap properties = new LinkedHashMap();
      properties.put("test", "test value");
      String actual = PropertiesUtil.toString(properties);
      String expected="test=test value\n";
      assertEquals(expected,actual);
    }
    catch (Exception e){
      fail("Test LinkedHashMap to String");
    }

  }
}
