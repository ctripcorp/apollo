package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.portal.environment.Env;

public class EnvironmentInfo {

  private String env;
  private boolean active;
  private String metaServerAddress;

  private ServiceDTO[] configServices;
  private ServiceDTO[] adminServices;

  private String errorMessage;

  public Env getEnv() {
    return Env.valueOf(env);
  }

  public void setEnv(Env env) {
    this.env = env.toString();
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getMetaServerAddress() {
    return metaServerAddress;
  }

  public void setMetaServerAddress(String metaServerAddress) {
    this.metaServerAddress = metaServerAddress;
  }

  public ServiceDTO[] getConfigServices() {
    return configServices;
  }

  public void setConfigServices(ServiceDTO[] configServices) {
    this.configServices = configServices;
  }

  public ServiceDTO[] getAdminServices() {
    return adminServices;
  }

  public void setAdminServices(ServiceDTO[] adminServices) {
    this.adminServices = adminServices;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
