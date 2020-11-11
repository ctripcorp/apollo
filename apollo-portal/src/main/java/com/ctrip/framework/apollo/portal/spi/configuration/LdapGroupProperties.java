

package com.ctrip.framework.apollo.portal.spi.configuration;

import lombok.Data;

/**
 * Ldap组属性集描述.
 *
 * @author wuzishu
 */
@Data
public class LdapGroupProperties {

  /**
   * 组搜索基（group search base）
   */
  private String groupBase;

  /**
   * 组搜索筛选器(group search filter)
   */
  private String groupSearch;

  /**
   * 组成员资格属性（group membership prop）
   */
  private String groupMembership;
}
