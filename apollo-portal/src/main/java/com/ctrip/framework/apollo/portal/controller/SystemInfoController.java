package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.Apollo;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.component.RestTemplateFactory;
import com.ctrip.framework.apollo.portal.entity.vo.EnvironmentInfo;
import com.ctrip.framework.apollo.portal.entity.vo.SystemInfo;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.environment.PortalMetaDomainService;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * 系统信息 Controller
 */
@Slf4j
@RestController
@RequestMapping("/system-info")
public class SystemInfoController {

  /**
   * 配置服务URL路径.
   */
  private static final String CONFIG_SERVICE_URL_PATH = "/services/config";
  /**
   * 管理服务URL路径.
   */
  private static final String ADMIN_SERVICE_URL_PATH = "/services/admin";

  private RestTemplate restTemplate;
  private final PortalSettings portalSettings;
  private final RestTemplateFactory restTemplateFactory;
  private final PortalMetaDomainService portalMetaDomainService;

  public SystemInfoController(
      final PortalSettings portalSettings,
      final RestTemplateFactory restTemplateFactory,
      final PortalMetaDomainService portalMetaDomainService
  ) {
    this.portalSettings = portalSettings;
    this.restTemplateFactory = restTemplateFactory;
    this.portalMetaDomainService = portalMetaDomainService;
  }

  /**
   * 初始化restTemplate
   */
  @PostConstruct
  private void init() {
    restTemplate = restTemplateFactory.getObject();
  }

  /**
   * 获取系统信息
   *
   * @return 系统信息
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @GetMapping
  public SystemInfo getSystemInfo() {
    SystemInfo systemInfo = new SystemInfo();

    String version = Apollo.VERSION;
    //设置版本
    if (isValidVersion(version)) {
      systemInfo.setVersion(version);
    }

    List<Env> allEnvList = portalSettings.getAllEnvs();

    //设置环境信息
    for (Env env : allEnvList) {
      EnvironmentInfo environmentInfo = adaptEnv2EnvironmentInfo(env);

      systemInfo.addEnvironment(environmentInfo);
    }

    return systemInfo;
  }

  /**
   * 健康检查
   *
   * @param instanceId 实例id
   * @return 健康信息
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @GetMapping(value = "/health")
  public Health checkHealth(@RequestParam String instanceId) {
    List<Env> allEnvs = portalSettings.getAllEnvs();

    // 匹配实例id
    ServiceDTO service = null;
    for (final Env env : allEnvs) {
      EnvironmentInfo envInfo = adaptEnv2EnvironmentInfo(env);
      if (envInfo.getAdminServices() != null) {
        for (final ServiceDTO s : envInfo.getAdminServices()) {
          if (instanceId.equals(s.getInstanceId())) {
            service = s;
            break;
          }
        }
      }
      if (envInfo.getConfigServices() != null) {
        for (final ServiceDTO s : envInfo.getConfigServices()) {
          if (instanceId.equals(s.getInstanceId())) {
            service = s;
            break;
          }
        }
      }
    }

    // 为空校验
    if (service == null) {
      throw new IllegalArgumentException("No such instance of instanceId: " + instanceId);
    }

    // 获取指定环境的健康信息
    return restTemplate.getForObject(service.getHomepageUrl() + "/health", Health.class);
  }

  /**
   * 甜酸环境
   *
   * @param env 环境类型
   * @return 环境信息
   */
  private EnvironmentInfo adaptEnv2EnvironmentInfo(final Env env) {
    EnvironmentInfo environmentInfo = new EnvironmentInfo();
    // 元服务地址信息
    String metaServerAddresses = portalMetaDomainService.getMetaServerAddress(env);

    environmentInfo.setEnv(env.getName());
    environmentInfo.setActive(portalSettings.isEnvActive(env));
    environmentInfo.setMetaServerAddress(metaServerAddresses);

    // 选中的元服务器地址
    String selectedMetaServerAddress = portalMetaDomainService.getDomain(env);
    try {
      environmentInfo
          .setConfigServices(getServerAddress(selectedMetaServerAddress, CONFIG_SERVICE_URL_PATH));

      environmentInfo
          .setAdminServices(getServerAddress(selectedMetaServerAddress, ADMIN_SERVICE_URL_PATH));
    } catch (Throwable ex) {
      String errorMessage =
          "Loading config/admin services from meta server: " + selectedMetaServerAddress
              + " failed!";
      log.error(errorMessage, ex);
      environmentInfo.setErrorMessage(errorMessage + " Exception: " + ex.getMessage());
    }
    return environmentInfo;
  }

  /**
   * 获取服务信息
   *
   * @param metaServerAddress 元服务地址
   * @param path              路径
   * @return 服务信息
   */
  private ServiceDTO[] getServerAddress(String metaServerAddress, String path) {
    String url = metaServerAddress + path;
    return restTemplate.getForObject(url, ServiceDTO[].class);
  }

  /**
   * 是否为有效版本
   *
   * @param version 版本
   * @return true, 是有效版本，否则 ，false
   */
  private boolean isValidVersion(String version) {
    return !version.equals("java-null");
  }
}
