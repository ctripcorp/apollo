package com.ctrip.framework.apollo.metaservice.service;

import com.ctrip.framework.apollo.core.ServiceNameConsts;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DiscoveryService {

  private final EurekaClient eurekaClient;

  public DiscoveryService(final EurekaClient eurekaClient) {
    this.eurekaClient = eurekaClient;
  }

  public List<InstanceInfo> getConfigServiceInstances() {
    Application application = eurekaClient.getApplication(ServiceNameConsts.APOLLO_CONFIGSERVICE);
    if (application == null) {
      Tracer.logEvent("Apollo.EurekaDiscovery.NotFound", ServiceNameConsts.APOLLO_CONFIGSERVICE);
    }
    return application != null ? application.getInstances() : Collections.emptyList();
  }

  public List<InstanceInfo> getMetaServiceInstances() {
    Application application = eurekaClient.getApplication(ServiceNameConsts.APOLLO_METASERVICE);
    if (application == null) {
      Tracer.logEvent("Apollo.EurekaDiscovery.NotFound", ServiceNameConsts.APOLLO_METASERVICE);
    }
    return application != null ? application.getInstances() : Collections.emptyList();
  }

  public List<InstanceInfo> getAdminServiceInstances() {
    Application application = eurekaClient.getApplication(ServiceNameConsts.APOLLO_ADMINSERVICE);
    if (application == null) {
      Tracer.logEvent("Apollo.EurekaDiscovery.NotFound", ServiceNameConsts.APOLLO_ADMINSERVICE);
    }
    return application != null ? application.getInstances() : Collections.emptyList();
  }
}
