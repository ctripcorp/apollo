package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.vo.PageSetting;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PageSettingController {

  private final PortalConfig portalConfig;

  public PageSettingController(final PortalConfig portalConfig) {
    this.portalConfig = portalConfig;
  }

  @GetMapping("/page-settings")
  public PageSetting getPageSetting() {
    PageSetting setting = new PageSetting();

    setting.setWikiAddress(portalConfig.wikiAddress());
    setting.setCanAppAdminCreatePrivateNamespace(portalConfig.canAppAdminCreatePrivateNamespace());

    return setting;
  }

}
