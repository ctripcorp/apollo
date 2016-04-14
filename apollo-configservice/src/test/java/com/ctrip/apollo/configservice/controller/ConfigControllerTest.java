package com.ctrip.apollo.configservice.controller;

import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.service.ConfigService;
import com.ctrip.apollo.core.dto.ApolloConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigControllerTest {

  private ConfigController configController;
  @Mock
  private ConfigService configService;

  @Before
  public void setUp() throws Exception {
    configController = new ConfigController();
    ReflectionTestUtils.setField(configController, "configService", configService);
  }

  @Test
  public void testQueryConfig() throws Exception {
    ApolloConfig someApolloConfig = mock(ApolloConfig.class);
    String someAppId = "1";
    String someClusterName = "someClusterName";
    String someNamespaceName = "someNamespaceName";
    long someClientSideReleaseId = 1;
    long someServerSideNewReleaseId = 2;
    HttpServletResponse someResponse = mock(HttpServletResponse.class);
    Release someRelease = mock(Release.class);

    when(configService.findRelease(someAppId, someClusterName, someNamespaceName))
        .thenReturn(someRelease);
    when(someRelease.getId()).thenReturn(someServerSideNewReleaseId);
    when(configService.loadConfig(someRelease, someNamespaceName)).thenReturn(someApolloConfig);

    ApolloConfig result = configController.queryConfig(someAppId, someClusterName,
        someNamespaceName, someClientSideReleaseId, someResponse);

    assertEquals(someApolloConfig, result);
    verify(configService, times(1)).findRelease(someAppId, someClusterName, someNamespaceName);
    verify(configService, times(1)).loadConfig(someRelease, someNamespaceName);
  }


  @Test
  public void testQueryConfigWithVersionNotFound() throws Exception {
    String someAppId = "1";
    String someClusterName = "someClusterName";
    String someNamespaceName = "someNamespaceName";
    long someClientSideReleaseId = 1;
    HttpServletResponse someResponse = mock(HttpServletResponse.class);

    when(configService.findRelease(someAppId, someClusterName, someNamespaceName)).thenReturn(null);

    ApolloConfig result = configController.queryConfig(someAppId, someClusterName,
        someNamespaceName, someClientSideReleaseId, someResponse);

    assertNull(result);
    verify(someResponse, times(1)).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
  }

  @Test
  public void testQueryConfigWithApolloConfigNotFound() throws Exception {
    String someAppId = "1";
    String someClusterName = "someClusterName";
    String someNamespaceName = "someNamespaceName";
    long someClientSideReleaseId = 1;
    long someServerSideNewReleaseId = 2;
    HttpServletResponse someResponse = mock(HttpServletResponse.class);
    Release someRelease = mock(Release.class);

    when(configService.findRelease(someAppId, someClusterName, someNamespaceName))
        .thenReturn(someRelease);
    when(someRelease.getId()).thenReturn(someServerSideNewReleaseId);
    when(configService.loadConfig(someRelease, someNamespaceName)).thenReturn(null);

    ApolloConfig result = configController.queryConfig(someAppId, someClusterName,
        someNamespaceName, someClientSideReleaseId, someResponse);

    assertNull(result);
    verify(someResponse, times(1)).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
  }

  @Test
  public void testQueryConfigWithApolloConfigNotModified() throws Exception {
    String someAppId = "1";
    String someClusterName = "someClusterName";
    String someNamespaceName = "someNamespaceName";
    long someClientSideReleaseId = 1;
    long someServerSideReleaseId = someClientSideReleaseId;
    HttpServletResponse someResponse = mock(HttpServletResponse.class);
    Release someRelease = mock(Release.class);

    when(configService.findRelease(someAppId, someClusterName, someNamespaceName))
        .thenReturn(someRelease);
    when(someRelease.getId()).thenReturn(someServerSideReleaseId);

    ApolloConfig result = configController.queryConfig(someAppId, someClusterName, someNamespaceName,
        someClientSideReleaseId, someResponse);

    assertNull(result);
    verify(someResponse, times(1)).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    verify(configService, never()).loadConfig(any(Release.class), anyString());
  }
}
