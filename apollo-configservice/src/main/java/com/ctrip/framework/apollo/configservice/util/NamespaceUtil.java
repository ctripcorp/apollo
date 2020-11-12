package com.ctrip.framework.apollo.configservice.util;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.configservice.service.AppNamespaceServiceWithCache;
import org.springframework.stereotype.Component;

/**
 * 名称空间工具类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Component
public class NamespaceUtil {

  private final AppNamespaceServiceWithCache appNamespaceServiceWithCache;

  public NamespaceUtil(final AppNamespaceServiceWithCache appNamespaceServiceWithCache) {
    this.appNamespaceServiceWithCache = appNamespaceServiceWithCache;
  }

  /**
   * 过滤后缀，获取名称空间名称
   *
   * @param namespaceName 名称空间名称
   * @return 名称空间名称
   */
  public String filterNamespaceName(String namespaceName) {
    //判断名称空间名称是否以指定后缀结尾
    if (namespaceName.toLowerCase().endsWith(".properties")) {
      int dotIndex = namespaceName.lastIndexOf(".");
      return namespaceName.substring(0, dotIndex);
    }

    return namespaceName;
  }

  /**
   * 获取标准化的名称空间名称
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 标准化的名称空间名称
   */
  public String normalizeNamespace(String appId, String namespaceName) {
    // 获取名称空间名称，不为空直接返回
    AppNamespace appNamespace = appNamespaceServiceWithCache
        .findByAppIdAndNamespace(appId, namespaceName);
    if (appNamespace != null) {
      return appNamespace.getName();
    }

    // 从应用名称空间服务缓存中获取名称空间
    appNamespace = appNamespaceServiceWithCache.findPublicNamespaceByName(namespaceName);
    if (appNamespace != null) {
      return appNamespace.getName();
    }

    return namespaceName;
  }
}
