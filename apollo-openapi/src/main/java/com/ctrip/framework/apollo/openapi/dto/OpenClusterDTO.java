package com.ctrip.framework.apollo.openapi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 开放的集群 Dto
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OpenClusterDTO extends BaseDTO {

  /**
   * 集群名称
   */
  private String name;
  /**
   * 应用Id
   */
  private String appId;

  @Override
  public String toString() {
    return "OpenClusterDTO{" +
        "name='" + name + '\'' +
        ", appId='" + appId + '\'' +
        ", dataChangeCreatedBy='" + dataChangeCreatedBy + '\'' +
        ", dataChangeLastModifiedBy='" + dataChangeLastModifiedBy + '\'' +
        ", dataChangeCreatedTime=" + dataChangeCreatedTime +
        ", dataChangeLastModifiedTime=" + dataChangeLastModifiedTime +
        '}';
  }
}
