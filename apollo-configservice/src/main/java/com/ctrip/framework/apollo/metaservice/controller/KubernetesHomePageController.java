package com.ctrip.framework.apollo.metaservice.controller;

import com.ctrip.framework.apollo.core.ServiceNameConsts;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.metaservice.service.DiscoveryService;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * kubernetes主页 Controller,对于kubernetes发现服务，没有eureka主页，所以我们需要添加一个默认主页
 */
@Profile({"kubernetes"})
@RestController
public class KubernetesHomePageController {

  /**
   * 服务发现
   */
  private final DiscoveryService discoveryService;

  public KubernetesHomePageController(DiscoveryService discoveryService) {
    this.discoveryService = discoveryService;
  }

  /**
   * 所有服务列表
   *
   * @return 服务信息列表
   */
  @GetMapping("/")
  public List<ServiceDTO> listAllServices() {
    List<ServiceDTO> allServices = Lists.newLinkedList();
    // 配置服务实例
    allServices.addAll(discoveryService.getServiceInstances(
        ServiceNameConsts.APOLLO_CONFIG_SERVICE));
    // admin服务实例
    allServices.addAll(discoveryService.getServiceInstances(
        ServiceNameConsts.APOLLO_ADMIN_SERVICE));

    return allServices;
  }
}
