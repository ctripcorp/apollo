package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.dto.AppNamespaceDTO;
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
 * 对象删除 监听器
 */
@Slf4j
@Component
public class DeletionListener {

  private final PortalSettings portalSettings;
  private final AdminServiceAPI.AppAPI appAPI;
  private final AdminServiceAPI.NamespaceAPI namespaceAPI;

  public DeletionListener(
      final PortalSettings portalSettings,
      final AdminServiceAPI.AppAPI appAPI,
      final AdminServiceAPI.NamespaceAPI namespaceAPI) {
    this.portalSettings = portalSettings;
    this.appAPI = appAPI;
    this.namespaceAPI = namespaceAPI;
  }

  /**
   * 监听应用删除事件
   *
   * @param event 应用删除事件
   */
  @EventListener
  public void onAppDeletionEvent(AppDeletionEvent event) {
    // 将 App 转成 AppDTO 对象
    AppDTO appDTO = BeanUtils.transform(AppDTO.class, event.getApp());

    String appId = appDTO.getAppId();
    // 循环 Env 数组，调用对应的 Admin Service 的 API ，创建 App 对象
    String operator = appDTO.getDataChangeLastModifiedBy();
    // 获得有效的 Env 数组
    List<Env> envs = portalSettings.getActiveEnvs();
    // 循环 Env 数组，调用对应的 Admin Service 的 API ，删除App 对象
    for (Env env : envs) {
      try {
        appAPI.deleteApp(env, appId, operator);
      } catch (Throwable e) {
        log.error("Delete app failed. Env = {}, AppId = {}", env, appId, e);
        Tracer.logError(String.format("Delete app failed. Env = %s, AppId = %s", env, appId), e);
      }
    }
  }

  /**
   * 监听应用名称空间删除事件
   *
   * @param event 应用名称空间删除事件
   */
  @EventListener
  public void onAppNamespaceDeletionEvent(AppNamespaceDeletionEvent event) {
    // 将 AppNamespace 转成 AppNamespaceDTO 对象
    AppNamespaceDTO appNamespace = BeanUtils.transform(AppNamespaceDTO.class,
        event.getAppNamespace());
    // 获得有效的 Env 数组
    List<Env> envs = portalSettings.getActiveEnvs();
    String appId = appNamespace.getAppId();
    String namespaceName = appNamespace.getName();
    String operator = appNamespace.getDataChangeLastModifiedBy();

    // 循环 Env 数组，调用对应的 Admin Service 的 API ，删除 AppNamespace 对象
    for (Env env : envs) {
      try {
        namespaceAPI.deleteAppNamespace(env, appId, namespaceName, operator);
      } catch (Throwable e) {
        log.error("Delete appNamespace failed. appId = {}, namespace = {}, env = {}", appId,
            namespaceName, env, e);
        Tracer.logError(String
            .format("Delete appNamespace failed. appId = %s, namespace = %s, env = %s", appId,
                namespaceName, env), e);
      }
    }
  }
}
