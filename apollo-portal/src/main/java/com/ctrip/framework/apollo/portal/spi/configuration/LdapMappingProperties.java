

package com.ctrip.framework.apollo.portal.spi.configuration;

/**
 * the LdapMappingProperties description.
 *
 * @author wuzishu
 */
public class LdapMappingProperties {

  /**
   * user ldap objectClass
   */
  private String objectClass;

  /**
   * user login Id
   */
  private String loginId;

  /**
   * user rdn key
   */
  private String rdnKey;

  /**
   * user display name
   */
  private String userDisplayName;

  /**
   * email
   */
  private String email;

  public String getObjectClass() {
    return objectClass;
  }

  public void setObjectClass(String objectClass) {
    this.objectClass = objectClass;
  }

  public String getLoginId() {
    return loginId;
  }

  public void setLoginId(String loginId) {
    this.loginId = loginId;
  }

  public String getRdnKey() {
    return rdnKey;
  }

  public void setRdnKey(String rdnKey) {
    this.rdnKey = rdnKey;
  }

  public String getUserDisplayName() {
    return userDisplayName;
  }

  public void setUserDisplayName(String userDisplayName) {
    this.userDisplayName = userDisplayName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
