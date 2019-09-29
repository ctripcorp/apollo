package com.ctrip.framework.apollo.openapi.dto;

public class OpenClusterDTO extends BaseDTO {
  private long id;

  private String name;

  private String appId;

  private long associateClusterId;

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
  
  public long getAssociateClusterId() {
    return associateClusterId;
  }

  public void setAssociateClusterId(long associateClusterId) {
    this.associateClusterId = associateClusterId;
  }
}
