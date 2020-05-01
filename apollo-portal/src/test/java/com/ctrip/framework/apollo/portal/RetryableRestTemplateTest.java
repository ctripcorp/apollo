package com.ctrip.framework.apollo.portal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.portal.component.AdminServiceAddressLocator;
import com.ctrip.framework.apollo.portal.component.RetryableRestTemplate;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.environment.PortalMetaDomainService;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collections;
import org.apache.http.HttpHost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class RetryableRestTemplateTest extends AbstractUnitTest {

  @Mock
  private AdminServiceAddressLocator serviceAddressLocator;
  @Mock
  private RestTemplate restTemplate;
  @Mock
  private PortalMetaDomainService portalMetaDomainService;
  @InjectMocks
  private RetryableRestTemplate retryableRestTemplate;

  private String path = "app";
  private String serviceOne = "http://10.0.0.1";
  private String serviceTwo = "http://10.0.0.2";
  private String serviceThree = "http://10.0.0.3";
  private ResourceAccessException socketTimeoutException = new ResourceAccessException("");
  private ResourceAccessException httpHostConnectException = new ResourceAccessException("");
  private ResourceAccessException connectTimeoutException = new ResourceAccessException("");
  private Object request = new Object();
  private ResponseEntity<Object> entity = new ResponseEntity<>(HttpStatus.OK);


  @Before
  public void init() {
    socketTimeoutException.initCause(new SocketTimeoutException());

    httpHostConnectException
        .initCause(new HttpHostConnectException(new ConnectTimeoutException(), new HttpHost(serviceOne, 80)));
    connectTimeoutException.initCause(new ConnectTimeoutException());
  }

  @Test(expected = ServiceException.class)
  public void testNoAdminServer() {

    when(serviceAddressLocator.getServiceList(any())).thenReturn(Collections.emptyList());

    retryableRestTemplate.get(Env.DEV, path, Object.class);
  }

  @Test(expected = ServiceException.class)
  public void testAllServerDown() {

    when(serviceAddressLocator.getServiceList(any()))
        .thenReturn(Arrays.asList(mockService(serviceOne), mockService(serviceTwo), mockService(serviceThree)));
    when(restTemplate.getForObject(serviceOne + "/" + path, Object.class)).thenThrow(socketTimeoutException);
    when(restTemplate.getForObject(serviceTwo + "/" + path, Object.class)).thenThrow(httpHostConnectException);
    when(restTemplate.getForObject(serviceThree + "/" + path, Object.class)).thenThrow(connectTimeoutException);

    retryableRestTemplate.get(Env.DEV, path, Object.class);

    verify(restTemplate).getForObject(serviceOne + "/" + path, Object.class);
    verify(restTemplate).getForObject(serviceTwo + "/" + path, Object.class);
    verify(restTemplate).getForObject(serviceThree + "/" + path, Object.class);


  }

  @Test
  public void testOneServerDown() {

    Object result = new Object();
    when(serviceAddressLocator.getServiceList(any()))
        .thenReturn(Arrays.asList(mockService(serviceOne), mockService(serviceTwo), mockService(serviceThree)));
    when(restTemplate.getForObject(serviceOne + "/" + path, Object.class)).thenThrow(socketTimeoutException);
    when(restTemplate.getForObject(serviceTwo + "/" + path, Object.class)).thenReturn(result);
    when(restTemplate.getForObject(serviceThree + "/" + path, Object.class)).thenThrow(connectTimeoutException);

    Object o = retryableRestTemplate.get(Env.DEV, path, Object.class);

    verify(restTemplate).getForObject(serviceOne + "/" + path, Object.class);
    verify(restTemplate).getForObject(serviceTwo + "/" + path, Object.class);
    verify(restTemplate, times(0)).getForObject(serviceThree + "/" + path, Object.class);
    Assert.assertEquals(result, o);
  }

  @Test(expected = ResourceAccessException.class)
  public void testPostSocketTimeoutNotRetry(){
    when(serviceAddressLocator.getServiceList(any()))
        .thenReturn(Arrays.asList(mockService(serviceOne), mockService(serviceTwo), mockService(serviceThree)));

    when(restTemplate.postForEntity(serviceOne + "/" + path, request, Object.class)).thenThrow(socketTimeoutException);
    when(restTemplate.postForEntity(serviceTwo + "/" + path, request, Object.class)).thenReturn(entity);

    retryableRestTemplate.post(Env.DEV, path, request, Object.class);

    verify(restTemplate).postForEntity(serviceOne + "/" + path, request, Object.class);
    verify(restTemplate, times(0)).postForEntity(serviceTwo + "/" + path, request, Object.class);
  }


  @Test
  public void testDelete(){
    when(serviceAddressLocator.getServiceList(any()))
        .thenReturn(Arrays.asList(mockService(serviceOne), mockService(serviceTwo), mockService(serviceThree)));

    retryableRestTemplate.delete(Env.DEV, path);

    verify(restTemplate).delete(serviceOne + "/" + path);

  }

  @Test
  public void testPut(){
    when(serviceAddressLocator.getServiceList(any()))
        .thenReturn(Arrays.asList(mockService(serviceOne), mockService(serviceTwo), mockService(serviceThree)));

    retryableRestTemplate.put(Env.DEV, path, request);

    verify(restTemplate).put(serviceOne + "/" + path, request);
  }

  private ServiceDTO mockService(String homeUrl) {
    ServiceDTO serviceDTO = new ServiceDTO();
    serviceDTO.setHomepageUrl(homeUrl);
    return serviceDTO;
  }

}
