/*
 * Copyright (c) 2019 www.ceair.com Inc. All rights reserved.
 */

package com.ctrip.framework.apollo.portal.spi.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ldap继承属性集描述.
 *
 * @author wuzishu
 */
@Data
@ConfigurationProperties(prefix = "ldap")
public class LdapExtendProperties {

  /**
   * Ldap映射属性集的描述
   */
  private LdapMappingProperties mapping;
  /**
   * Ldap组属性集描述
   */
  private LdapGroupProperties group;
}