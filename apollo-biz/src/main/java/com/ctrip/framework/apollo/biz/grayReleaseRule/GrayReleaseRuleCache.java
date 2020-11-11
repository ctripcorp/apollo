package com.ctrip.framework.apollo.biz.grayReleaseRule;

import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleItemDTO;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 灰度发布规则缓存
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@AllArgsConstructor
@Data
public class GrayReleaseRuleCache implements Comparable<GrayReleaseRuleCache> {

  /**
   * 规则id
   */
  private long ruleId;
  /**
   * 分支名称
   */
  private String branchName;
  /**
   * 名称空间名称
   */
  private String namespaceName;
  /**
   * 发布id
   */
  private long releaseId;
  /**
   * 加载的版本
   */
  private long loadVersion;
  /**
   * 分支的状态
   */
  private int branchStatus;
  /**
   * 灰度发布的规则配置项列表
   */
  private Set<GrayReleaseRuleItemDTO> ruleItems;

  /**
   * 配置项匹配
   *
   * @param clientAppId 客户端应用id
   * @param clientIp    客户端ip
   * @return 匹配, true, 否则，false
   */
  public boolean matches(String clientAppId, String clientIp) {
    for (GrayReleaseRuleItemDTO ruleItem : ruleItems) {
      if (ruleItem.matches(clientAppId, clientIp)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int compareTo(GrayReleaseRuleCache that) {
    return Long.compare(this.ruleId, that.ruleId);
  }
}
