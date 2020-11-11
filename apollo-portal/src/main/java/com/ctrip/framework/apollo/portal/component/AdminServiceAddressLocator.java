package com.ctrip.framework.apollo.portal.component;

import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.environment.PortalMetaDomainService;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

/**
 * 管理服务的地址定位器,主要是刷新服务器信息缓存<环境，服务地址>
 */
@Slf4j
@Component
public class AdminServiceAddressLocator {

  /**
   * 正常刷新间隔，默认5分钟
   */
  private static final long NORMAL_REFRESH_INTERVAL = TimeUnit.MINUTES.toMillis(5);
  /**
   * 离线刷新间隔，默认为10秒
   */
  private static final long OFFLINE_REFRESH_INTERVAL = TimeUnit.SECONDS.toMillis(10);
  /**
   * 重新次数，默认为3
   */
  private static final int RETRY_TIMES = 3;
  /**
   * 管理服务的URL路径
   */
  private static final String ADMIN_SERVICE_URL_PATH = "/services/admin";

  /**
   * 刷新服务地址的定时任务线程池
   */
  private ScheduledExecutorService refreshServiceAddressService;
  /**
   * rest服务请求模板
   */
  private RestTemplate restTemplate;
  /**
   * 所有的环境列表
   */
  private List<Env> allEnvs;
  /**
   * 服务缓存<环境，服务地址>
   */
  private Map<Env, List<ServiceDTO>> cache = new ConcurrentHashMap<>();

  private final PortalSettings portalSettings;
  private final RestTemplateFactory restTemplateFactory;
  private final PortalMetaDomainService portalMetaDomainService;

  public AdminServiceAddressLocator(
      final HttpMessageConverters httpMessageConverters,
      final PortalSettings portalSettings,
      final RestTemplateFactory restTemplateFactory,
      final PortalMetaDomainService portalMetaDomainService
  ) {
    this.portalSettings = portalSettings;
    this.restTemplateFactory = restTemplateFactory;
    this.portalMetaDomainService = portalMetaDomainService;
  }

  @PostConstruct
  public void init() {
    allEnvs = portalSettings.getAllEnvs();

    // 初始化restTemplate
    restTemplate = restTemplateFactory.getObject();

    // 构建刷新服务地址的服务
    refreshServiceAddressService = Executors.newScheduledThreadPool(1,
        ApolloThreadFactory.create("ServiceLocator", true));
    refreshServiceAddressService.schedule(new RefreshAdminServerAddressTask(), 1,
        TimeUnit.MILLISECONDS);
  }

  /**
   * 获取指定环境下的服务列表
   *
   * @param env 环境
   * @return 指定环境下的服务列表
   */
  public List<ServiceDTO> getServiceList(Env env) {
    List<ServiceDTO> services = cache.get(env);
    if (CollectionUtils.isEmpty(services)) {
      return Collections.emptyList();
    }
    List<ServiceDTO> randomConfigServices = Lists.newArrayList(services);
    Collections.shuffle(randomConfigServices);
    return randomConfigServices;
  }

  // 维护管理服务器地址

  /**
   * 刷新管理服务地址任务
   */
  private class RefreshAdminServerAddressTask implements Runnable {

    @Override
    public void run() {
      boolean refreshSuccess = true;
      // 如果获取任何环境管理服务的地址失败，则刷新失败
      for (Env env : allEnvs) {
        boolean currentEnvRefreshResult = refreshServerAddressCache(env);
        refreshSuccess = refreshSuccess && currentEnvRefreshResult;
      }

      if (refreshSuccess) {
        refreshServiceAddressService.schedule(new RefreshAdminServerAddressTask(),
            NORMAL_REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
      } else {
        refreshServiceAddressService.schedule(new RefreshAdminServerAddressTask(),
            OFFLINE_REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
      }
    }
  }

  /**
   * 刷新服务地址缓存
   *
   * @param env 环境
   * @return true, 刷新成功，否则，刷新失败
   */
  private boolean refreshServerAddressCache(Env env) {

    for (int i = 0; i < RETRY_TIMES; i++) {

      try {
        //添加指定环境中管理服务的地址
        ServiceDTO[] services = getAdminServerAddress(env);
        if (ArrayUtils.isEmpty(services)) {
          continue;
        }
        cache.put(env, Arrays.asList(services));
        return true;
      } catch (Throwable e) {
        log.error(String.format(
            "Get admin server address from meta server failed. env: %s, meta server address:%s",
            env, portalMetaDomainService.getDomain(env)), e);
        Tracer
            .logError(String.format(
                "Get admin server address from meta server failed. env: %s, meta server address:%s",
                env, portalMetaDomainService.getDomain(env)), e);
      }
    }
    return false;
  }

  /**
   * 获取指定环境的管理服务的地址
   *
   * @param env 环境
   * @return 指定环境的管理服务的地址
   */
  private ServiceDTO[] getAdminServerAddress(Env env) {
    String domainName = portalMetaDomainService.getDomain(env);
    String url = domainName + ADMIN_SERVICE_URL_PATH;
    return restTemplate.getForObject(url, ServiceDTO[].class);
  }


}
