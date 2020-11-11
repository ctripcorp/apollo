package com.ctrip.framework.apollo.common.dto;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 灰度发布规则明细 Dto
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GrayReleaseRuleItemDTO {

  /**
   * 所有的ip表示
   */
  public static final String ALL_IP = "*";
  /**
   * 客户端appId
   */
  private String clientAppId;
  /**
   * 客户端IP列表
   */
  private Set<String> clientIpList;

  public GrayReleaseRuleItemDTO(String clientAppId) {
    this(clientAppId, Sets.newHashSet());
  }

  public boolean matches(String clientAppId, String clientIp) {
    return appIdMatches(clientAppId) && ipMatches(clientIp);
  }

  /**
   * 客户端应用id匹配
   *
   * @param clientAppId 客户端应用id
   * @return true, 匹配，否则，不匹配
   */
  private boolean appIdMatches(String clientAppId) {
    return this.clientAppId.equalsIgnoreCase(clientAppId);
  }

  /**
   * ip匹配
   *
   * @param clientIp 客户端ip
   * @return true, 匹配，否则，不匹配
   */
  private boolean ipMatches(String clientIp) {
    return this.clientIpList.contains(ALL_IP) || clientIpList.contains(clientIp);
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("clientAppId", clientAppId)
        .add("clientIpList", clientIpList).toString();
  }
}
