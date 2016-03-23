package com.ctrip.apollo.core.serivce;

public class ApolloService {

  private String appName;

  private String instanceId;

  private String homepageUrl;

  public String getAppName() {
    return appName;
  }

  public String getHomepageUrl() {
    return homepageUrl;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public void setHomepageUrl(String homepageUrl) {
    this.homepageUrl = homepageUrl;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }
}
