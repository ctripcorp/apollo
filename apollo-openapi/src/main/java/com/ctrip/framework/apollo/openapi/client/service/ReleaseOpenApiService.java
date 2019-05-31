package com.ctrip.framework.apollo.openapi.client.service;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenReleaseDTO;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class ReleaseOpenApiService extends AbstractOpenApiService {

  public ReleaseOpenApiService(CloseableHttpClient client, String baseUrl, Gson gson) {
    super(client, baseUrl, gson);
  }

  public OpenReleaseDTO publishNamespace(String appId, String env, String clusterName, String namespaceName,
      NamespaceReleaseDTO releaseDTO) {
    if (Strings.isNullOrEmpty(clusterName)) {
      clusterName = ConfigConsts.CLUSTER_NAME_DEFAULT;
    }
    if (Strings.isNullOrEmpty(namespaceName)) {
      namespaceName = ConfigConsts.NAMESPACE_APPLICATION;
    }

    checkNotEmpty(appId, "App id");
    checkNotEmpty(env, "Env");
    checkNotEmpty(releaseDTO.getReleaseTitle(), "Release title");
    checkNotEmpty(releaseDTO.getReleasedBy(), "Released by");

    String path = String.format("envs/%s/apps/%s/clusters/%s/namespaces/%s/releases",
        escapePath(env), escapePath(appId), escapePath(clusterName), escapePath(namespaceName));

    try (CloseableHttpResponse response = post(path, releaseDTO)) {
      return gson.fromJson(EntityUtils.toString(response.getEntity()), OpenReleaseDTO.class);
    } catch (Throwable ex) {
      throw new RuntimeException(String
          .format("Release namespace: %s for appId: %s, cluster: %s in env: %s failed", namespaceName, appId,
              clusterName, env), ex);
    }
  }

  public OpenReleaseDTO getLatestActiveRelease(String appId, String env, String clusterName, String namespaceName) {
    if (Strings.isNullOrEmpty(clusterName)) {
      clusterName = ConfigConsts.CLUSTER_NAME_DEFAULT;
    }
    if (Strings.isNullOrEmpty(namespaceName)) {
      namespaceName = ConfigConsts.NAMESPACE_APPLICATION;
    }

    checkNotEmpty(appId, "App id");
    checkNotEmpty(env, "Env");

    String path = String.format("envs/%s/apps/%s/clusters/%s/namespaces/%s/releases/latest",
        escapePath(env), escapePath(appId), escapePath(clusterName), escapePath(namespaceName));

    try (CloseableHttpResponse response = get(path)) {
      return gson.fromJson(EntityUtils.toString(response.getEntity()), OpenReleaseDTO.class);
    } catch (Throwable ex) {
      throw new RuntimeException(String
          .format("Get latest active release for appId: %s, cluster: %s, namespace: %s in env: %s failed", appId,
              clusterName, namespaceName, env), ex);
    }
  }

}
