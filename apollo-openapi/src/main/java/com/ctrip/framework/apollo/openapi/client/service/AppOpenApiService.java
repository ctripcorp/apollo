package com.ctrip.framework.apollo.openapi.client.service;

import com.ctrip.framework.apollo.openapi.dto.OpenAppDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenEnvClusterDTO;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * 应用开放API服务
 */
public class AppOpenApiService extends AbstractOpenApiService {

  /**
   * 开放的环境集群类型
   */
  private static final Type OPEN_ENV_CLUSTER_DTO_LIST_TYPE = new TypeToken<List<OpenEnvClusterDTO>>() {
  }.getType();

  /**
   * 开放的应用信息
   */
  private static final Type OPEN_APP_DTO_LIST_TYPE = new TypeToken<List<OpenAppDTO>>() {
  }.getType();

  /**
   * 构建应用开放API服务
   *
   * @param client  客户端
   * @param baseUrl 基Url
   * @param gson    Json对象
   */
  public AppOpenApiService(CloseableHttpClient client, String baseUrl, Gson gson) {
    super(client, baseUrl, gson);
  }

  /**
   * 获取环境集群信息列表
   *
   * @param appId 应用id
   * @return 环境集群信息列表
   */
  public List<OpenEnvClusterDTO> getEnvClusterInfo(String appId) {
    checkNotEmpty(appId, "App id");

    String path = String.format("apps/%s/envclusters", escapePath(appId));

    try (CloseableHttpResponse response = get(path)) {
      return gson
          .fromJson(EntityUtils.toString(response.getEntity()), OPEN_ENV_CLUSTER_DTO_LIST_TYPE);
    } catch (Throwable ex) {
      throw new RuntimeException(
          String.format("Load env cluster information for appId: %s failed", appId), ex);
    }
  }

  /**
   * 获取应用信息列表
   *
   * @param appIds 应用id列表
   * @return 环境集群信息列表
   */
  public List<OpenAppDTO> getAppsInfo(List<String> appIds) {
    String path = "apps";

    if (appIds != null && !appIds.isEmpty()) {
      String param = Joiner.on(",").join(appIds);
      path = String.format("apps?appIds=%s", escapeParam(param));
    }

    try (CloseableHttpResponse response = get(path)) {
      return gson.fromJson(EntityUtils.toString(response.getEntity()), OPEN_APP_DTO_LIST_TYPE);
    } catch (Throwable ex) {
      throw new RuntimeException(
          String.format("Load app information for appIds: %s failed", appIds), ex);
    }
  }
}
