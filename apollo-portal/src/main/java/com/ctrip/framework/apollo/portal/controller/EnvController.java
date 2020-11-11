package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.environment.Env;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 环境信息
 */
@RestController
@RequestMapping("/envs")
public class EnvController {

  private final PortalSettings portalSettings;

  public EnvController(final PortalSettings portalSettings) {
    this.portalSettings = portalSettings;
  }

  /**
   * 获取环境列表
   *
   * @return 环境列表信息
   */
  @GetMapping
  public List<String> envs() {
    List<String> environments = new ArrayList<>();
    for (Env env : portalSettings.getActiveEnvs()) {
      environments.add(env.toString());
    }
    return environments;
  }

}
