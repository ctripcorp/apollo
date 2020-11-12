package com.ctrip.framework.apollo.openapi.dto;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 开发的发布信息 Dto
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OpenReleaseDTO extends BaseDTO {

  /**
   * 主键id
   */
  private long id;
  /**
   * 应用id
   */
  private String appId;
  /**
   * 集群名称
   */
  private String clusterName;
  /**
   * 名称空间名称
   */
  private String namespaceName;
  /**
   * 发布名字
   */
  private String name;
  /**
   * 发布配置
   */
  private Map<String, String> configurations;
  /**
   * 发布说明
   */
  private String comment;

  @Override
  public String toString() {
    return "OpenReleaseDTO{" +
        "id=" + id +
        ", appId='" + appId + '\'' +
        ", clusterName='" + clusterName + '\'' +
        ", namespaceName='" + namespaceName + '\'' +
        ", name='" + name + '\'' +
        ", configurations=" + configurations +
        ", comment='" + comment + '\'' +
        ", dataChangeCreatedBy='" + dataChangeCreatedBy + '\'' +
        ", dataChangeLastModifiedBy='" + dataChangeLastModifiedBy + '\'' +
        ", dataChangeCreatedTime=" + dataChangeCreatedTime +
        ", dataChangeLastModifiedTime=" + dataChangeLastModifiedTime +
        '}';
  }
}
