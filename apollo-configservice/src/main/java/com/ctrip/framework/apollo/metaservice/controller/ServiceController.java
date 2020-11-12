package com.ctrip.framework.apollo.metaservice.controller;

import com.ctrip.framework.apollo.core.ServiceNameConsts;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.metaservice.service.DiscoveryService;
import java.util.Collections;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务 Controller.
 */
@RestController
@RequestMapping("/services")
public class ServiceController {

  private final DiscoveryService discoveryService;

  public ServiceController(final DiscoveryService discoveryService) {
    this.discoveryService = discoveryService;
  }

  /**
   * 此方法始终返回空列表，因为根本没有使用元服务.
   */
  @Deprecated
  @RequestMapping("/meta")
  public List<ServiceDTO> getMetaService() {
    return Collections.emptyList();
  }

  /**
   * 获取配置服务列表信息.
   *
   * @param appId    应用id
   * @param clientIp 客户端ip
   * @return 配置服务列表信息
   */
  @RequestMapping("/config")
  public List<ServiceDTO> getConfigService(
      @RequestParam(value = "appId", defaultValue = "") String appId,
      @RequestParam(value = "ip", required = false) String clientIp) {
    return discoveryService.getServiceInstances(ServiceNameConsts.APOLLO_CONFIG_SERVICE);
  }

  /**
   * 获取系统服务列表信息.
   *
   * @return 系统服务列表信息
   */
  @RequestMapping("/admin")
  public List<ServiceDTO> getAdminService() {
    return discoveryService.getServiceInstances(ServiceNameConsts.APOLLO_ADMIN_SERVICE);
  }
}
