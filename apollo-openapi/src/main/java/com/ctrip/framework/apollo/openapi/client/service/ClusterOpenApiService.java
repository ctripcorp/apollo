package com.ctrip.framework.apollo.openapi.client.service;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.openapi.dto.OpenClusterDTO;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * 集群 开放API
 */
public class ClusterOpenApiService extends AbstractOpenApiService {

  /**
   * 构建集群 开放API服务
   *
   * @param client  客户端
   * @param baseUrl 基Url
   * @param gson    Json对象
   */
  public ClusterOpenApiService(CloseableHttpClient client, String baseUrl, Gson gson) {
    super(client, baseUrl, gson);
  }

  /**
   * 获取集群信息
   *
   * @param appId       应用id
   * @param env         环境
   * @param clusterName 集群名称
   * @return 集群信息
   */
  public OpenClusterDTO getCluster(String appId, String env, String clusterName) {
    checkNotEmpty(appId, "App id");
    checkNotEmpty(env, "Env");

    if (Strings.isNullOrEmpty(clusterName)) {
      clusterName = ConfigConsts.CLUSTER_NAME_DEFAULT;
    }

    String path = String.format("envs/%s/apps/%s/clusters/%s", escapePath(env), escapePath(appId),
        escapePath(clusterName));

    try (CloseableHttpResponse response = get(path)) {
      return gson.fromJson(EntityUtils.toString(response.getEntity()), OpenClusterDTO.class);
    } catch (Throwable ex) {
      throw new RuntimeException(String
          .format("Get cluster for appId: %s, cluster: %s in env: %s failed", appId, clusterName,
              env), ex);
    }
  }

  /**
   * 创建集群
   *
   * @param env            环境
   * @param openClusterDTO 集群实体
   * @return 创建的集群信息
   */
  public OpenClusterDTO createCluster(String env, OpenClusterDTO openClusterDTO) {
    checkNotEmpty(openClusterDTO.getAppId(), "App id");
    checkNotEmpty(env, "Env");
    checkNotEmpty(openClusterDTO.getName(), "Cluster name");
    checkNotEmpty(openClusterDTO.getDataChangeCreatedBy(), "Created by");

    String path = String
        .format("envs/%s/apps/%s/clusters", escapePath(env), escapePath(openClusterDTO.getAppId()));

    try (CloseableHttpResponse response = post(path, openClusterDTO)) {
      return gson.fromJson(EntityUtils.toString(response.getEntity()), OpenClusterDTO.class);
    } catch (Throwable ex) {
      throw new RuntimeException(String
          .format("Create cluster: %s for appId: %s in env: %s failed", openClusterDTO.getName(),
              openClusterDTO.getAppId(), env), ex);
    }
  }
}
