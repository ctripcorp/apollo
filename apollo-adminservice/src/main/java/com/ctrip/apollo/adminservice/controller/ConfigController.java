package com.ctrip.apollo.adminservice.controller;

import com.ctrip.apollo.biz.service.AdminConfigService;
import com.ctrip.apollo.core.Constants;
import com.ctrip.apollo.core.dto.Config4PortalDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/configs")
public class ConfigController {

    @Resource(name = "adminConfigService")
    private AdminConfigService adminConfigService;

    @RequestMapping("/{appId}/{versionId}")
    public Config4PortalDTO detail(@PathVariable long appId, @PathVariable long versionId) {
        if (versionId == Constants.LASTEST_VERSION_ID) {
            return adminConfigService.loadLatestConfig(appId);
        } else if (versionId > 0) {
            return adminConfigService.loadReleaseConfig(appId, versionId);
        } else {
            return null;
        }
    }


}
