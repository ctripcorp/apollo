package com.ctrip.framework.apollo.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提交记录 dto
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CommitDTO extends BaseDTO {

  /**
   * 修改变更集
   */
  private String changeSets;
  /**
   * AppId
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
   * 发布说明
   */
  private String comment;


}
