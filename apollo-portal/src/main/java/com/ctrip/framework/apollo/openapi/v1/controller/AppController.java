package com.ctrip.framework.apollo.openapi.v1.controller;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.openapi.dto.OpenEnvClusterDTO;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.service.ClusterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController("openapiAppController")
@RequestMapping("/openapi/v1")
public class AppController {

  @Autowired
  private PortalSettings portalSettings;
  @Autowired
  private ClusterService clusterService;

  @RequestMapping(value = "/apps/{appId}/envclusters", method = RequestMethod.GET)
  public List<OpenEnvClusterDTO> loadEnvClusterInfo(@PathVariable String appId){

    List<OpenEnvClusterDTO> envClusters = new LinkedList<>();

    List<Env> envs = portalSettings.getActiveEnvs();
    for (Env env : envs) {
      OpenEnvClusterDTO envCluster = new OpenEnvClusterDTO();

      envCluster.setEnv(env.name());
      List<ClusterDTO> clusterDTOs = clusterService.findClusters(env, appId);
      envCluster.setClusters(BeanUtils.toPropertySet("name", clusterDTOs));

      envClusters.add(envCluster);
    }

    return envClusters;

  }

}
