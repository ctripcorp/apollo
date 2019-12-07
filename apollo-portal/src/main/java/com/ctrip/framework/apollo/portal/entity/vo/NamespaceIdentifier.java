package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.core.constants.Env;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.entity.model.Verifiable;

public class NamespaceIdentifier implements Verifiable {
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

  public String getEnv() {
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

  @Override
  public String toString() {
    return "NamespaceIdentifer{" +
        "appId='" + appId + '\'' +
        ", env='" + env + '\'' +
        ", clusterName='" + clusterName + '\'' +
        ", namespaceName='" + namespaceName + '\'' +
        '}';
  }
}
