package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.vo.Organization;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/organizations")
public class OrganizationController {

  private final PortalConfig portalConfig;

  public OrganizationController(final PortalConfig portalConfig) {
    this.portalConfig = portalConfig;
  }


  @RequestMapping
  public List<Organization> loadOrganization() {
    return portalConfig.organizations();
  }
}
