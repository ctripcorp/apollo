package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.component.PortalSettings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/envs")
public class EnvController {

  @Autowired
  private PortalSettings portalSettings;

  @RequestMapping(value = "", method = RequestMethod.GET)
  public List<Env> envs() {
    return portalSettings.getActiveEnvs();
  }

}
