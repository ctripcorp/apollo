package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.ServiceNameConsts;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.ctrip.framework.apollo.util.http.HttpRequest;
import com.ctrip.framework.apollo.util.http.HttpResponse;
import com.ctrip.framework.apollo.util.http.HttpUtil;
import com.ctrip.framework.foundation.Foundation;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Config Service 定位器(配置服务定位器)
 * <ul>
 *   <li>初始时，从 Meta Service 获取 Config Service 集群地址进行缓存</li>
 *   <li>定时任务，每 5 分钟，从 Meta Service 获取 Config Service 集群地址刷新缓存</li>
 * </ul>
 */
@Slf4j
public class ConfigServiceLocator {

  private HttpUtil m_httpUtil;
  private ConfigUtil m_configUtil;
  /**
   * 服务配置信息列表
   */
  private AtomicReference<List<ServiceDTO>> m_configServices;
  /**
   * 响应类型
   */
  private Type m_responseType;
  /**
   * 定时任务执行器
   */
  private ScheduledExecutorService m_executorService;
  private static final Joiner.MapJoiner MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");
  private static final Escaper queryParamEscaper = UrlEscapers.urlFormParameterEscaper();

  /**
   * Create a config service locator.
   */
  public ConfigServiceLocator() {
    List<ServiceDTO> initial = Lists.newArrayList();
    m_configServices = new AtomicReference<>(initial);
    m_responseType = new TypeToken<List<ServiceDTO>>() {
    }.getType();
    m_httpUtil = ApolloInjector.getInstance(HttpUtil.class);
    m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    this.m_executorService = Executors.newScheduledThreadPool(1,
        ApolloThreadFactory.create("ConfigServiceLocator", true));
    initConfigServices();
  }

  /**
   * 初始化配置服务信息
   */
  private void initConfigServices() {
    // get from run time configurations
    List<ServiceDTO> customizedConfigServices = getCustomizedConfigService();

    if (customizedConfigServices != null) {
      setConfigServices(customizedConfigServices);
      return;
    }

    // 初始拉取 Config Service 地址
    this.tryUpdateConfigServices();
    // 创建定时任务，定时拉取 Config Service 地址
    this.schedulePeriodicRefresh();
  }

  /**
   * 获取自定义配置服务信息
   *
   * @return 自定义配置服务信息列表
   */
  private List<ServiceDTO> getCustomizedConfigService() {
    // 1. 从系统属性中获取
    String configServices = System.getProperty("apollo.configService");
    if (Strings.isNullOrEmpty(configServices)) {
      // 2. 从操作系统环境变量中获取
      configServices = System.getenv("APOLLO_CONFIGSERVICE");
    }
    if (Strings.isNullOrEmpty(configServices)) {
      // 3. 从server.properties获取
      configServices = Foundation.server().getProperty("apollo.configService", null);
    }

    if (Strings.isNullOrEmpty(configServices)) {
      return null;
    }

    log.warn(
        "Located config services from apollo.configService configuration: {}, will not refresh config services from remote meta service!",
        configServices);

    // 模拟服务dto列表
    String[] configServiceUrls = configServices.split(",");
    List<ServiceDTO> serviceDTOS = Lists.newArrayList();

    for (String configServiceUrl : configServiceUrls) {
      configServiceUrl = configServiceUrl.trim();
      ServiceDTO serviceDTO = new ServiceDTO();
      serviceDTO.setHomepageUrl(configServiceUrl);
      serviceDTO.setAppName(ServiceNameConsts.APOLLO_CONFIG_SERVICE);
      serviceDTO.setInstanceId(configServiceUrl);
      serviceDTOS.add(serviceDTO);
    }

    return serviceDTOS;
  }

  /**
   * 从远程元服务器获取配置服务信息.
   *
   * @return 配置服务信息
   */
  public List<ServiceDTO> getConfigServices() {
    // 缓存为空，强制拉取
    if (m_configServices.get().isEmpty()) {
      updateConfigServices();
    }
    // 返回服务配置信息列表缓存
    return m_configServices.get();
  }

  /**
   * 尝试更新配置服务列表
   *
   * @return true, 更新成功，否则，false
   */
  private boolean tryUpdateConfigServices() {
    try {
      updateConfigServices();
      return true;
    } catch (Throwable ex) {
      //ignore
    }
    return false;
  }

  /**
   * 定时刷新计划
   */
  private void schedulePeriodicRefresh() {
    this.m_executorService.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            log.debug("refresh config services");
            Tracer.logEvent("Apollo.MetaService", "periodicRefresh");
            // 拉取配置服务地址
            tryUpdateConfigServices();
          }
        }, m_configUtil.getRefreshInterval(), m_configUtil.getRefreshInterval(),
        m_configUtil.getRefreshIntervalTimeUnit());
  }

  /**
   * 更新配置服务
   */
  private synchronized void updateConfigServices() {
    // 拼接请求 Meta Service URL
    String url = assembleMetaServiceUrl();

    HttpRequest request = new HttpRequest(url);
    // 最多重试两次
    int maxRetries = 2;
    Throwable exception = null;

    // 循环请求 Meta Service ，获取 Config Service 地址
    for (int i = 0; i < maxRetries; i++) {
      Transaction transaction = Tracer.newTransaction("Apollo.MetaService", "getConfigService");
      transaction.addData("Url", url);
      try {
        // 请求
        HttpResponse<List<ServiceDTO>> response = m_httpUtil.doGet(request, m_responseType);
        transaction.setStatus(Transaction.SUCCESS);
        // 获得结果 ServiceDTO 数组
        List<ServiceDTO> services = response.getBody();
        // 获得结果为空，重新请求
        if (CollectionUtils.isEmpty(services)) {
          logConfigService("Empty response!");
          continue;
        }
        // 更新缓存
        setConfigServices(services);
        return;
      } catch (Throwable ex) {
        Tracer.logEvent("ApolloConfigException", ExceptionUtil.getDetailMessage(ex));
        transaction.setStatus(ex);
        exception = ex;
      } finally {
        transaction.complete();
      }

      try {
        m_configUtil.getOnErrorRetryIntervalTimeUnit()
            .sleep(m_configUtil.getOnErrorRetryInterval());
      } catch (InterruptedException ex) {
        //ignore
      }
    }

    throw new ApolloConfigException(
        String.format("Get config services failed from %s", url), exception);
  }

  /**
   * 设置配置服务列表
   *
   * @param services 服务信息列表
   */
  private void setConfigServices(List<ServiceDTO> services) {
    // 更新缓存
    m_configServices.set(services);
    // 打印结果 ServiceDTO 数组
    logConfigServices(services);
  }

  /**
   * 组装元服务Url
   *
   * @return 组装好的URL
   */
  private String assembleMetaServiceUrl() {
    String domainName = m_configUtil.getMetaServerDomainName();
    String appId = m_configUtil.getAppId();
    String localIp = m_configUtil.getLocalIp();
    // 查询参数
    Map<String, String> queryParams = Maps.newHashMap();
    // 应用id
    queryParams.put("appId", queryParamEscaper.escape(appId));
    // ip
    if (!Strings.isNullOrEmpty(localIp)) {
      queryParams.put("ip", queryParamEscaper.escape(localIp));
    }

    return domainName + "/services/config?" + MAP_JOINER.join(queryParams);
  }

  /**
   * 配置服务日志
   *
   * @param serviceDtos 服务配置列表
   */
  private void logConfigServices(List<ServiceDTO> serviceDtos) {
    for (ServiceDTO serviceDto : serviceDtos) {
      logConfigService(serviceDto.getHomepageUrl());
    }
  }

  /**
   * 配置服务日志
   *
   * @param serviceUrl 服务url
   */
  private void logConfigService(String serviceUrl) {
    Tracer.logEvent("Apollo.Config.Services", serviceUrl);
  }
}
