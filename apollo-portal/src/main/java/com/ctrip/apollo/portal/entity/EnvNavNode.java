package com.ctrip.apollo.portal.entity;

import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.core.enums.Env;

import java.util.List;

public class EnvNavNode {
  private Env env;
  private List<ClusterDTO> clusters;

  public EnvNavNode(Env env){
    this.env = env;
  }

  public Env getEnv() {
    return env;
  }

  public void setEnv(Env env) {
    this.env = env;
  }

  public List<ClusterDTO> getClusters() {
    return clusters;
  }

  public void setClusters(List<ClusterDTO> clusters) {
    this.clusters = clusters;
  }

}
