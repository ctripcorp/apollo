package com.ctrip.framework.apollo.openapi.v1.controller;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.openapi.dto.OpenAppDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenEnvClusterDTO;
import com.ctrip.framework.apollo.openapi.util.OpenApiBeanUtils;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.AppService;
import com.ctrip.framework.apollo.portal.service.ClusterService;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开放API - 应用 Controller
 */
@RestController("openapiAppController")
@RequestMapping("/openapi/v1")
public class AppController {

  private final PortalSettings portalSettings;
  private final ClusterService clusterService;
  private final AppService appService;

  public AppController(final PortalSettings portalSettings,
      final ClusterService clusterService,
      final AppService appService) {
    this.portalSettings = portalSettings;
    this.clusterService = clusterService;
    this.appService = appService;
  }

  /**
   * 加载环境集群信息
   *
   * @param appId 应用id
   * @return
   */
  @GetMapping(value = "/apps/{appId}/envclusters")
  public List<OpenEnvClusterDTO> loadEnvClusterInfo(@PathVariable String appId) {

    List<OpenEnvClusterDTO> envClusters = new LinkedList<>();

    // 所有活动的环境
    List<Env> envs = portalSettings.getActiveEnvs();
    for (Env env : envs) {
      OpenEnvClusterDTO envCluster = new OpenEnvClusterDTO();

      envCluster.setEnv(env.getName());
      List<ClusterDTO> clusterDTOs = clusterService.findClusters(env, appId);
      envCluster.setClusters(BeanUtils.toPropertySet("name", clusterDTOs));

      envClusters.add(envCluster);
    }

    return envClusters;

  }

  /**
   * 查询指定的应用列表
   *
   * @param appIds 应用id列表
   * @return 指定的应用列表
   */
  @GetMapping("/apps")
  public List<OpenAppDTO> findApps(
      @RequestParam(value = "appIds", required = false) String appIds) {
    final List<App> apps = new ArrayList<>();
    // 为空，查询全部
    if (StringUtils.isEmpty(appIds)) {
      apps.addAll(appService.findAll());
    } else {
      apps.addAll(appService.findByAppIds(Sets.newHashSet(appIds.split(","))));
    }
    return OpenApiBeanUtils.transformFromApps(apps);
  }
}
