package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.tracer.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppInfoChangedListener {
  private static final Logger logger = LoggerFactory.getLogger(AppInfoChangedListener.class);

  private final AdminServiceAPI.AppAPI appAPI;
  private final PortalSettings portalSettings;

  public AppInfoChangedListener(final AdminServiceAPI.AppAPI appAPI, final PortalSettings portalSettings) {
    this.appAPI = appAPI;
    this.portalSettings = portalSettings;
  }

  @EventListener
  public void onAppInfoChange(AppInfoChangedEvent event) {
    AppDTO appDTO = BeanUtils.transform(AppDTO.class, event.getApp());
    String appId = appDTO.getAppId();

    List<Env> envs = portalSettings.getActiveEnvs();
    for (Env env : envs) {
      try {
        appAPI.updateApp(env, appDTO);
      } catch (Throwable e) {
        logger.error("Update app's info failed. Env = {}, AppId = {}", env, appId, e);
        Tracer.logError(String.format("Update app's info failed. Env = %s, AppId = %s", env, appId), e);
      }
    }
  }
}
