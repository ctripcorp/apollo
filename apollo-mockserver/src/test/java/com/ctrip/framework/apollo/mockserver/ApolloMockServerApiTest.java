package com.ctrip.framework.apollo.mockserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.ClassRule;
import org.junit.Test;

public class ApolloMockServerApiTest {

  private static final String otherNamespace = "otherNamespace";

  @ClassRule
  public static EmbeddedApollo embeddedApollo = new EmbeddedApollo();

  @Test
  public void testGetProperty() throws Exception {
    Config applicationConfig = ConfigService.getAppConfig();

    assertEquals("value1", applicationConfig.getProperty("key1", null));
    assertEquals("value2", applicationConfig.getProperty("key2", null));
  }

  @Test
  public void testUpdateProperties() throws Exception {
    String someNewValue = "someNewValue";

    Config otherConfig = ConfigService.getConfig(otherNamespace);

    SettableFuture<ConfigChangeEvent> future = SettableFuture.create();

    otherConfig.addChangeListener(future::set);

    assertEquals("otherValue1", otherConfig.getProperty("key1", null));
    assertEquals("otherValue2", otherConfig.getProperty("key2", null));

    embeddedApollo.addOrModifyProperty(otherNamespace, "key1", someNewValue);

    ConfigChangeEvent changeEvent = future.get(5, TimeUnit.SECONDS);

    assertEquals(someNewValue, otherConfig.getProperty("key1", null));
    assertEquals("otherValue2", otherConfig.getProperty("key2", null));

    assertTrue(changeEvent.isChanged("key1"));
  }
}
