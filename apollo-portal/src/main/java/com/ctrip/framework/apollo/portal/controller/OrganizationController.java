package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.vo.Organization;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 部门列表 Controller
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/organizations")
public class OrganizationController {

  private final PortalConfig portalConfig;

  public OrganizationController(final PortalConfig portalConfig) {
    this.portalConfig = portalConfig;
  }

  /**
   * 加载配置中的部门列表
   *
   * @return 部门列表信息
   */
  @RequestMapping
  public List<Organization> loadOrganization() {
    return portalConfig.organizations();
  }
}
