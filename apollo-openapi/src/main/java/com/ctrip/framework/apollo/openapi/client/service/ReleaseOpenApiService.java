package com.ctrip.framework.apollo.openapi.client.service;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenReleaseDTO;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * 发布 开放API
 */
public class ReleaseOpenApiService extends AbstractOpenApiService {

  /**
   * 构造发布 开放API
   *
   * @param client  客户端
   * @param baseUrl 基Url
   * @param gson    Json对象
   */
  public ReleaseOpenApiService(CloseableHttpClient client, String baseUrl, Gson gson) {
    super(client, baseUrl, gson);
  }

  /**
   * 名称空间发布
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param releaseDTO    发布信息
   * @return 发布的信息
   */
  public OpenReleaseDTO publishNamespace(String appId, String env, String clusterName,
      String namespaceName,
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
          .format("Release namespace: %s for appId: %s, cluster: %s in env: %s failed",
              namespaceName, appId,
              clusterName, env), ex);
    }
  }

  /**
   * 获取最新的发布信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 最新的发布信息
   */
  public OpenReleaseDTO getLatestActiveRelease(String appId, String env, String clusterName,
      String namespaceName) {
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
          .format(
              "Get latest active release for appId: %s, cluster: %s, namespace: %s in env: %s failed",
              appId,
              clusterName, namespaceName, env), ex);
    }
  }

  /**
   * 回滚发布信息
   *
   * @param env       环境
   * @param releaseId 发布id
   * @param operator  操作者
   */
  public void rollbackRelease(String env, long releaseId, String operator) {
    checkNotEmpty(env, "Env");
    checkNotEmpty(operator, "Operator");

    String path = String.format("envs/%s/releases/%s/rollback?operator=%s", escapePath(env),
        releaseId, escapeParam(operator));

    try (CloseableHttpResponse ignored = put(path, null)) {
    } catch (Throwable ex) {
      throw new RuntimeException(
          String.format("Rollback release: %s in env: %s failed", releaseId, env), ex);
    }
  }
}
