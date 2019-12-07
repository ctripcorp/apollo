package com.ctrip.framework.apollo.spring.spi;

import com.ctrip.framework.apollo.spring.annotation.ApolloConfigRegistrar;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.springframework.test.util.AssertionErrors.assertEquals;

public class ApolloConfigRegistrarHelperTest {

  @Test
  public void testHelperLoadingOrder() {
    ApolloConfigRegistrar apolloConfigRegistrar = new ApolloConfigRegistrar();

    Field field = ReflectionUtils.findField(ApolloConfigRegistrar.class, "helper");
    ReflectionUtils.makeAccessible(field);
    Object helper = ReflectionUtils.getField(field, apolloConfigRegistrar);

    assertEquals("helper is not TestRegistrarHelper instance", TestRegistrarHelper.class, helper.getClass());
  }
}
