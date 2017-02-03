package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.tracer.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreationListener {

  private static Logger logger = LoggerFactory.getLogger(CreationListener.class);

  @Autowired
  private PortalSettings portalSettings;
  @Autowired
  private AdminServiceAPI.AppAPI appAPI;
  @Autowired
  private AdminServiceAPI.NamespaceAPI namespaceAPI;

  @EventListener
  public void onAppCreationEvent(AppCreationEvent event) {
    AppDTO appDTO = BeanUtils.transfrom(AppDTO.class, event.getApp());
    List<Env> envs = portalSettings.getActiveEnvs();
    for (Env env : envs) {
      try {
        appAPI.createApp(env, appDTO);
      } catch (Throwable e) {
        logger.error("call appAPI.createApp error.(appId={appId}, env={env})", appDTO.getAppId(), env, e);
        Tracer.logError(String.format("call appAPI.createApp error. (appId=%s, env=%s)", appDTO.getAppId(), env), e);
      }
    }
  }

  @EventListener
  public void onAppNamespaceCreationEvent(AppNamespaceCreationEvent event) {
    AppNamespaceDTO appNamespace = BeanUtils.transfrom(AppNamespaceDTO.class, event.getAppNamespace());
    List<Env> envs = portalSettings.getActiveEnvs();
    for (Env env : envs) {
      try {
        namespaceAPI.createAppNamespace(env, appNamespace);
      } catch (Throwable e) {
        logger.error("call appAPI.createApp error.(appId={appId}, env={env})", appNamespace.getAppId(), env, e);
        Tracer.logError(String.format("call appAPI.createApp error. (appId=%s, env=%s)", appNamespace.getAppId(), env), e);
      }
    }
  }

}
