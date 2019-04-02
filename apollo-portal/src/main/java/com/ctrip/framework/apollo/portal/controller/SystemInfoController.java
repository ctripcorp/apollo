package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.Apollo;
import com.ctrip.framework.apollo.core.MetaDomainConsts;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.component.RestTemplateFactory;
import com.ctrip.framework.apollo.portal.entity.vo.EnvironmentInfo;
import com.ctrip.framework.apollo.portal.entity.vo.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping("/system-info")
public class SystemInfoController {

  private static final Logger logger = LoggerFactory.getLogger(SystemInfoController.class);
  private static final String CONFIG_SERVICE_URL_PATH = "/services/config";
  private static final String ADMIN_SERVICE_URL_PATH = "/services/admin";

  private RestTemplate restTemplate;
  private final PortalSettings portalSettings;
  private final RestTemplateFactory restTemplateFactory;

  public SystemInfoController(
      final PortalSettings portalSettings,
      final RestTemplateFactory restTemplateFactory) {
    this.portalSettings = portalSettings;
    this.restTemplateFactory = restTemplateFactory;
  }

  @PostConstruct
  private void init() {
    restTemplate = restTemplateFactory.getObject();
  }

  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @GetMapping
  public SystemInfo getSystemInfo() {
    SystemInfo systemInfo = new SystemInfo();

    String version = Apollo.VERSION;
    if (isValidVersion(version)) {
      systemInfo.setVersion(version);
    }

    List<Env> allEnvList = portalSettings.getAllEnvs();

    for (Env env : allEnvList) {
      EnvironmentInfo environmentInfo = adaptEnv2EnvironmentInfo(env);

      systemInfo.addEnvironment(environmentInfo);
    }

    return systemInfo;
  }

  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @GetMapping(value = "/health")
  public Health checkHealth(@RequestParam String instanceId) {
    List<Env> allEnvs = portalSettings.getAllEnvs();

    ServiceDTO service = null;
    for (final Env env : allEnvs) {
      EnvironmentInfo envInfo = adaptEnv2EnvironmentInfo(env);
      for (final ServiceDTO s : envInfo.getAdminServices()) {
        if (instanceId.equals(s.getInstanceId())) {
          service = s;
          break;
        }
      }
      for (final ServiceDTO s : envInfo.getConfigServices()) {
        if (instanceId.equals(s.getInstanceId())) {
          service = s;
          break;
        }
      }
    }

    if (service == null) {
      throw new IllegalArgumentException("No such instance of instanceId: " + instanceId);
    }

    return restTemplate.getForObject(service.getHomepageUrl() + "/health", Health.class);
  }

  private EnvironmentInfo adaptEnv2EnvironmentInfo(final Env env) {
    EnvironmentInfo environmentInfo = new EnvironmentInfo();
    String metaServerAddresses = MetaDomainConsts.getMetaServerAddress(env);

    environmentInfo.setEnv(env);
    environmentInfo.setActive(portalSettings.isEnvActive(env));
    environmentInfo.setMetaServerAddress(metaServerAddresses);

    String selectedMetaServerAddress = MetaDomainConsts.getDomain(env);
    try {
      environmentInfo.setConfigServices(getServerAddress(selectedMetaServerAddress, CONFIG_SERVICE_URL_PATH));

      environmentInfo.setAdminServices(getServerAddress(selectedMetaServerAddress, ADMIN_SERVICE_URL_PATH));
    } catch (Throwable ex) {
      String errorMessage = "Loading config/admin services from meta server: " + selectedMetaServerAddress + " failed!";
      logger.error(errorMessage, ex);
      environmentInfo.setErrorMessage(errorMessage + " Exception: " + ex.getMessage());
    }
    return environmentInfo;
  }

  private ServiceDTO[] getServerAddress(String metaServerAddress, String path) {
    String url = metaServerAddress + path;
    return restTemplate.getForObject(url, ServiceDTO[].class);
  }

  private boolean isValidVersion(String version) {
    return !version.equals("java-null");
  }
}
