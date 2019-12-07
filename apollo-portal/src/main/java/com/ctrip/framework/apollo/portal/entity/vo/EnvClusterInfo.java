package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.core.constants.Env;

import java.util.List;

public class EnvClusterInfo {
  private String env;
  private List<ClusterDTO> clusters;

  public EnvClusterInfo(String env) {
    this.env = Env.valueOf(env);
  }

  public String getEnv() {
    return env;
  }

  public void setEnv(String env) {
    this.env = Env.valueOf(env);
  }

  public List<ClusterDTO> getClusters() {
    return clusters;
  }

  public void setClusters(List<ClusterDTO> clusters) {
    this.clusters = clusters;
  }

}
