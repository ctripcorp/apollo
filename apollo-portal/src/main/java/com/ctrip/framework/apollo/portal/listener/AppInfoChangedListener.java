package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.tracer.Tracer;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用信息变更监听器
 */
@Slf4j
@Component
public class AppInfoChangedListener {


  private final AdminServiceAPI.AppAPI appAPI;
  private final PortalSettings portalSettings;

  public AppInfoChangedListener(final AdminServiceAPI.AppAPI appAPI,
      final PortalSettings portalSettings) {
    this.appAPI = appAPI;
    this.portalSettings = portalSettings;
  }

  /**
   * 监听应用信息变更
   *
   * @param event 应用创建事件
   */
  @EventListener
  public void onAppInfoChange(AppInfoChangedEvent event) {
    // 将 App 转成 AppDTO 对象
    AppDTO appDTO = BeanUtils.transform(AppDTO.class, event.getApp());
    String appId = appDTO.getAppId();
    // 获得有效的 Env 数组
    List<Env> envs = portalSettings.getActiveEnvs();
    // 循环 Env 数组，调用对应的 Admin Service 的 API ，更新 App 对象
    for (Env env : envs) {
      try {
        appAPI.updateApp(env, appDTO);
      } catch (Throwable e) {
        log.error("Update app's info failed. Env = {}, AppId = {}", env, appId, e);
        Tracer.logError(String.format("Update app's info failed. Env = %s, AppId = %s", env, appId),
            e);
      }
    }
  }
}
