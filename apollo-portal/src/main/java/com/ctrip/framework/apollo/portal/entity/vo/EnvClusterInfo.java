package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.portal.environment.Env;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 环境集群信息
 */
public class EnvClusterInfo {

  /**
   * 环境
   */
  private String env;
  /**
   * 集群列表
   */
  @Getter
  @Setter
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
}
