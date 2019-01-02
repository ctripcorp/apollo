package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.tracer.Tracer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DeletionListener {

  private static final Logger logger = LoggerFactory.getLogger(DeletionListener.class);

  @Autowired
  private PortalSettings portalSettings;
  @Autowired
  private AdminServiceAPI.AppAPI appAPI;
  @Autowired
  private AdminServiceAPI.NamespaceAPI namespaceAPI;

  @EventListener
  public void onAppDeletionEvent(AppDeletionEvent event) {
    AppDTO appDTO = BeanUtils.transform(AppDTO.class, event.getApp());
    String appId = appDTO.getAppId();
    String operator = appDTO.getDataChangeLastModifiedBy();

    List<Env> envs = portalSettings.getActiveEnvs();
    for (Env env : envs) {
      try {
        appAPI.deleteApp(env, appId, operator);
      } catch (Throwable e) {
        logger.error("Delete app failed. Env = {}, AppId = {}", env, appId, e);
        Tracer.logError(String.format("Delete app failed. Env = %s, AppId = %s", env, appId), e);
      }
    }
  }

  @EventListener
  public void onAppNamespaceDeletionEvent(AppNamespaceDeletionEvent event) {
    AppNamespaceDTO appNamespace = BeanUtils.transform(AppNamespaceDTO.class, event.getAppNamespace());
    List<Env> envs = portalSettings.getActiveEnvs();
    String appId = appNamespace.getAppId();
    String namespaceName = appNamespace.getName();
    String operator = appNamespace.getDataChangeLastModifiedBy();

    for (Env env : envs) {
      try {
        namespaceAPI.deleteAppNamespace(env, appId, namespaceName, operator);
      } catch (Throwable e) {
        logger.error("Delete appNamespace failed. appId = {}, namespace = {}, env = {}", appId, namespaceName, env, e);
        Tracer.logError(String
            .format("Delete appNamespace failed. appId = %s, namespace = %s, env = %s", appId, namespaceName, env), e);
      }
    }
  }
}
