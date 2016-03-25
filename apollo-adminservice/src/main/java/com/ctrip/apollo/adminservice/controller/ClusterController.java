package com.ctrip.apollo.adminservice.controller;

import com.ctrip.apollo.biz.service.AdminConfigService;
import com.ctrip.apollo.core.dto.ClusterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cluster")
public class ClusterController {

    @Autowired
    private AdminConfigService adminConfigService;

    @RequestMapping("/app/{appId}")
    public List<ClusterDTO> findClustersByApp(@PathVariable long appId){
        return adminConfigService.findClustersByApp(appId);
    }
}
