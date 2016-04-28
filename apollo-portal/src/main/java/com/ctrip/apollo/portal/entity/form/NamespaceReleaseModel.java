package com.ctrip.apollo.portal.entity.form;


import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.utils.StringUtils;

public class NamespaceReleaseModel implements Verifiable {

  private String appId;
  private String env;
  private String clusterName;
  private String namespaceName;
  private String releaseBy;
  private String releaseComment;

  @Override
  public boolean isInvalid() {
    return StringUtils.isContainEmpty(appId, env, clusterName, namespaceName, releaseBy);
  }

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

  public String getReleaseBy() {
    return releaseBy;
  }

  public void setReleaseBy(String releaseBy) {
    this.releaseBy = releaseBy;
  }

  public String getReleaseComment() {
    return releaseComment;
  }

  public void setReleaseComment(String releaseComment) {
    this.releaseComment = releaseComment;
  }

}
