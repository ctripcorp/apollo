package com.ctrip.framework.apollo.metaservice.service;

import com.ctrip.framework.apollo.common.condition.ConditionalOnMissingProfile;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.tracer.Tracer;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@ConditionalOnMissingProfile({"kubernetes"})
public class DefaultDiscoveryService implements DiscoveryService {

  private final DiscoveryClient discoveryClient;

  public DefaultDiscoveryService(final DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  public List<ServiceDTO> getServiceInstances(String serviceId) {
    List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
    if (CollectionUtils.isEmpty(instances)) {
      Tracer.logEvent("Apollo.Discovery.NotFound", serviceId);
      return Collections.emptyList();
    }
    return instances.stream().map(instanceInfoToServiceDTOFunc)
        .collect(Collectors.toList());
  }

  private static Function<ServiceInstance, ServiceDTO> instanceInfoToServiceDTOFunc = instance -> {
    ServiceDTO service = new ServiceDTO();
    service.setAppName(instance.getServiceId());
    service.setInstanceId(
        String.format("%s:%s:%s", instance.getHost(), instance.getServiceId(), instance.getPort()));
    String uri = instance.getUri().toString();
    if (!uri.endsWith("/")) {
      uri += "/";
    }
    service.setHomepageUrl(uri);
    return service;
  };

}
