package com.ctrip.apollo.portal.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import com.ctrip.apollo.common.utils.ExceptionUtils;
import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.exception.BadRequestException;
import com.ctrip.apollo.core.exception.ServiceException;
import com.ctrip.apollo.portal.PortalSettings;
import com.ctrip.apollo.portal.api.AdminServiceAPI;
import com.ctrip.apollo.portal.entity.EnvNavNode;

@Service
public class AppService {

  private Logger logger = LoggerFactory.getLogger(AppService.class);

  @Autowired
  private PortalSettings portalSettings;

  @Autowired
  private AdminServiceAPI.AppAPI appAPI;

  @Autowired ClusterService clusterService;

  public List<AppDTO> findAll(Env env) {
    return appAPI.findApps(env);
  }

  public AppDTO load(String appId) {
    //轮询环境直到能找到此app的信息
    AppDTO app = null;
    boolean isCallAdminServiceError = false;
    for (Env env : portalSettings.getEnvs()) {
      try {
        app = appAPI.loadApp(env, appId);
        break;
      } catch (HttpClientErrorException e) {
        //not exist maybe because create app fail.
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
          logger.warn("app:{} in {} not exist", appId, env);
        } else {
          isCallAdminServiceError = true;
          logger.error("load app info({}) from env:{} error.", appId, env);
        }
      }
    }
    if (app == null) {
      if (isCallAdminServiceError){
        throw new ServiceException("call admin service error");
      }else {
        throw new BadRequestException(String.format("invalid app id %s", appId));
      }
    }

    return app;
  }

  public AppDTO load(Env env, String appId){
    return appAPI.loadApp(env, appId);
  }

  public void createAppInAllEnvs(AppDTO app) {
    List<Env> envs = portalSettings.getEnvs();
    for (Env env : envs) {
      try {
        appAPI.createApp(env, app);
      } catch (HttpStatusCodeException e) {
        logger.error(ExceptionUtils.toString(e));
        throw e;
      }
    }
  }

  public void createApp(Env env, AppDTO app) {
    try {
      appAPI.createApp(env, app);
    } catch (HttpStatusCodeException e) {
      logger.error(ExceptionUtils.toString(e));
      throw e;
    }
  }

  public EnvNavNode createEnvNavNode(Env env, String appId){
    EnvNavNode node = new EnvNavNode(env);
    node.setClusters(clusterService.findClusters(env, appId));
    return node;
  }

}
