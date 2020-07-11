package com.ctrip.framework.apollo.metaservice.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.google.common.collect.Lists;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

@RunWith(MockitoJUnitRunner.class)
public class DefaultDiscoveryServiceTest {

  @Mock
  private DiscoveryClient discoveryClient;

  private DefaultDiscoveryService defaultDiscoveryService;

  private String someServiceId;

  @Before
  public void setUp() throws Exception {
    defaultDiscoveryService = new DefaultDiscoveryService(discoveryClient);

    someServiceId = "someServiceId";
  }

  @Test
  public void testGetServiceInstancesWithNullInstances() {
    when(discoveryClient.getInstances(someServiceId)).thenReturn(null);

    assertTrue(defaultDiscoveryService.getServiceInstances(someServiceId).isEmpty());
  }

  @Test
  public void testGetServiceInstancesWithEmptyInstances() {
    when(discoveryClient.getInstances(someServiceId)).thenReturn(new ArrayList<>());

    assertTrue(defaultDiscoveryService.getServiceInstances(someServiceId).isEmpty());
  }

  @Test
  public void testGetServiceInstances() throws URISyntaxException {
    String someHost = "1.2.3.4";
    int somePort = 8080;
    String someUri = String.format("http://%s:%s/some-path/", someHost, somePort);
    ServiceInstance someServiceInstance = mockServiceInstance(someServiceId, someHost, somePort,
        someUri);

    String anotherHost = "2.3.4.5";
    int anotherPort = 9090;
    String anotherUri = String.format("http://%s:%s/some-path-with-no-slash", anotherHost, anotherPort);
    ServiceInstance anotherServiceInstance = mockServiceInstance(someServiceId, anotherHost, anotherPort,
        anotherUri);

    when(discoveryClient.getInstances(someServiceId))
        .thenReturn(Lists.newArrayList(someServiceInstance, anotherServiceInstance));

    List<ServiceDTO> serviceDTOList = defaultDiscoveryService.getServiceInstances(someServiceId);

    assertEquals(2, serviceDTOList.size());
    check(someServiceInstance, serviceDTOList.get(0), false);
    check(anotherServiceInstance, serviceDTOList.get(1), true);
  }

  private void check(ServiceInstance serviceInstance, ServiceDTO serviceDTO, boolean appendSlashToUri) {
    assertEquals(serviceInstance.getServiceId(), serviceDTO.getAppName());
    assertEquals(serviceDTO.getInstanceId(), String
        .format("%s:%s:%s", serviceInstance.getHost(), serviceInstance.getServiceId(),
            serviceInstance.getPort()));
    if (appendSlashToUri) {
      assertEquals(serviceInstance.getUri().toString() + "/", serviceDTO.getHomepageUrl());
    } else {
      assertEquals(serviceInstance.getUri().toString(), serviceDTO.getHomepageUrl());
    }
  }

  private ServiceInstance mockServiceInstance(String serviceId, String host, int port, String uri)
      throws URISyntaxException {
    ServiceInstance serviceInstance = mock(ServiceInstance.class);
    when(serviceInstance.getServiceId()).thenReturn(serviceId);
    when(serviceInstance.getHost()).thenReturn(host);
    when(serviceInstance.getPort()).thenReturn(port);
    when(serviceInstance.getUri()).thenReturn(new URI(uri));

    return serviceInstance;
  }
}