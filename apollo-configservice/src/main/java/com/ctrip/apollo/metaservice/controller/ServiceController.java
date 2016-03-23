package com.ctrip.apollo.metaservice.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.core.serivce.ApolloService;
import com.ctrip.apollo.metaservice.service.DiscoveryService;
import com.netflix.appinfo.InstanceInfo;

@RestController
@RequestMapping("/services")
public class ServiceController {

  @Autowired
  private DiscoveryService discoveryService;


  @RequestMapping("/meta")
  public List<ApolloService> getMetaService() {
    List<InstanceInfo> instances = discoveryService.getMetaServiceInstances();
    List<ApolloService> result = new ArrayList<ApolloService>();
    for (InstanceInfo instance : instances) {
      ApolloService service = new ApolloService();
      service.setAppName(instance.getAppName());
      service.setInstanceId(instance.getInstanceId());
      service.setHomepageUrl(instance.getHomePageUrl());
      result.add(service);
    }
    return result;
  }

  @RequestMapping("/config")
  public List<ApolloService> getConfigService() {
    List<InstanceInfo> instances = discoveryService.getConfigServiceInstances();
    List<ApolloService> result = new ArrayList<ApolloService>();
    for (InstanceInfo instance : instances) {
      ApolloService service = new ApolloService();
      service.setAppName(instance.getAppName());
      service.setInstanceId(instance.getInstanceId());
      service.setHomepageUrl(instance.getHomePageUrl());
      result.add(service);
    }
    return result;
  }

  @RequestMapping("/admin")
  public List<ApolloService> getAdminService() {
    List<InstanceInfo> instances = discoveryService.getAdminServiceInstances();
    List<ApolloService> result = new ArrayList<ApolloService>();
    for (InstanceInfo instance : instances) {
      ApolloService service = new ApolloService();
      service.setAppName(instance.getAppName());
      service.setInstanceId(instance.getInstanceId());
      service.setHomepageUrl(instance.getHomePageUrl());
      result.add(service);
    }
    return result;
  }
}
