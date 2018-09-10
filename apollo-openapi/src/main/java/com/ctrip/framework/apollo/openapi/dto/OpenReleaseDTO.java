package com.ctrip.framework.apollo.openapi.dto;

import java.util.Map;

public class OpenReleaseDTO extends BaseDTO {

  private String appId;

  private String clusterName;

  private String namespaceName;

  private String name;

  private Map<String, String> configurations;

  private String comment;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
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

  public Map<String, String> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(Map<String, String> configurations) {
    this.configurations = configurations;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
