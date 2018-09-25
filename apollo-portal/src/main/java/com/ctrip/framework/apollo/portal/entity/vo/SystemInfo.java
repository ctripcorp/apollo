package com.ctrip.framework.apollo.portal.entity.vo;

import com.google.common.collect.Lists;
import java.util.List;

public class SystemInfo {

  private String version;

  private List<EnvironmentInfo> environments = Lists.newLinkedList();

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public List<EnvironmentInfo> getEnvironments() {
    return environments;
  }

  public void addEnvironment(EnvironmentInfo environment) {
    this.environments.add(environment);
  }
}
