package com.ctrip.framework.apollo.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发布信息 Dto
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ReleaseDTO extends BaseDTO {

  /**
   * 主键Id
   */
  private Long id;
  /**
   * 发布的Key
   */
  private String releaseKey;
  /**
   * 发布名字
   */
  private String name;
  /**
   * AppID
   */
  private String appId;
  /**
   * 集群的名称
   */
  private String clusterName;
  /**
   * 命名空间的名称
   */
  private String namespaceName;
  /**
   * 发布配置
   */
  private String configurations;
  /**
   * 发布说明
   */
  private String comment;
  /**
   * 是否废弃
   */
  private Boolean isAbandoned;
}
