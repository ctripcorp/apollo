package com.ctrip.framework.apollo.portal.environment;

import static org.junit.Assert.*;

import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatabasePortalMetaServerProviderTest {

  private DatabasePortalMetaServerProvider databasePortalMetaServerProvider;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    // mock it
    PortalConfig portalConfig = Mockito.mock(PortalConfig.class);
    final Map<String, String> map = new HashMap<>();
    map.put("nothing", "http://unknown.com");
    map.put("dev", "http://server.com:8080");
    Mockito.when(portalConfig.getMetaServers()).thenReturn(map);

    // use mocked object to construct
    databasePortalMetaServerProvider = new DatabasePortalMetaServerProvider(portalConfig);
  }

  @Test
  public void getMetaServerAddress() {
    String address = databasePortalMetaServerProvider.getMetaServerAddress(Env.DEV);
    assertEquals("http://server.com:8080", address);
  }
}