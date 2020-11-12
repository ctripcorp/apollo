package com.ctrip.framework.apollo.openapi.dto;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 开发的灰度发布规则 Dto
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OpenGrayReleaseRuleDTO extends BaseDTO {

  /**
   * 应用Id
   */
  private String appId;
  /**
   * 集群的名称
   */
  private String clusterName;
  /**
   * 名称空间名称
   */
  private String namespaceName;
  /**
   * 分支名称
   */
  private String branchName;
  /**
   * 灰度规则列表
   */
  private Set<OpenGrayReleaseRuleItemDTO> ruleItems;
}
