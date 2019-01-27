package com.ctrip.framework.apollo.metaservice.controller;

import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.metaservice.service.DiscoveryService;
import com.netflix.appinfo.InstanceInfo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/services")
public class ServiceController {

  private final DiscoveryService discoveryService;

  public ServiceController(final DiscoveryService discoveryService) {
    this.discoveryService = discoveryService;
  }


  @RequestMapping("/meta")
  public List<ServiceDTO> getMetaService() {
    List<InstanceInfo> instances = discoveryService.getMetaServiceInstances();
    List<ServiceDTO> result = instances.stream().map(InstanceInfo_To_ServiceDTO_Func).collect(Collectors.toList());
    return result;
  }

  @RequestMapping("/config")
  public List<ServiceDTO> getConfigService(
      @RequestParam(value = "appId", defaultValue = "") String appId,
      @RequestParam(value = "ip", required = false) String clientIp) {
    List<InstanceInfo> instances = discoveryService.getConfigServiceInstances();
    List<ServiceDTO> result = instances.stream().map(InstanceInfo_To_ServiceDTO_Func).collect(Collectors.toList());
    return result;
  }

  @RequestMapping("/admin")
  public List<ServiceDTO> getAdminService() {
    List<InstanceInfo> instances = discoveryService.getAdminServiceInstances();
    List<ServiceDTO> result = instances.stream().map(InstanceInfo_To_ServiceDTO_Func).collect(Collectors.toList());
    return result;
  }

  private static Function<InstanceInfo, ServiceDTO> InstanceInfo_To_ServiceDTO_Func = new Function<InstanceInfo, ServiceDTO>() {
      @Override
      public ServiceDTO apply(InstanceInfo instance) {
          ServiceDTO service = new ServiceDTO();
          service.setAppName(instance.getAppName());
          service.setInstanceId(instance.getInstanceId());
          service.setHomepageUrl(instance.getHomePageUrl());
          return service;
      }
  };

}
