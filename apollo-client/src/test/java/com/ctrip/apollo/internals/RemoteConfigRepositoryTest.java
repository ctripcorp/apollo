package com.ctrip.apollo.internals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.ctrip.apollo.core.dto.ApolloConfig;
import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.util.ConfigUtil;
import com.ctrip.apollo.util.http.HttpRequest;
import com.ctrip.apollo.util.http.HttpResponse;
import com.ctrip.apollo.util.http.HttpUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unidal.lookup.ComponentTestCase;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Jason on 4/9/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteConfigRepositoryTest extends ComponentTestCase {
  @Mock
  private ConfigServiceLocator configServiceLocator;
  private String someNamespace;
  @Mock
  private static HttpResponse<ApolloConfig> someResponse;
  @Mock
  private ConfigUtil someConfigUtil;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    someNamespace = "someName";

    defineComponent(ConfigUtil.class, MockConfigUtil.class);
    defineComponent(ConfigServiceLocator.class, MockConfigServiceLocator.class);
    defineComponent(HttpUtil.class, MockHttpUtil.class);
  }

  @Test
  public void testLoadConfig() throws Exception {
    String someKey = "someKey";
    String someValue = "someValue";
    Map<String, String> configurations = Maps.newHashMap();
    configurations.put(someKey, someValue);
    ApolloConfig someApolloConfig = assembleApolloConfig(configurations);

    when(someResponse.getStatusCode()).thenReturn(200);
    when(someResponse.getBody()).thenReturn(someApolloConfig);

    RemoteConfigRepository remoteConfigRepository = new RemoteConfigRepository(someNamespace);
    Properties config = remoteConfigRepository.getConfig();

    assertEquals(configurations, config);
  }

  @Test(expected = RuntimeException.class)
  public void testGetRemoteConfigWithServerError() throws Exception {

    when(someResponse.getStatusCode()).thenReturn(500);

    RemoteConfigRepository remoteConfigRepository = new RemoteConfigRepository(someNamespace);
    remoteConfigRepository.getConfig();
  }

  @Test
  public void testRepositoryChangeListener() throws Exception {
    Map<String, String> configurations = ImmutableMap.of("someKey", "someValue");
    ApolloConfig someApolloConfig = assembleApolloConfig(configurations);

    when(someResponse.getStatusCode()).thenReturn(200);
    when(someResponse.getBody()).thenReturn(someApolloConfig);

    RepositoryChangeListener someListener = mock(RepositoryChangeListener.class);
    RemoteConfigRepository remoteConfigRepository = new RemoteConfigRepository(someNamespace);
    remoteConfigRepository.addChangeListener(someListener);
    final ArgumentCaptor<Properties> captor = ArgumentCaptor.forClass(Properties.class);

    remoteConfigRepository.loadRemoteConfig();

    verify(someListener, times(1)).onRepositoryChange(eq(someNamespace), captor.capture());

    assertEquals(configurations, captor.getValue());
  }

  private ApolloConfig assembleApolloConfig(Map<String, String> configurations) {
    String someAppId = "appId";
    String someClusterName = "cluster";
    long someReleaseId = 1;
    ApolloConfig apolloConfig =
            new ApolloConfig(someAppId, someClusterName, someNamespace, someReleaseId);

    apolloConfig.setConfigurations(configurations);

    return apolloConfig;
  }

  public static class MockConfigUtil extends ConfigUtil {
    @Override
    public String getAppId() {
      return "someApp";
    }

    @Override
    public String getCluster() {
      return "someCluster";
    }
  }

  public static class MockConfigServiceLocator extends ConfigServiceLocator {
    @Override
    public List<ServiceDTO> getConfigServices() {
      String someServerUrl = "http://someServer";

      ServiceDTO serviceDTO = mock(ServiceDTO.class);

      when(serviceDTO.getHomepageUrl()).thenReturn(someServerUrl);
      return Lists.newArrayList(serviceDTO);
    }
  }

  public static class MockHttpUtil extends HttpUtil {
    @Override
    public <T> HttpResponse<T> doGet(HttpRequest httpRequest, Class<T> responseType) {
      return (HttpResponse<T>) someResponse;
    }
  }

}
