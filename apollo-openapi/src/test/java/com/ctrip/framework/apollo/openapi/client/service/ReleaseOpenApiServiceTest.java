package com.ctrip.framework.apollo.openapi.client.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ReleaseOpenApiServiceTest extends AbstractOpenApiServiceTest {

  private ReleaseOpenApiService releaseOpenApiService;

  private String someAppId;
  private String someEnv;
  private String someCluster;
  private String someNamespace;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    someAppId = "someAppId";
    someEnv = "someEnv";
    someCluster = "someCluster";
    someNamespace = "someNamespace";

    StringEntity responseEntity = new StringEntity("{}");
    when(someHttpResponse.getEntity()).thenReturn(responseEntity);

    releaseOpenApiService = new ReleaseOpenApiService(httpClient, someBaseUrl, gson);
  }

  @Test
  public void testPublishNamespace() throws Exception {
    String someReleaseTitle = "someReleaseTitle";
    String someReleasedBy = "someReleasedBy";

    NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
    namespaceReleaseDTO.setReleaseTitle(someReleaseTitle);
    namespaceReleaseDTO.setReleasedBy(someReleasedBy);

    final ArgumentCaptor<HttpPost> request = ArgumentCaptor.forClass(HttpPost.class);

    releaseOpenApiService.publishNamespace(someAppId, someEnv, someCluster, someNamespace, namespaceReleaseDTO);

    verify(httpClient, times(1)).execute(request.capture());

    HttpPost post = request.getValue();

    assertEquals(String
        .format("%s/envs/%s/apps/%s/clusters/%s/namespaces/%s/releases", someBaseUrl, someEnv, someAppId, someCluster,
            someNamespace), post.getURI().toString());
  }

  @Test(expected = RuntimeException.class)
  public void testPublishNamespaceWithError() throws Exception {
    String someReleaseTitle = "someReleaseTitle";
    String someReleasedBy = "someReleasedBy";

    NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
    namespaceReleaseDTO.setReleaseTitle(someReleaseTitle);
    namespaceReleaseDTO.setReleasedBy(someReleasedBy);

    when(statusLine.getStatusCode()).thenReturn(400);

    releaseOpenApiService.publishNamespace(someAppId, someEnv, someCluster, someNamespace, namespaceReleaseDTO);
  }

  @Test
  public void testGetLatestActiveRelease() throws Exception {
    final ArgumentCaptor<HttpGet> request = ArgumentCaptor.forClass(HttpGet.class);

    releaseOpenApiService.getLatestActiveRelease(someAppId, someEnv, someCluster, someNamespace);

    verify(httpClient, times(1)).execute(request.capture());

    HttpGet get = request.getValue();

    assertEquals(String
        .format("%s/envs/%s/apps/%s/clusters/%s/namespaces/%s/releases/latest", someBaseUrl, someEnv, someAppId, someCluster,
            someNamespace), get.getURI().toString());
  }

  @Test(expected = RuntimeException.class)
  public void testGetLatestActiveReleaseWithError() throws Exception {
    when(statusLine.getStatusCode()).thenReturn(400);

    releaseOpenApiService.getLatestActiveRelease(someAppId, someEnv, someCluster, someNamespace);
  }
}
