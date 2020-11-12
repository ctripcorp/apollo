package com.ctrip.framework.apollo.configservice.util;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.configservice.service.AppNamespaceServiceWithCache;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * 监听Key的工具类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Component
public class WatchKeysUtil {

  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
  /**
   * 缓存 AppNamespace 的 Service 实现类
   */
  private final AppNamespaceServiceWithCache appNamespaceService;

  public WatchKeysUtil(final AppNamespaceServiceWithCache appNamespaceService) {
    this.appNamespaceService = appNamespaceService;
  }

  /**
   * 组装所有的 Watch Key Multimap 。其中 Key 为 Namespace 的名字，Value 为 Watch Key 集合。
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @param namespace   名称空间名称
   * @param dataCenter  数据中心的集群名称
   * @return WatchKey的多Map对象c
   */
  public Set<String> assembleAllWatchKeys(String appId, String clusterName, String namespace,
      String dataCenter) {
    Multimap<String, String> watchedKeysMap = assembleAllWatchKeys(appId, clusterName,
        Sets.newHashSet(namespace), dataCenter);
    return Sets.newHashSet(watchedKeysMap.get(namespace));
  }

  /**
   * 组装所有的 Watch Key Multimap 。其中 Key 为 Namespace 的名字，Value 为 Watch Key 集合。
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @param namespaces  名称空间名称数组
   * @param dataCenter  数据中心的集群名称
   * @return WatchKey的多Map对象c
   */
  public Multimap<String, String> assembleAllWatchKeys(String appId, String clusterName,
      Set<String> namespaces, String dataCenter) {
    // 组装 Watch Key Multimap
    Multimap<String, String> watchedKeysMap = assembleWatchKeys(appId, clusterName, namespaces,
        dataCenter);

    //Every app has an 'application' namespace
    // 如果不是仅监听 'application' 名称空间 ，处理其关联来的 Namespace 。
    if (!(namespaces.size() == 1 && namespaces.contains(ConfigConsts.NAMESPACE_APPLICATION))) {
      // 获得属于该 App 的 Namespace 的名字的集合
      Set<String> namespacesBelongToAppId = namespacesBelongToAppId(appId, namespaces);
      // 获得关联来的 Namespace 的名字的集合
      Set<String> publicNamespaces = Sets.difference(namespaces, namespacesBelongToAppId);
      // 添加到 Watch Key Multimap 中
      //Listen on more namespaces if it's a public namespace
      if (!publicNamespaces.isEmpty()) {
        watchedKeysMap
            .putAll(findPublicConfigWatchKeys(appId, clusterName, publicNamespaces, dataCenter));
      }
    }

    return watchedKeysMap;
  }

  /**
   * 获得 名称空间类型为public对应的 Watch Key Multimap
   * <p>
   * 重要：要求非当前 App 的 Namespace
   *
   * @param applicationId 应用id
   * @param clusterName   集群名称
   * @param namespaces    名称空间集合
   * @param dataCenter    数据中心
   * @return Watch Key Map
   */
  private Multimap<String, String> findPublicConfigWatchKeys(String applicationId,
      String clusterName,
      Set<String> namespaces,
      String dataCenter) {
    Multimap<String, String> watchedKeysMap = HashMultimap.create();
    // 获得名称空间类型为 public 的 AppNamespace 数组
    List<AppNamespace> appNamespaces = appNamespaceService.findPublicNamespacesByNames(namespaces);

    // 获得名称空间类型为public 的 AppNamespace 数组
    for (AppNamespace appNamespace : appNamespaces) {
      // 排除非关联类型的 Namespace
      //check whether the namespace's appId equals to current one
      if (Objects.equals(applicationId, appNamespace.getAppId())) {
        continue;
      }

      String publicConfigAppId = appNamespace.getAppId();
      // 组装指定Namespace的 Watch Key 数组
      watchedKeysMap.putAll(appNamespace.getName(), assembleWatchKeys(publicConfigAppId,
          clusterName, appNamespace.getName(), dataCenter));
    }

    return watchedKeysMap;
  }

  /**
   * 拼接 Watch Key
   *
   * @param appId     应用id
   * @param cluster   集群名称
   * @param namespace 名称空间
   * @return Watch Key
   */
  private String assembleKey(String appId, String cluster, String namespace) {
    return STRING_JOINER.join(appId, cluster, namespace);
  }

  /**
   * 组装WatchKey列表
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @param namespace   名称空间
   * @param dataCenter  数据中心
   * @return 组装好的WatchKey列表
   */
  private Set<String> assembleWatchKeys(String appId, String clusterName, String namespace,
      String dataCenter) {
    // 应用id为空，返回空
    if (ConfigConsts.NO_APPID_PLACEHOLDER.equalsIgnoreCase(appId)) {
      return Collections.emptySet();
    }
    Set<String> watchedKeys = Sets.newHashSet();

    // 监视指定的群集配置变改
    if (!Objects.equals(ConfigConsts.CLUSTER_NAME_DEFAULT, clusterName)) {
      watchedKeys.add(assembleKey(appId, clusterName, namespace));
    }

    // 监视数据中心配置更改
    if (!Strings.isNullOrEmpty(dataCenter) && !Objects.equals(dataCenter, clusterName)) {
      watchedKeys.add(assembleKey(appId, dataCenter, namespace));
    }

    // 监视默认群集配置更改
    watchedKeys.add(assembleKey(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace));

    return watchedKeys;
  }

  /**
   * 组装WatchKey
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @param namespaces  名称空间列表
   * @param dataCenter  数据中心
   * @return 组装后的发布key
   */
  private Multimap<String, String> assembleWatchKeys(String appId, String clusterName,
      Set<String> namespaces, String dataCenter) {
    Multimap<String, String> watchedKeysMap = HashMultimap.create();

    // 向名称空间添加WatchKey
    for (String namespace : namespaces) {
      watchedKeysMap.putAll(namespace, assembleWatchKeys(appId, clusterName, namespace,
          dataCenter));
    }

    return watchedKeysMap;
  }

  /**
   * 名称空间是否属于应用id下
   *
   * @param appId      应用id
   * @param namespaces 名称空间列表
   * @return true, 属于，否则，false
   */
  private Set<String> namespacesBelongToAppId(String appId, Set<String> namespaces) {
    if (ConfigConsts.NO_APPID_PLACEHOLDER.equalsIgnoreCase(appId)) {
      return Collections.emptySet();
    }
    //  非当前应用id 下的 名称空间
    List<AppNamespace> appNamespaces =
        appNamespaceService.findByAppIdAndNamespaces(appId, namespaces);

    if (CollectionUtils.isEmpty(appNamespaces)) {
      return Collections.emptySet();
    }

    return appNamespaces.stream().map(AppNamespace::getName).collect(Collectors.toSet());
  }
}
