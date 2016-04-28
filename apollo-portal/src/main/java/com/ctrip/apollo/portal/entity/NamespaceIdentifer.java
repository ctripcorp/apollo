package com.ctrip.apollo.portal.entity;

import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.entity.form.Verifiable;

public class NamespaceIdentifer implements Verifiable{
  private String appId;
  private String env;
  private String clusterName;
  private String namespaceName;

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public Env getEnv() {
    return Env.valueOf(env);
  }

  public void setEnv(String env) {
    this.env = env;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getNamespaceName() {
    return namespaceName;
  }

  public void setNamespaceName(String namespaceName) {
    this.namespaceName = namespaceName;
  }


  @Override
  public boolean isInvalid() {
    return StringUtils.isContainEmpty(env, clusterName, namespaceName);
  }
}
