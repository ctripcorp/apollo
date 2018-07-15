package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.tracer.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppInfoChangedListener {
  private static final Logger logger = LoggerFactory.getLogger(AppInfoChangedListener.class);

  @Autowired
  private AdminServiceAPI.AppAPI appAPI;
  @Autowired
  private PortalSettings portalSettings;
  @Autowired
  private UserInfoHolder userInfoHolder;

  @EventListener
  public void onAppInfoChange(AppInfoChangedEvent event) {
    AppDTO appDTO = BeanUtils.transfrom(AppDTO.class, event.getApp());
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

  @EventListener
  public void onAppDelete(AppDeletionEvent event) {
    AppDTO appDTO = BeanUtils.transfrom(AppDTO.class, event.getApp());
    String appId = appDTO.getAppId();
    String operator = userInfoHolder.getUser().getName();

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
}
