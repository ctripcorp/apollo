package com.ctrip.framework.apollo.portal.component;

import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.environment.PortalMetaDomainService;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

/**
 * 封装RestTemplate. admin server集群在某些机器宕机或者超时的情况下轮询重试
 */
@Slf4j
@Component
public class RetryableRestTemplate {

  /**
   * uri模样处理器
   */
  private UriTemplateHandler uriTemplateHandler = new DefaultUriBuilderFactory();

  private static final Gson GSON = new Gson();
  /**
   * 在"PortalDB.ServerConfig"中管理的服务访问令牌
   */
  private static final Type ACCESS_TOKENS = new TypeToken<Map<String, String>>() {
  }.getType();

  private RestTemplate restTemplate;

  private final RestTemplateFactory restTemplateFactory;
  private final AdminServiceAddressLocator adminServiceAddressLocator;
  private final PortalMetaDomainService portalMetaDomainService;
  private final PortalConfig portalConfig;
  /**
   * 最后访问的系统服务访问Token
   */
  private volatile String lastAdminServiceAccessTokens;
  /**
   * 系统服务访问Token Map<环境 ， token>
   */
  private volatile Map<Env, String> adminServiceAccessTokenMap;

  public RetryableRestTemplate(
      final @Lazy RestTemplateFactory restTemplateFactory,
      final @Lazy AdminServiceAddressLocator adminServiceAddressLocator,
      final PortalMetaDomainService portalMetaDomainService,
      final PortalConfig portalConfig
  ) {
    this.restTemplateFactory = restTemplateFactory;
    this.adminServiceAddressLocator = adminServiceAddressLocator;
    this.portalMetaDomainService = portalMetaDomainService;
    this.portalConfig = portalConfig;
  }

  /**
   * 初始化restTemplate
   */
  @PostConstruct
  private void postConstruct() {
    restTemplate = restTemplateFactory.getObject();
  }

  /**
   * Get方式请求指定环境指定路径指定参数的响应信息
   *
   * @param env          环境
   * @param path         指定路径
   * @param responseType 响应类型
   * @param urlVariables url请求参数
   * @param <T>          响应类型泛型
   * @return 响应结果
   * @throws RestClientException 客户端异常
   */
  public <T> T get(Env env, String path, Class<T> responseType, Object... urlVariables)
      throws RestClientException {
    return execute(HttpMethod.GET, env, path, null, responseType, urlVariables);
  }

  /**
   * Get方式exchange方法请求指定环境指定路径指定参数
   *
   * @param env          环境
   * @param path         指定路径
   * @param reference
   * @param uriVariables uri请求参数
   * @param <T>          响应类型泛型
   * @return 响应结果
   * @throws RestClientException 客户端异常
   */
  public <T> ResponseEntity<T> get(Env env, String path, ParameterizedTypeReference<T> reference,
      Object... uriVariables)
      throws RestClientException {

    return exchangeGet(env, path, reference, uriVariables);
  }

  /**
   * Get方式请求指定环境指定路径指定参数的响应信息
   *
   * @param env          环境
   * @param path         指定路径
   * @param request      请求对象
   * @param responseType 响应类型
   * @param uriVariables uri参数
   * @param <T>          响应类型泛型
   * @return 响应结果
   * @throws RestClientException 客户端异常
   */
  public <T> T post(Env env, String path, Object request, Class<T> responseType,
      Object... uriVariables)
      throws RestClientException {
    return execute(HttpMethod.POST, env, path, request, responseType, uriVariables);
  }

  /**
   * put方式，更新指定环境指定请求数据
   *
   * @param env          环境
   * @param path         指定路径
   * @param request      请求对象
   * @param urlVariables url参数
   * @throws RestClientException 客户端异常
   */
  public void put(Env env, String path, Object request, Object... urlVariables)
      throws RestClientException {
    execute(HttpMethod.PUT, env, path, request, null, urlVariables);
  }

  /**
   * delete方式，删除指定环境指定路径下的数据
   *
   * @param env          环境
   * @param path         指定路径
   * @param urlVariables url参数
   * @throws RestClientException 客户端异常
   */
  public void delete(Env env, String path, Object... urlVariables) throws RestClientException {
    execute(HttpMethod.DELETE, env, path, null, null, urlVariables);
  }

  /**
   * 执行
   *
   * @param method       请求方法
   * @param env          环境
   * @param path         路径
   * @param request      请求对象
   * @param responseType 响应对象
   * @param uriVariables uri变量
   * @param <T>          执行结果实体对象
   * @return 执行结果
   */
  private <T> T execute(HttpMethod method, Env env, String path, Object request,
      Class<T> responseType,
      Object... uriVariables) {
    // 忽略路径中的"/"
    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    String uri = uriTemplateHandler.expand(path, uriVariables).getPath();
    Transaction ct = Tracer.newTransaction("AdminAPI", uri);
    ct.addData("Env", env);

    // 指定环境的服务信息列表
    List<ServiceDTO> services = getAdminServices(env, ct);
    // 组装指定环境下的访问token作为header
    HttpHeaders extraHeaders = assembleExtraHeaders(env);

    for (ServiceDTO serviceDTO : services) {
      try {

        T result = doExecute(method, extraHeaders, serviceDTO, path, request, responseType,
            uriVariables);

        ct.setStatus(Transaction.SUCCESS);
        ct.complete();
        return result;
      } catch (Throwable t) {
        log.error("Http request failed, uri: {}, method: {}", uri, method, t);
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
        new ServiceException(String
            .format("Admin servers are unresponsive. meta server address: %s, admin servers: %s",
                portalMetaDomainService.getDomain(env), services));
    ct.setStatus(e);
    ct.complete();
    throw e;
  }

  /**
   * Get方式的exchange
   *
   * @param env          环境
   * @param path         路径
   * @param reference    参数引用类型
   * @param uriVariables uri变量
   * @param <T>          响应信息泛型
   * @return 响应信息
   */
  private <T> ResponseEntity<T> exchangeGet(Env env, String path,
      ParameterizedTypeReference<T> reference, Object... uriVariables) {
    // 忽略路径中的"/"
    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    String uri = uriTemplateHandler.expand(path, uriVariables).getPath();
    Transaction ct = Tracer.newTransaction("AdminAPI", uri);
    ct.addData("Env", env);

    // 获取系统服务列表，并设置header
    List<ServiceDTO> services = getAdminServices(env, ct);
    HttpEntity<Void> entity = new HttpEntity<>(assembleExtraHeaders(env));

    for (ServiceDTO serviceDTO : services) {
      try {
        ResponseEntity<T> result =
            restTemplate.exchange(parseHost(serviceDTO) + path, HttpMethod.GET, entity, reference,
                uriVariables);

        ct.setStatus(Transaction.SUCCESS);
        ct.complete();
        return result;
      } catch (Throwable t) {
        log.error("Http request failed, uri: {}, method: {}", uri, HttpMethod.GET, t);
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
    ServiceException e = new ServiceException(String.format(
        "Admin servers are unresponsive. meta server address: %s, admin servers: %s",
        portalMetaDomainService.getDomain(env), services));
    ct.setStatus(e);
    ct.complete();
    throw e;

  }

  /**
   * 组装额外的header，添加管理服务
   *
   * @param env 环境
   * @return 额外的header对象
   */
  private HttpHeaders assembleExtraHeaders(Env env) {
    String adminServiceAccessToken = getAdminServiceAccessToken(env);

    if (!Strings.isNullOrEmpty(adminServiceAccessToken)) {
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.AUTHORIZATION, adminServiceAccessToken);
      return headers;
    }
    return null;
  }

  /**
   * 获取系统服务集
   *
   * @param env 指定环境
   * @param ct  cat的事务
   * @return 指定环境的服务列表
   */
  private List<ServiceDTO> getAdminServices(Env env, Transaction ct) {

    List<ServiceDTO> services = adminServiceAddressLocator.getServiceList(env);

    if (CollectionUtils.isEmpty(services)) {
      ServiceException e = new ServiceException(String.format("No available admin server."
              + " Maybe because of meta server down or all admin server down. "
              + "Meta server address: %s",
          portalMetaDomainService.getDomain(env)));
      ct.setStatus(e);
      ct.complete();
      throw e;
    }

    return services;
  }

  /**
   * 获取指定环境的系统服务访问Token
   *
   * @param env 环境
   * @return 指定环境的系统服务访问Token
   */
  private String getAdminServiceAccessToken(Env env) {
    String accessTokens = portalConfig.getAdminServiceAccessTokens();

    if (StringUtils.isBlank(accessTokens)) {
      return null;
    }

    //如果不是之前调用的访问token重新去解析
    if (!accessTokens.equals(lastAdminServiceAccessTokens)) {
      synchronized (this) {
        adminServiceAccessTokenMap = parseAdminServiceAccessTokens(accessTokens);
        lastAdminServiceAccessTokens = accessTokens;
      }
    }
    // 从缓存中去获取
    return adminServiceAccessTokenMap.get(env);
  }

  /**
   * 解析管理服务访问token集
   *
   * @param accessTokens 访问token集
   * @return tokenMap<环境 ， token>
   */
  private Map<Env, String> parseAdminServiceAccessTokens(String accessTokens) {
    //将token放入这个Map中
    Map<Env, String> tokenMap = Maps.newHashMap();
    try {
      // 开始解析
      Map<String, String> map = GSON.fromJson(accessTokens, ACCESS_TOKENS);
      map.forEach((env, token) -> {
        if (Env.exists(env)) {
          tokenMap.put(Env.valueOf(env), token);
        }
      });
    } catch (Exception e) {
      log.error("Wrong format of admin service access tokens: {}", accessTokens, e);
    }
    return tokenMap;
  }

  /**
   * 执行请求
   *
   * @param method       方法请求方式
   * @param extraHeaders 额外的header
   * @param service      服务器信息
   * @param path         路径
   * @param request      请求对象
   * @param responseType 响应类型
   * @param uriVariables url参数
   * @param <T>          响应实体类型
   * @return 响应信息
   */
  private <T> T doExecute(HttpMethod method, HttpHeaders extraHeaders, ServiceDTO service,
      String path, Object request,
      Class<T> responseType, Object... uriVariables) {
    T result = null;
    switch (method) {
      case GET:
      case POST:
      case PUT:
      case DELETE:
        //拼接请求实体和header
        HttpEntity entity;
        if (request instanceof HttpEntity) {
          entity = (HttpEntity) request;
          if (!CollectionUtils.isEmpty(extraHeaders)) {
            HttpHeaders headers = new HttpHeaders();
            headers.addAll(entity.getHeaders());
            headers.addAll(extraHeaders);
            entity = new HttpEntity<>(entity.getBody(), headers);
          }
        } else {
          entity = new HttpEntity<>(request, extraHeaders);
        }
        // 调用接口
        result = restTemplate
            .exchange(parseHost(service) + path, method, entity, responseType, uriVariables)
            .getBody();
        break;
      default:
        throw new UnsupportedOperationException(
            String.format("unsupported http method(method=%s)", method));
    }
    return result;
  }

  /**
   * 解析主页地址。如homePageUrl=http://localhost:8080
   *
   * @param serviceAddress 服务地址信息
   * @return host Url
   */
  private String parseHost(ServiceDTO serviceAddress) {
    return serviceAddress.getHomepageUrl() + "/";
  }

  //post,delete,put请求在admin server处理超时情况下不重试

  /**
   * 能否重试.
   *
   * @param e      异常
   * @param method 请求方式
   * @return true, 重试，否则，false
   */
  private boolean canRetry(Throwable e, HttpMethod method) {
    Throwable nestedException = e.getCause();
    // GET请求 网络超时，拒绝连接，连接超时都重试
    if (method == HttpMethod.GET) {
      return nestedException instanceof SocketTimeoutException
          || nestedException instanceof HttpHostConnectException
          || nestedException instanceof ConnectTimeoutException;
    }
    //除GET方式外，其它连接方式超时不重试
    return nestedException instanceof HttpHostConnectException
        || nestedException instanceof ConnectTimeoutException;
  }

}
