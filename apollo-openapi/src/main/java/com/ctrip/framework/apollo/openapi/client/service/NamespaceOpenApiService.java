package com.ctrip.framework.apollo.openapi.client.service;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.openapi.dto.OpenAppNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceLockDTO;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * 名称空间 开放Api
 */
public class NamespaceOpenApiService extends AbstractOpenApiService {

  /**
   * 名称空间类型
   */
  private static final Type OPEN_NAMESPACE_DTO_LIST_TYPE = new TypeToken<List<OpenNamespaceDTO>>() {
  }.getType();

  /**
   * 构建名称空间 开放API服务
   *
   * @param client  客户端
   * @param baseUrl 基Url
   * @param gson    Json对象
   */
  public NamespaceOpenApiService(CloseableHttpClient client, String baseUrl, Gson gson) {
    super(client, baseUrl, gson);
  }

  /**
   * 获取名称空间信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 名称空间信息
   */
  public OpenNamespaceDTO getNamespace(String appId, String env, String clusterName,
      String namespaceName) {
    if (Strings.isNullOrEmpty(clusterName)) {
      clusterName = ConfigConsts.CLUSTER_NAME_DEFAULT;
    }
    if (Strings.isNullOrEmpty(namespaceName)) {
      namespaceName = ConfigConsts.NAMESPACE_APPLICATION;
    }

    checkNotEmpty(appId, "App id");
    checkNotEmpty(env, "Env");

    String path = String.format("envs/%s/apps/%s/clusters/%s/namespaces/%s", escapePath(env),
        escapePath(appId), escapePath(clusterName), escapePath(namespaceName));

    try (CloseableHttpResponse response = get(path)) {
      return gson.fromJson(EntityUtils.toString(response.getEntity()), OpenNamespaceDTO.class);
    } catch (Throwable ex) {
      throw new RuntimeException(String
          .format("Get namespace for appId: %s, cluster: %s, namespace: %s in env: %s failed",
              appId, clusterName,
              namespaceName, env), ex);
    }
  }

  /**
   * 获取名称空间信息列表
   *
   * @param appId       应用id
   * @param env         环境
   * @param clusterName 集群名称
   * @return 名称空间信息列表
   */
  public List<OpenNamespaceDTO> getNamespaces(String appId, String env, String clusterName) {
    if (Strings.isNullOrEmpty(clusterName)) {
      clusterName = ConfigConsts.CLUSTER_NAME_DEFAULT;
    }

    checkNotEmpty(appId, "App id");
    checkNotEmpty(env, "Env");

    String path = String.format("envs/%s/apps/%s/clusters/%s/namespaces", escapePath(env),
        escapePath(appId), escapePath(clusterName));

    try (CloseableHttpResponse response = get(path)) {
      return gson
          .fromJson(EntityUtils.toString(response.getEntity()), OPEN_NAMESPACE_DTO_LIST_TYPE);
    } catch (Throwable ex) {
      throw new RuntimeException(String
          .format("Get namespaces for appId: %s, cluster: %s in env: %s failed", appId, clusterName,
              env), ex);
    }
  }

  /**
   * 创建名称空间
   *
   * @param appNamespaceDTO 名称空间信息
   * @return 创建的名称空间
   */
  public OpenAppNamespaceDTO createAppNamespace(OpenAppNamespaceDTO appNamespaceDTO) {
    checkNotEmpty(appNamespaceDTO.getAppId(), "App id");
    checkNotEmpty(appNamespaceDTO.getName(), "Name");
    checkNotEmpty(appNamespaceDTO.getDataChangeCreatedBy(), "Created by");

    if (Strings.isNullOrEmpty(appNamespaceDTO.getFormat())) {
      appNamespaceDTO.setFormat(ConfigFileFormat.Properties.getValue());
    }

    String path = String.format("apps/%s/appnamespaces", escapePath(appNamespaceDTO.getAppId()));

    try (CloseableHttpResponse response = post(path, appNamespaceDTO)) {
      return gson.fromJson(EntityUtils.toString(response.getEntity()), OpenAppNamespaceDTO.class);
    } catch (Throwable ex) {
      throw new RuntimeException(String
          .format("Create app namespace: %s for appId: %s, format: %s failed",
              appNamespaceDTO.getName(),
              appNamespaceDTO.getAppId(), appNamespaceDTO.getFormat()), ex);
    }
  }

  /**
   * 获取名称空间锁信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 名称空间锁信息
   */
  public OpenNamespaceLockDTO getNamespaceLock(String appId, String env, String clusterName,
      String namespaceName) {
    if (Strings.isNullOrEmpty(clusterName)) {
      clusterName = ConfigConsts.CLUSTER_NAME_DEFAULT;
    }
    if (Strings.isNullOrEmpty(namespaceName)) {
      namespaceName = ConfigConsts.NAMESPACE_APPLICATION;
    }

    checkNotEmpty(appId, "App id");
    checkNotEmpty(env, "Env");

    String path = String.format("envs/%s/apps/%s/clusters/%s/namespaces/%s/lock", escapePath(env),
        escapePath(appId),
        escapePath(clusterName), escapePath(namespaceName));

    try (CloseableHttpResponse response = get(path)) {
      return gson.fromJson(EntityUtils.toString(response.getEntity()), OpenNamespaceLockDTO.class);
    } catch (Throwable ex) {
      throw new RuntimeException(String
          .format("Get namespace lock for appId: %s, cluster: %s, namespace: %s in env: %s failed",
              appId, clusterName,
              namespaceName, env), ex);
    }
  }
}
