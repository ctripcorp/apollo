package com.ctrip.framework.apollo.biz;

import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.entity.ServerConfig;
import com.ctrip.framework.apollo.common.entity.AppNamespace;

public class MockBeanFactory {

  public static Namespace mockNamespace(String appId, String clusterName, String namespaceName) {
    Namespace instance = new Namespace();

    instance.setAppId(appId);
    instance.setClusterName(clusterName);
    instance.setNamespaceName(namespaceName);

    return instance;
  }

  public static AppNamespace mockAppNamespace(String appId, String name, boolean isPublic) {
    AppNamespace instance = new AppNamespace();

    instance.setAppId(appId);
    instance.setName(name);
    instance.setPublic(isPublic);

    return instance;
  }

  public static ServerConfig mockServerConfig(String key, String value, String cluster) {
    ServerConfig instance = new ServerConfig();

    instance.setKey(key);
    instance.setValue(value);
    instance.setCluster(cluster);

    return instance;
  }

  public static Release mockRelease(long releaseId, String releaseKey, String appId,
                                    String clusterName, String groupName, String configurations) {
    Release instance = new Release();

    instance.setId(releaseId);
    instance.setReleaseKey(releaseKey);
    instance.setAppId(appId);
    instance.setClusterName(clusterName);
    instance.setNamespaceName(groupName);
    instance.setConfigurations(configurations);

    return instance;
  }

}
