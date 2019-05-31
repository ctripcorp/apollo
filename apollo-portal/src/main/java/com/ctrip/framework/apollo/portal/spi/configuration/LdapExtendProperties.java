/*
 * Copyright (c) 2019 www.ceair.com Inc. All rights reserved.
 */

package com.ctrip.framework.apollo.portal.spi.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * the LdapExtendProperties description.
 *
 * @author wuzishu
 */
@ConfigurationProperties(prefix = "ldap")
public class LdapExtendProperties {

  private LdapMappingProperties mapping;
  private LdapGroupProperties group;

  public LdapMappingProperties getMapping() {
    return mapping;
  }

  public void setMapping(LdapMappingProperties mapping) {
    this.mapping = mapping;
  }

  public LdapGroupProperties getGroup() {
    return group;
  }

  public void setGroup(LdapGroupProperties group) {
    this.group = group;
  }
}
class LdapMappingProperties{

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
class LdapGroupProperties{

  /**
   * group search base
   */
  private String groupBase;

  /**
   * group search filter
   */
  private String groupSearch;

  /**
   * group membership prop
   */
  private String groupMembership;

  public String getGroupBase() {
    return groupBase;
  }

  public void setGroupBase(String groupBase) {
    this.groupBase = groupBase;
  }

  public String getGroupSearch() {
    return groupSearch;
  }

  public void setGroupSearch(String groupSearch) {
    this.groupSearch = groupSearch;
  }

  public String getGroupMembership() {
    return groupMembership;
  }

  public void setGroupMembership(String groupMembership) {
    this.groupMembership = groupMembership;
  }
}
