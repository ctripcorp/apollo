package com.ctrip.framework.apollo.portal.spi.configuration;

import com.ctrip.framework.apollo.portal.service.RoleInitializationService;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.spi.defaultimpl.DefaultRoleInitializationService;
import com.ctrip.framework.apollo.portal.spi.defaultimpl.DefaultRolePermissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 角色配置
 *
 * @author Timothy Liu(timothy.liu@cvte.com)
 */
@Configuration
public class RoleConfiguration {

  /**
   * 默认的角色初始化服务
   *
   * @return 角色初始化服务Bean
   */
  @Bean
  public RoleInitializationService roleInitializationService() {
    return new DefaultRoleInitializationService();
  }

  /**
   * 默认的角色权限服务
   *
   * @return 默认的角色权限服务Bean
   */
  @Bean
  public RolePermissionService rolePermissionService() {
    return new DefaultRolePermissionService();
  }
}
