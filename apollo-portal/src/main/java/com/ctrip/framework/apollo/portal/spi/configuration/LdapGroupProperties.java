

package com.ctrip.framework.apollo.portal.spi.configuration;

/**
 * the LdapGroupProperties description.
 *
 * @author wuzishu
 */
public class LdapGroupProperties {

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
