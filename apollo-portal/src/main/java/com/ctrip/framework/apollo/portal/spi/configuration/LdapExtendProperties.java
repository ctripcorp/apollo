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
