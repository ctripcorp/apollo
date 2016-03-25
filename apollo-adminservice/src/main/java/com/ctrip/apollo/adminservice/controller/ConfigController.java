package com.ctrip.apollo.adminservice.controller;

import com.ctrip.apollo.biz.service.AdminConfigService;
import com.ctrip.apollo.core.dto.ConfigItemDTO;
import com.ctrip.apollo.core.dto.ReleaseSnapshotDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/configs")
public class ConfigController {

    @Resource(name = "adminConfigService")
    private AdminConfigService adminConfigService;


    @RequestMapping("/release/{releaseId}")
    public List<ReleaseSnapshotDTO> getRelaseSnapshot(@PathVariable long releaseId){
        return adminConfigService.findReleaseSnapshotByReleaseId(releaseId);
    }

    @RequestMapping("/latest")
    public List<ConfigItemDTO> findConfigItemsByClusters(@RequestParam(value = "clusterIds") List<Long> clusterIds){
        return adminConfigService.findConfigItemsByClusters(clusterIds);
    }


}
