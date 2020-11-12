package com.ctrip.framework.apollo.common.dto;


import com.google.common.collect.Sets;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 灰度发布 dto
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class GrayReleaseRuleDTO extends BaseDTO {

  /**
   * appId
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
   * 分支名称
   */
  private String branchName;
  /**
   * 灰度规则
   */
  @Setter
  private Set<GrayReleaseRuleItemDTO> ruleItems;
  /**
   * 灰度对应的release
   */
  @Setter
  private Long releaseId;

  public GrayReleaseRuleDTO(String appId, String clusterName, String namespaceName,
      String branchName) {
    this.appId = appId;
    this.clusterName = clusterName;
    this.namespaceName = namespaceName;
    this.branchName = branchName;
    this.ruleItems = Sets.newHashSet();
  }

  public void addRuleItem(GrayReleaseRuleItemDTO ruleItem) {
    this.ruleItems.add(ruleItem);
  }
}

