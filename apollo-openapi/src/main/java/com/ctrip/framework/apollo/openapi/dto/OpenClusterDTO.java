package com.ctrip.framework.apollo.openapi.dto;

public class OpenClusterDTO extends BaseDTO {

  private String name;

  private String appId;

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
}
