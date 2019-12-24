package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.environment.Env;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/envs")
public class EnvController {

  private final PortalSettings portalSettings;

  public EnvController(final PortalSettings portalSettings) {
    this.portalSettings = portalSettings;
  }

  @GetMapping
  public List<String> envs() {
    List<String> environments = new ArrayList<>();
    for(Env env : portalSettings.getActiveEnvs()) {
      environments.add(env.toString());
    }
    return environments;
  }

}
