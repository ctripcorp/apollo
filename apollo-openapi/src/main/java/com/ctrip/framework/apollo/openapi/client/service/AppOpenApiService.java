package com.ctrip.framework.apollo.openapi.client.service;

import com.ctrip.framework.apollo.openapi.dto.OpenEnvClusterDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class AppOpenApiService extends AbstractOpenApiService {
  private static final Type OPEN_ENV_CLUSTER_DTO_LIST_TYPE = new TypeToken<List<OpenEnvClusterDTO>>() {
  }.getType();

  public AppOpenApiService(CloseableHttpClient client, String baseUrl, Gson gson) {
    super(client, baseUrl, gson);
  }

  public List<OpenEnvClusterDTO> getEnvClusterInfo(String appId) {
    checkNotEmpty(appId, "App id");

    String path = String.format("apps/%s/envclusters", escapePath(appId));

    try (CloseableHttpResponse response = get(path)) {
      return gson.fromJson(EntityUtils.toString(response.getEntity()), OPEN_ENV_CLUSTER_DTO_LIST_TYPE);
    } catch (Throwable ex) {
      throw new RuntimeException(String.format("Load env cluster information for appId: %s failed", appId), ex);
    }
  }
}
