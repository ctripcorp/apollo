package com.ctrip.framework.apollo.spring.spi;

import com.ctrip.framework.apollo.spring.config.ConfigPropertySourcesProcessor;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.springframework.test.util.AssertionErrors.assertEquals;

public class ConfigPropertySourcesProcessorHelperTest {

  @Test
  public void testHelperLoadingOrder() {
    ConfigPropertySourcesProcessor processor = new ConfigPropertySourcesProcessor();

    Field field = ReflectionUtils.findField(ConfigPropertySourcesProcessor.class, "helper");
    ReflectionUtils.makeAccessible(field);
    Object helper = ReflectionUtils.getField(field, processor);

    assertEquals("helper is not TestProcessorHelper instance", TestProcessorHelper.class, helper.getClass());
  }
}
