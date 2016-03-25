package com.ctrip.apollo.portal.controller;

import com.ctrip.apollo.core.dto.VersionDTO;
import com.ctrip.apollo.portal.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/version")
public class VersionController {

    @Autowired
    private VersionService versionService;

    @RequestMapping("/{appId}/{env}")
    public List<VersionDTO> versions(@PathVariable long appId, @PathVariable String env){
        return versionService.findVersionsByApp(appId, env);
    }
}
