package com.ctrip.framework.apollo.portal.component;

import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.core.MetaDomainConsts;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

import javax.annotation.PostConstruct;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * 封装RestTemplate. admin server集群在某些机器宕机或者超时的情况下轮询重试
 */
@Component
public class RetryableRestTemplate {

  private Logger logger = LoggerFactory.getLogger(RetryableRestTemplate.class);

  private UriTemplateHandler uriTemplateHandler = new DefaultUriBuilderFactory();

  private RestTemplate restTemplate;

  private final RestTemplateFactory restTemplateFactory;
  private final AdminServiceAddressLocator adminServiceAddressLocator;

  public RetryableRestTemplate(
      final @Lazy RestTemplateFactory restTemplateFactory,
      final @Lazy AdminServiceAddressLocator adminServiceAddressLocator) {
    this.restTemplateFactory = restTemplateFactory;
    this.adminServiceAddressLocator = adminServiceAddressLocator;
  }


  @PostConstruct
  private void postConstruct() {
    restTemplate = restTemplateFactory.getObject();
  }

  public <T> T get(String env, String path, Class<T> responseType, Object... urlVariables)
      throws RestClientException {
    return execute(HttpMethod.GET, env, path, null, responseType, urlVariables);
  }

  public <T> ResponseEntity<T> get(String env, String path, ParameterizedTypeReference<T> reference,
                                   Object... uriVariables)
      throws RestClientException {

    return exchangeGet(env, path, reference, uriVariables);
  }

  public <T> T post(String env, String path, Object request, Class<T> responseType, Object... uriVariables)
      throws RestClientException {
    return execute(HttpMethod.POST, env, path, request, responseType, uriVariables);
  }

  public void put(String env, String path, Object request, Object... urlVariables) throws RestClientException {
    execute(HttpMethod.PUT, env, path, request, null, urlVariables);
  }

  public void delete(String env, String path, Object... urlVariables) throws RestClientException {
    execute(HttpMethod.DELETE, env, path, null, null, urlVariables);
  }

  private <T> T execute(HttpMethod method, String env, String path, Object request, Class<T> responseType,
                        Object... uriVariables) {

    if (path.startsWith("/")) {
      path = path.substring(1, path.length());
    }

    String uri = uriTemplateHandler.expand(path, uriVariables).getPath();
    Transaction ct = Tracer.newTransaction("AdminAPI", uri);
    ct.addData("Env", env);

    List<ServiceDTO> services = getAdminServices(env, ct);

    for (ServiceDTO serviceDTO : services) {
      try {

        T result = doExecute(method, serviceDTO, path, request, responseType, uriVariables);

        ct.setStatus(Transaction.SUCCESS);
        ct.complete();
        return result;
      } catch (Throwable t) {
        logger.error("Http request failed, uri: {}, method: {}", uri, method, t);
        Tracer.logError(t);
        if (canRetry(t, method)) {
          Tracer.logEvent(TracerEventType.API_RETRY, uri);
        } else {//biz exception rethrow
          ct.setStatus(t);
          ct.complete();
          throw t;
        }
      }
    }

    //all admin server down
    ServiceException e =
        new ServiceException(String.format("Admin servers are unresponsive. meta server address: %s, admin servers: %s",
                                           MetaDomainConsts.getDomain(env), services));
    ct.setStatus(e);
    ct.complete();
    throw e;
  }

  private <T> ResponseEntity<T> exchangeGet(String env, String path, ParameterizedTypeReference<T> reference,
                                            Object... uriVariables) {
    if (path.startsWith("/")) {
      path = path.substring(1, path.length());
    }

    String uri = uriTemplateHandler.expand(path, uriVariables).getPath();
    Transaction ct = Tracer.newTransaction("AdminAPI", uri);
    ct.addData("Env", env);

    List<ServiceDTO> services = getAdminServices(env, ct);

    for (ServiceDTO serviceDTO : services) {
      try {

        ResponseEntity<T> result =
            restTemplate.exchange(parseHost(serviceDTO) + path, HttpMethod.GET, null, reference, uriVariables);

        ct.setStatus(Transaction.SUCCESS);
        ct.complete();
        return result;
      } catch (Throwable t) {
        logger.error("Http request failed, uri: {}, method: {}", uri, HttpMethod.GET, t);
        Tracer.logError(t);
        if (canRetry(t, HttpMethod.GET)) {
          Tracer.logEvent(TracerEventType.API_RETRY, uri);
        } else {// biz exception rethrow
          ct.setStatus(t);
          ct.complete();
          throw t;
        }

      }
    }

    //all admin server down
    ServiceException e =
        new ServiceException(String.format("Admin servers are unresponsive. meta server address: %s, admin servers: %s",
                                           MetaDomainConsts.getDomain(env), services));
    ct.setStatus(e);
    ct.complete();
    throw e;

  }

  private List<ServiceDTO> getAdminServices(String env, Transaction ct) {

    List<ServiceDTO> services = adminServiceAddressLocator.getServiceList(env);

    if (CollectionUtils.isEmpty(services)) {
      ServiceException e = new ServiceException(String.format("No available admin server."
                                                              + " Maybe because of meta server down or all admin server down. "
                                                              + "Meta server address: %s",
                                                              MetaDomainConsts.getDomain(env)));
      ct.setStatus(e);
      ct.complete();
      throw e;
    }

    return services;
  }

  private <T> T doExecute(HttpMethod method, ServiceDTO service, String path, Object request,
                          Class<T> responseType,
                          Object... uriVariables) {
    T result = null;
    switch (method) {
      case GET:
        result = restTemplate.getForObject(parseHost(service) + path, responseType, uriVariables);
        break;
      case POST:
        result =
            restTemplate.postForEntity(parseHost(service) + path, request, responseType, uriVariables).getBody();
        break;
      case PUT:
        restTemplate.put(parseHost(service) + path, request, uriVariables);
        break;
      case DELETE:
        restTemplate.delete(parseHost(service) + path, uriVariables);
        break;
      default:
        throw new UnsupportedOperationException(String.format("unsupported http method(method=%s)", method));
    }
    return result;
  }

  private String parseHost(ServiceDTO serviceAddress) {
    return serviceAddress.getHomepageUrl() + "/";
  }

  //post,delete,put请求在admin server处理超时情况下不重试
  private boolean canRetry(Throwable e, HttpMethod method) {
    Throwable nestedException = e.getCause();
    if (method == HttpMethod.GET) {
      return nestedException instanceof SocketTimeoutException
             || nestedException instanceof HttpHostConnectException
             || nestedException instanceof ConnectTimeoutException;
    } else {
      return nestedException instanceof HttpHostConnectException
             || nestedException instanceof ConnectTimeoutException;
    }
  }

}
