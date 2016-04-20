package com.ctrip.apollo.core.dto;

public class ReleaseDTO{
  private long id;

  private String name;

  private String appId;

  private String clusterName;

  private String namespaceName;

  private String configurations;

  private String comment;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getAppId() {
    return appId;
  }

  public String getClusterName() {
    return clusterName;
  }

  public String getComment() {
    return comment;
  }

  public String getConfigurations() {
    return configurations;
  }

  public String getName() {
    return name;
  }

  public String getNamespaceName() {
    return namespaceName;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setConfigurations(String configurations) {
    this.configurations = configurations;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNamespaceName(String namespaceName) {
    this.namespaceName = namespaceName;
  }

}
