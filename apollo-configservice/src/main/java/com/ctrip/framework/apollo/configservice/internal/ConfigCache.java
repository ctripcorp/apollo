package com.ctrip.framework.apollo.configservice.internal;

import com.ctrip.framework.apollo.biz.entity.Release;

public interface ConfigCache {

  Release get(String appId, String clusterName, String namespaceName);

  void invalidate(String appId, String clusterName, String namespaceName);

  void invalidate(String key);

  void clear();

}
