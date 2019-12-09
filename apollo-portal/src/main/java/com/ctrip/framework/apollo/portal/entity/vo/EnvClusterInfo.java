package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.core.enums.Env;

import java.util.List;

public class EnvClusterInfo {
  private String env;
  private List<ClusterDTO> clusters;

  public EnvClusterInfo(Env env) {
    this.env = env.toString();
  }

  public Env getEnv() {
    return Env.valueOf(env);
  }

  public void setEnv(Env env) {
    this.env = env.toString();
  }

  public List<ClusterDTO> getClusters() {
    return clusters;
  }

  public void setClusters(List<ClusterDTO> clusters) {
    this.clusters = clusters;
  }

}
