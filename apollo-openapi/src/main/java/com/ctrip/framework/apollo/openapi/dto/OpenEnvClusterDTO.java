package com.ctrip.framework.apollo.openapi.dto;

import java.util.Set;

public class OpenEnvClusterDTO {

  private String env;
  private Set<String> clusters;

  public String getEnv() {
    return env;
  }

  public void setEnv(String env) {
    this.env = env;
  }

  public Set<String> getClusters() {
    return clusters;
  }

  public void setClusters(Set<String> clusters) {
    this.clusters = clusters;
  }

  @Override
  public String toString() {
    return "OpenEnvClusterDTO{" +
        "env='" + env + '\'' +
        ", clusters=" + clusters +
        '}';
  }
}
