package com.ctrip.apollo.metaservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.core.ServiceNameConsts;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

@Service
public class DiscoveryService {

  @Autowired
  private EurekaClient eurekaClient;

  public List<InstanceInfo> getConfigServiceInstances() {
    Application application = eurekaClient.getApplication(ServiceNameConsts.APOLLO_CONFIGSERVICE);
    return application != null ? application.getInstances() : new ArrayList<>();
  }

  public List<InstanceInfo> getMetaServiceInstances() {
    Application application = eurekaClient.getApplication(ServiceNameConsts.APOLLO_METASERVICE);
    return application != null ? application.getInstances() : new ArrayList<>();
  }

  public List<InstanceInfo> getAdminServiceInstances(){
    Application application = eurekaClient.getApplication(ServiceNameConsts.APOLLO_ADMINSERVICE);
    return application != null ? application.getInstances() : new ArrayList<>();
  }
}
