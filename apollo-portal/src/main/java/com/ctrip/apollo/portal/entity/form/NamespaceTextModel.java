package com.ctrip.apollo.portal.entity.form;


import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.utils.StringUtils;

public class NamespaceTextModel implements FormModel{

  private String appId;
  private String env;
  private String clusterName;
  private String namespaceName;
  private int namespaceId;
  private String configText;
  private String modifyBy;
  private String comment;

  @Override
  public boolean isInvalid(){
    return StringUtils.isContainEmpty(appId, env, clusterName, namespaceName, configText, modifyBy) || namespaceId <= 0;
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

  public int getNamespaceId() {
    return namespaceId;
  }

  public void setNamespaceId(int namespaceId) {
    this.namespaceId = namespaceId;
  }

  public String getConfigText() {
    return configText;
  }

  public void setConfigText(String configText) {
    this.configText = configText;
  }

  public String getModifyBy() {
    return modifyBy;
  }

  public void setModifyBy(String modifyBy) {
    this.modifyBy = modifyBy;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
