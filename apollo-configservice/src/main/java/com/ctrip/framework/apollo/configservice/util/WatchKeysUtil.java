package com.ctrip.framework.apollo.configservice.util;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.biz.service.AppNamespaceService;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.core.ConfigConsts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Component
public class WatchKeysUtil {
  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
  @Autowired
  private AppNamespaceService appNamespaceService;

  public Set<String> assembleAllWatchKeys(String appId, String clusterName, String namespace,
                                          String dataCenter) {
    return assembleAllWatchKeys(appId, clusterName, Sets.newHashSet(namespace), dataCenter);
  }

  public Set<String> assembleAllWatchKeys(String appId, String clusterName, Set<String> namespaces,
                                          String dataCenter) {
    Set<String> watchedKeys = assembleWatchKeys(appId, clusterName, namespaces, dataCenter);

    Set<String> namespacesBelongToAppId = namespacesBelongToAppId(appId, namespaces);
    Set<String> publicNamespaces = Sets.difference(namespaces, namespacesBelongToAppId);

    //Listen on more namespaces if it's a public namespace
    if (!publicNamespaces.isEmpty()) {
      watchedKeys
          .addAll(findPublicConfigWatchKeys(appId, clusterName, publicNamespaces, dataCenter));

    }

    return watchedKeys;
  }

  private Set<String> findPublicConfigWatchKeys(String applicationId, String clusterName,
                                                Set<String> namespaces, String dataCenter) {
    Set<String> watchedKeys = Sets.newHashSet();
    List<AppNamespace> appNamespaces = appNamespaceService.findPublicNamespacesByNames(namespaces);

    for (AppNamespace appNamespace : appNamespaces) {
      //check whether the namespace's appId equals to current one
      if (Objects.equals(applicationId, appNamespace.getAppId())) {
        continue;
      }

      String publicConfigAppId = appNamespace.getAppId();

      watchedKeys.addAll(
          assembleWatchKeys(publicConfigAppId, clusterName, appNamespace.getName(), dataCenter));
    }

    return watchedKeys;
  }

  private Set<String> assembleKey(String appId, String cluster, Set<String> namespaces) {
    return FluentIterable.from(namespaces).transform(
        namespace -> STRING_JOINER.join(appId, cluster, namespace)).toSet();
  }

  private Set<String> assembleWatchKeys(String appId, String clusterName, String namespace,
                                        String dataCenter) {
    return assembleWatchKeys(appId, clusterName, Sets.newHashSet(namespace), dataCenter);
  }

  private Set<String> assembleWatchKeys(String appId, String clusterName, Set<String> namespaces,
                                        String dataCenter) {
    Set<String> watchedKeys = Sets.newHashSet();

    //watch specified cluster config change
    if (!Objects.equals(ConfigConsts.CLUSTER_NAME_DEFAULT, clusterName)) {
      watchedKeys.addAll(assembleKey(appId, clusterName, namespaces));
    }

    //watch data center config change
    if (!Strings.isNullOrEmpty(dataCenter) && !Objects.equals(dataCenter, clusterName)) {
      watchedKeys.addAll(assembleKey(appId, dataCenter, namespaces));
    }

    //watch default cluster config change
    watchedKeys.addAll(assembleKey(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespaces));

    return watchedKeys;
  }

  private Set<String> namespacesBelongToAppId(String appId, Set<String> namespaces) {
    List<AppNamespace> appNamespaces =
        appNamespaceService.findByAppIdAndNamespaces(appId, namespaces);

    if (appNamespaces == null || appNamespaces.isEmpty()) {
      return Collections.emptySet();
    }

    return FluentIterable.from(appNamespaces).transform(AppNamespace::getName).toSet();
  }
}
