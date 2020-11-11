package com.ctrip.framework.apollo.configservice.controller;

import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.configservice.service.AppNamespaceServiceWithCache;
import com.ctrip.framework.apollo.configservice.service.config.ConfigService;
import com.ctrip.framework.apollo.configservice.util.InstanceConfigAuditUtil;
import com.ctrip.framework.apollo.configservice.util.NamespaceUtil;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.core.dto.ApolloNotificationMessages;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Apollo配置 Controller层，提供配置读取的功能
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/configs")
public class ConfigController {

  private static final Splitter X_FORWARDED_FOR_SPLITTER = Splitter.on(",").omitEmptyStrings()
      .trimResults();
  private final ConfigService configService;
  private final AppNamespaceServiceWithCache appNamespaceService;
  private final NamespaceUtil namespaceUtil;
  private final InstanceConfigAuditUtil instanceConfigAuditUtil;
  private final Gson gson;

  private static final Type configurationTypeReference = new TypeToken<Map<String, String>>() {
  }.getType();

  public ConfigController(
      final ConfigService configService,
      final AppNamespaceServiceWithCache appNamespaceService,
      final NamespaceUtil namespaceUtil,
      final InstanceConfigAuditUtil instanceConfigAuditUtil,
      final Gson gson) {
    this.configService = configService;
    this.appNamespaceService = appNamespaceService;
    this.namespaceUtil = namespaceUtil;
    this.instanceConfigAuditUtil = instanceConfigAuditUtil;
    this.gson = gson;
  }

  /**
   * 查询配置
   *
   * @param appId                应用id
   * @param clusterName          集群名称
   * @param namespace            名称空间
   * @param dataCenter           数据中心
   * @param clientSideReleaseKey 客户端发布id
   * @param clientIp             客户端ip
   * @param messagesAsString     消息字符串
   * @param request              请求实体
   * @param response             响应实体
   * @return Apollo  配置信息
   * @throws IOException 如果发生输入或输出异常,抛出
   */
  @GetMapping(value = "/{appId}/{clusterName}/{namespace:.+}")
  public ApolloConfig queryConfig(@PathVariable String appId, @PathVariable String clusterName,
      @PathVariable String namespace,
      @RequestParam(value = "dataCenter", required = false) String dataCenter,
      @RequestParam(value = "releaseKey", defaultValue = "-1") String clientSideReleaseKey,
      @RequestParam(value = "ip", required = false) String clientIp,
      @RequestParam(value = "messages", required = false) String messagesAsString,
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    String originalNamespace = namespace;
    // 若 Namespace 名以 .properties 结尾，移除该结尾，并设置到 ApolloConfigNotification 中。例如 application.properties => application 。
    namespace = namespaceUtil.filterNamespaceName(namespace);
    // 获得标准化的 Namespace 名字。因为，客户端 Namespace 会填写错大小写。
    //fix the character case issue, such as FX.apollo <-> fx.apollo
    namespace = namespaceUtil.normalizeNamespace(appId, namespace);

    // 若 clientIp 未提交，从 Request 中获取。
    if (StringUtils.isBlank(clientIp)) {
      clientIp = tryToGetClientIp(request);
    }

    // 解析 messagesAsString 参数，创建 cc 对象。
    ApolloNotificationMessages clientMessages = transformMessages(messagesAsString);

    // 创建 Release 数组
    List<Release> releases = Lists.newLinkedList();
    // 获得 Namespace 对应的 Release 对象
    String appClusterNameLoaded = clusterName;
    if (!ConfigConsts.NO_APPID_PLACEHOLDER.equalsIgnoreCase(appId)) {
      // 获得 Release 对象
      Release currentAppRelease = configService.loadConfig(appId, clientIp, appId, clusterName,
          namespace, dataCenter, clientMessages);

      if (currentAppRelease != null) {
        // 添加到 Release 数组中。
        releases.add(currentAppRelease);
        // 获得 Release 对应的 Cluster 名字
        //we have cluster search process, so the cluster name might be overridden
        appClusterNameLoaded = currentAppRelease.getClusterName();
      }
    }

    // 若 Namespace 为关联类型，则获取关联的 Namespace 的 Release 对象
    //if namespace does not belong to this appId, should check if there is a public configuration
    if (!namespaceBelongsToAppId(appId, namespace)) {
      // 获得 Release 对象
      Release publicRelease = this.findPublicConfig(appId, clientIp, clusterName, namespace,
          dataCenter, clientMessages);
      // 添加到 Release 数组中
      if (!Objects.isNull(publicRelease)) {
        releases.add(publicRelease);
      }
    }

    // 若获得不到 Release ，返回状态码为 404 的响应
    if (releases.isEmpty()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          String.format(
              "Could not load configurations with appId: %s, clusterName: %s, namespace: %s",
              appId, clusterName, originalNamespace));
      Tracer.logEvent("Apollo.Config.NotFound",
          assembleKey(appId, clusterName, originalNamespace, dataCenter));
      return null;
    }

    // 记录 InstanceConfig
    auditReleases(appId, clusterName, dataCenter, clientIp, releases);

    // 计算 Config Service 的合并 ReleaseKey
    String mergedReleaseKey = releases.stream().map(Release::getReleaseKey)
        .collect(Collectors.joining(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR));

    // 对比 Client 的合并 Release Key 。若相等，说明没有改变，返回状态码为 302 的响应
    if (mergedReleaseKey.equals(clientSideReleaseKey)) {
      // Client side configuration is the same with server side, return 304
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      Tracer.logEvent("Apollo.Config.NotModified",
          assembleKey(appId, appClusterNameLoaded, originalNamespace, dataCenter));
      return null;
    }

    // 创建 ApolloConfig 对象
    ApolloConfig apolloConfig = new ApolloConfig(appId, appClusterNameLoaded, originalNamespace,
        mergedReleaseKey);
    // 合并 Release 的配置，并将结果设置到 ApolloConfig 中
    apolloConfig.setConfigurations(mergeReleaseConfigurations(releases));

    Tracer.logEvent("Apollo.Config.Found", assembleKey(appId, appClusterNameLoaded,
        originalNamespace, dataCenter));
    return apolloConfig;
  }

  /**
   * 名称空间是否属于应用id下
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return true, 属于，否则，false
   */
  private boolean namespaceBelongsToAppId(String appId, String namespaceName) {
    //  Namespace 非 'application' ，因为每个 App 都有
    if (Objects.equals(ConfigConsts.NAMESPACE_APPLICATION, namespaceName)) {
      return true;
    }

    //  App 编号非空
    if (ConfigConsts.NO_APPID_PLACEHOLDER.equalsIgnoreCase(appId)) {
      return false;
    }
    //  非当前应用id 下的 名称空间
    AppNamespace appNamespace = appNamespaceService.findByAppIdAndNamespace(appId, namespaceName);

    return appNamespace != null;
  }

  /**
   * 获得公用类型名称空间的 Release 对象
   *
   * @param clientAppId 使用公共配的应用id
   * @param namespace   名称空间
   * @param dataCenter  数据中心
   */
  private Release findPublicConfig(String clientAppId, String clientIp, String clusterName,
      String namespace, String dataCenter, ApolloNotificationMessages clientMessages) {
    // 获得公用类型的 AppNamespace 对象
    AppNamespace appNamespace = appNamespaceService.findPublicNamespaceByName(namespace);

    // 判断非当前 App 下的，那么就是关联类型。
    if (Objects.isNull(appNamespace) || Objects.equals(clientAppId, appNamespace.getAppId())) {
      return null;
    }

    String publicConfigAppId = appNamespace.getAppId();
    // 获得 Namespace 最新的 Release 对象
    return configService.loadConfig(clientAppId, clientIp, publicConfigAppId, clusterName,
        namespace, dataCenter, clientMessages);
  }

  /**
   * 合并多个 Release 的配置集合.较低的releasesId覆盖较高的releaseId
   *
   * @param releases 发布列表
   * @return 发布列表Map
   */
  Map<String, String> mergeReleaseConfigurations(List<Release> releases) {
    Map<String, String> result = Maps.newLinkedHashMap();
    // 反转 Release 数组，循环添加到 Map 中。
    for (Release release : Lists.reverse(releases)) {
      result.putAll(gson.fromJson(release.getConfigurations(), configurationTypeReference));
    }
    return result;
  }

  /**
   * 组装发布Key
   *
   * @param appId      应用id
   * @param cluster    集群名称
   * @param namespace  名称空间名称
   * @param dataCenter 数据中心
   * @return 组装后的发布key
   */
  private String assembleKey(String appId, String cluster, String namespace, String dataCenter) {
    List<String> keyParts = Lists.newArrayList(appId, cluster, namespace);
    if (StringUtils.isNotBlank(dataCenter)) {
      keyParts.add(dataCenter);
    }
    return String.join(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR, keyParts);
  }

  private void auditReleases(String appId, String cluster, String dataCenter, String clientIp,
      List<Release> releases) {
    if (Strings.isNullOrEmpty(clientIp)) {
      //no need to audit instance config when there is no ip
      return;
    }
    // 循环 Release 数组
    for (Release release : releases) {
      // 记录 InstanceConfig
      instanceConfigAuditUtil.audit(appId, cluster, dataCenter, clientIp, release.getAppId(),
          release.getClusterName(), release.getNamespaceName(), release.getReleaseKey());
    }
  }

  /**
   * 从请求中获取 IP
   *
   * @param request 请求对象
   * @return IP字符串
   */
  private String tryToGetClientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-FORWARDED-FOR");
    if (!Strings.isNullOrEmpty(forwardedFor)) {
      return X_FORWARDED_FOR_SPLITTER.splitToList(forwardedFor).get(0);
    }
    return request.getRemoteAddr();
  }

  /**
   * 解析 messagesAsString 参数，创建 ApolloNotificationMessages 对象
   *
   * @param messagesAsString 消息字符串
   * @return Apollo通知消息对象
   */
  ApolloNotificationMessages transformMessages(String messagesAsString) {
    ApolloNotificationMessages notificationMessages = null;
    if (StringUtils.isNotEmpty(messagesAsString)) {
      try {
        notificationMessages = gson.fromJson(messagesAsString, ApolloNotificationMessages.class);
      } catch (Throwable ex) {
        Tracer.logError(ex);
      }
    }
    return notificationMessages;
  }
}
