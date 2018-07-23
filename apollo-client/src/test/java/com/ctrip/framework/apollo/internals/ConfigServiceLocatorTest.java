package com.ctrip.framework.apollo.internals;

import static org.junit.Assert.assertEquals;

import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import java.util.List;
import org.junit.After;
import org.junit.Test;

public class ConfigServiceLocatorTest {

  @After
  public void tearDown() throws Exception {
    System.clearProperty("apollo.configService");
  }

  @Test
  public void testGetConfigServicesWithSystemProperty() throws Exception {
    String someConfigServiceUrl = " someConfigServiceUrl ";
    String anotherConfigServiceUrl = " anotherConfigServiceUrl ";

    System.setProperty("apollo.configService", someConfigServiceUrl + "," + anotherConfigServiceUrl);

    ConfigServiceLocator configServiceLocator = new ConfigServiceLocator();

    List<ServiceDTO> result = configServiceLocator.getConfigServices();

    assertEquals(2, result.size());

    assertEquals(someConfigServiceUrl.trim(), result.get(0).getHomepageUrl());
    assertEquals(anotherConfigServiceUrl.trim(), result.get(1).getHomepageUrl());
  }
}
