package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.portal.components.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.vo.Organization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/organizations")
public class OrganizationController {

  @Autowired
  private PortalConfig portalConfig;


  @RequestMapping
  public List<Organization> loadOrganization() {
    return portalConfig.organizations();
  }
}
