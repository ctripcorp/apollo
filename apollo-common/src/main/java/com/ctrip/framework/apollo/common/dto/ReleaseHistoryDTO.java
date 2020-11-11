package com.ctrip.framework.apollo.common.dto;


import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ReleaseHistoryDTO extends BaseDTO {

  /**
   * 主键id
   */
  private Long id;
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
   * 分支的名称
   */
  private String branchName;
  /**
   * 关联的Release Id
   */
  private Long releaseId;
  /**
   * 前一次发布的ReleaseId
   */
  private Long previousReleaseId;
  /**
   * 发布类型，0: 普通发布，1: 回滚，2: 灰度发布，3: 灰度规则更新，4: 灰度合并回主分支发布，5: 主分支发布灰度自动发布，6: 主分支回滚灰度自动发布，7: 放弃灰度
   */
  private Integer operation;
  /**
   * 发布上下文信息
   */
  private Map<String, Object> operationContext;
}