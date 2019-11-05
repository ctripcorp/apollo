package com.ctrip.framework.apollo.openapi.dto;

import java.util.Map;

public class OpenReleaseDTO extends BaseDTO {

  private long id;

  private String appId;

  private String clusterName;

  private String namespaceName;

  private String name;

  private Map<String, String> configurations;

  private String comment;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

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

  @Override
  public String toString() {
    return "OpenReleaseDTO{" +
        "id=" + id +
        ", appId='" + appId + '\'' +
        ", clusterName='" + clusterName + '\'' +
        ", namespaceName='" + namespaceName + '\'' +
        ", name='" + name + '\'' +
        ", configurations=" + configurations +
        ", comment='" + comment + '\'' +
        ", dataChangeCreatedBy='" + dataChangeCreatedBy + '\'' +
        ", dataChangeLastModifiedBy='" + dataChangeLastModifiedBy + '\'' +
        ", dataChangeCreatedTime=" + dataChangeCreatedTime +
        ", dataChangeLastModifiedTime=" + dataChangeLastModifiedTime +
        '}';
  }
}
