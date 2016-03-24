package com.ctrip.apollo.portal.controller;

import com.ctrip.apollo.core.dto.Config4PortalDTO;
import com.ctrip.apollo.portal.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/configs")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @RequestMapping("/{appId}")
    public Config4PortalDTO detail(@PathVariable long appId, String env, long versionId) {

        return configService.loadConfig(appId, env, versionId);

    }
}
