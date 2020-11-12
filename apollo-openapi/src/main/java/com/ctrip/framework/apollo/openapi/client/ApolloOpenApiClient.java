package com.ctrip.framework.apollo.openapi.client;

import com.ctrip.framework.apollo.openapi.client.constant.ApolloOpenApiConstants;
import com.ctrip.framework.apollo.openapi.client.service.AppOpenApiService;
import com.ctrip.framework.apollo.openapi.client.service.ClusterOpenApiService;
import com.ctrip.framework.apollo.openapi.client.service.ItemOpenApiService;
import com.ctrip.framework.apollo.openapi.client.service.NamespaceOpenApiService;
import com.ctrip.framework.apollo.openapi.client.service.ReleaseOpenApiService;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenAppDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenAppNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenClusterDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenEnvClusterDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceLockDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenReleaseDTO;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import lombok.Getter;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

/**
 * 此类包含访问Apollo OpenAPI的方法集合 <br />
 * <p>更多信息,请查看 <a href="https://github.com/ctripcorp/apollo/wiki/">Apollo
 * Wiki</a>.
 */
public class ApolloOpenApiClient {

  /**
   * 界面Url
   */
  @Getter
  private final String portalUrl;
  /**
   * 授权令牌
   */
  @Getter
  private final String token;
  private final AppOpenApiService appService;
  private final ItemOpenApiService itemService;
  private final ReleaseOpenApiService releaseService;
  private final NamespaceOpenApiService namespaceService;
  private final ClusterOpenApiService clusterService;
  private static final Gson GSON = new GsonBuilder()
      .setDateFormat(ApolloOpenApiConstants.JSON_DATE_FORMAT).create();

  /**
   * 构造Apollo开放平台客户端
   *
   * @param portalUrl
   * @param token
   * @param requestConfig
   */
  private ApolloOpenApiClient(String portalUrl, String token, RequestConfig requestConfig) {
    this.portalUrl = portalUrl;
    this.token = token;
    CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig)
        .setDefaultHeaders(Lists.newArrayList(new BasicHeader("Authorization", token))).build();

    String baseUrl = this.portalUrl + ApolloOpenApiConstants.OPEN_API_V1_PREFIX;
    appService = new AppOpenApiService(client, baseUrl, GSON);
    clusterService = new ClusterOpenApiService(client, baseUrl, GSON);
    namespaceService = new NamespaceOpenApiService(client, baseUrl, GSON);
    itemService = new ItemOpenApiService(client, baseUrl, GSON);
    releaseService = new ReleaseOpenApiService(client, baseUrl, GSON);
  }

  /**
   * 获取环境集群信息列表
   *
   * @param appId 应用id
   * @return 环境集群信息列表
   */
  public List<OpenEnvClusterDTO> getEnvClusterInfo(String appId) {
    return appService.getEnvClusterInfo(appId);
  }

  /**
   * 获取应用信息列表
   *
   * @return 环境集群信息列表
   */
  public List<OpenAppDTO> getAllApps() {
    return appService.getAppsInfo(null);
  }

  /**
   * 获取应用信息列表
   *
   * @param appIds 应用id列表
   * @return 环境集群信息列表
   */
  public List<OpenAppDTO> getAppsByIds(List<String> appIds) {
    return appService.getAppsInfo(appIds);
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
    return namespaceService.getNamespaces(appId, env, clusterName);
  }

  /**
   * 获取集群信息
   *
   * @param appId       应用id
   * @param env         环境
   * @param clusterName 集群名称
   * @return 集群信息
   * @since 1.5.0
   */
  public OpenClusterDTO getCluster(String appId, String env, String clusterName) {
    return clusterService.getCluster(appId, env, clusterName);
  }


  /**
   * 创建集群
   *
   * @param env            环境
   * @param openClusterDTO 集群实体
   * @return 创建的集群信息
   * @since 1.5.0
   */
  public OpenClusterDTO createCluster(String env, OpenClusterDTO openClusterDTO) {
    return clusterService.createCluster(env, openClusterDTO);
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
    return namespaceService.getNamespace(appId, env, clusterName, namespaceName);
  }

  /**
   * 创建名称空间
   *
   * @param appNamespaceDTO 名称空间信息
   * @return 创建的名称空间
   */
  public OpenAppNamespaceDTO createAppNamespace(OpenAppNamespaceDTO appNamespaceDTO) {
    return namespaceService.createAppNamespace(appNamespaceDTO);
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
    return namespaceService.getNamespaceLock(appId, env, clusterName, namespaceName);
  }

  /**
   * 获取配置项信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param key           配置项的key
   * @return 指定的配置项信息
   * @since 1.2.0
   */
  public OpenItemDTO getItem(String appId, String env, String clusterName, String namespaceName,
      String key) {
    return itemService.getItem(appId, env, clusterName, namespaceName, key);
  }

  /**
   * 获取配置项信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param itemDTO       配置项信息
   * @return 指定的配置项信息
   */
  public OpenItemDTO createItem(String appId, String env, String clusterName, String namespaceName,
      OpenItemDTO itemDTO) {
    return itemService.createItem(appId, env, clusterName, namespaceName, itemDTO);
  }

  /**
   * 更新配置项信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param itemDTO       配置项信息
   * @return 更新后的配置项信息
   */
  public void updateItem(String appId, String env, String clusterName, String namespaceName,
      OpenItemDTO itemDTO) {
    itemService.updateItem(appId, env, clusterName, namespaceName, itemDTO);
  }


  /**
   * 创建或更新配置项信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param itemDTO       配置项信息
   * @return 创建或更新后的配置项信息
   */
  public void createOrUpdateItem(String appId, String env, String clusterName, String namespaceName,
      OpenItemDTO itemDTO) {
    itemService.createOrUpdateItem(appId, env, clusterName, namespaceName, itemDTO);
  }

  /**
   * 移除配置项信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param key           配置项的key
   * @param operator      操作人
   * @return 创建或更新后的配置项信息
   */
  public void removeItem(String appId, String env, String clusterName, String namespaceName,
      String key,
      String operator) {
    itemService.removeItem(appId, env, clusterName, namespaceName, key, operator);
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
    return releaseService.publishNamespace(appId, env, clusterName, namespaceName, releaseDTO);
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
    return releaseService.getLatestActiveRelease(appId, env, clusterName, namespaceName);
  }

  /**
   * 回滚发布信息
   *
   * @param env       环境
   * @param releaseId 发布id
   * @param operator  操作者
   * @since 1.5.0
   */
  public void rollbackRelease(String env, long releaseId, String operator) {
    releaseService.rollbackRelease(env, releaseId, operator);
  }


  /**
   * 获取Apollo开放Api客户端构建器
   *
   * @return Apollo开放Api客户端构建器
   */

  public static ApolloOpenApiClientBuilder newBuilder() {
    return new ApolloOpenApiClientBuilder();
  }

  /**
   * Apollo开放Api客户端构建器
   */
  public static class ApolloOpenApiClientBuilder {

    /**
     * 界面Url
     */
    private String portalUrl;
    /**
     * 授权令牌
     */
    private String token;
    /**
     * 连接超时时间
     */
    private int connectTimeout = -1;
    /**
     * 读取超时时间
     */
    private int readTimeout = -1;

    /**
     * 构建界面Url
     *
     * @param portalUrl The apollo portal url, e.g http://localhost:8070
     */
    public ApolloOpenApiClientBuilder withPortalUrl(String portalUrl) {
      this.portalUrl = portalUrl;
      return this;
    }

    /**
     * 构建授权令牌
     *
     * @param token 授权令牌
     */
    public ApolloOpenApiClientBuilder withToken(String token) {
      this.token = token;
      return this;
    }

    /**
     * 构建连接超时时间
     *
     * @param connectTimeout 以毫秒为单位指定连接超时值的整数
     */
    public ApolloOpenApiClientBuilder withConnectTimeout(int connectTimeout) {
      this.connectTimeout = connectTimeout;
      return this;
    }

    /**
     * 构建读取超时时间
     *
     * @param readTimeout 一个整数，指定要以毫秒为单位的读取超时值
     */
    public ApolloOpenApiClientBuilder withReadTimeout(int readTimeout) {
      this.readTimeout = readTimeout;
      return this;
    }

    /**
     * 构建ApolloOpenApiClient对象
     *
     * @return apollo开放api客户端
     */
    public ApolloOpenApiClient build() {
      Preconditions.checkArgument(!Strings.isNullOrEmpty(portalUrl),
          "Portal url should not be null or empty!");
      Preconditions.checkArgument(portalUrl.startsWith("http://") ||
          portalUrl.startsWith("https://"), "Portal url should start with http:// or https://");
      Preconditions
          .checkArgument(!Strings.isNullOrEmpty(token), "Token should not be null or empty!");

      if (connectTimeout < 0) {
        connectTimeout = ApolloOpenApiConstants.DEFAULT_CONNECT_TIMEOUT;
      }

      if (readTimeout < 0) {
        readTimeout = ApolloOpenApiConstants.DEFAULT_READ_TIMEOUT;
      }

      RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectTimeout)
          .setSocketTimeout(readTimeout).build();

      return new ApolloOpenApiClient(portalUrl, token, requestConfig);
    }
  }
}
