package com.ctrip.framework.apollo.openapi.dto;

import java.util.Set;
import lombok.Data;

/**
 * 开放的环境集群 Dto
 */
@Data
public class OpenEnvClusterDTO {

  /**
   * 环境
   */
  private String env;
  /**
   * 集群列表
   */
  private Set<String> clusters;

  @Override
  public String toString() {
    return "OpenEnvClusterDTO{" +
        "env='" + env + '\'' +
        ", clusters=" + clusters +
        '}';
  }
}
