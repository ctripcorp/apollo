

package com.ctrip.framework.apollo.portal.spi.configuration;

import lombok.Data;

/**
 * Ldap映射属性集的描述
 *
 * @author wuzishu
 */
@Data
public class LdapMappingProperties {

  /**
   * ldap 用户 objectClass 配置
   */
  private String objectClass;

  /**
   * ldap 用户惟一 id，用来作为登录的 id
   */
  private String loginId;

  /**
   * ldap rdn key，可选项，如需启用group search需要配置
   */
  private String rdnKey;

  /**
   * ldap 用户名，用来作为显示名
   */
  private String userDisplayName;

  /**
   * ldap 邮箱属性
   */
  private String email;
}
