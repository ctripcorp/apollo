package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.NamespaceLockDTO;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.vo.LockInfo;
import com.ctrip.framework.apollo.portal.environment.Env;
import org.springframework.stereotype.Service;

/**
 * 名称空间锁 Service
 */
@Service
public class NamespaceLockService {

  private final AdminServiceAPI.NamespaceLockAPI namespaceLockAPI;
  private final PortalConfig portalConfig;

  public NamespaceLockService(final AdminServiceAPI.NamespaceLockAPI namespaceLockAPI,
      final PortalConfig portalConfig) {
    this.namespaceLockAPI = namespaceLockAPI;
    this.portalConfig = portalConfig;
  }

  /**
   * 获取名称空间锁信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 名称空间锁信息
   */
  public NamespaceLockDTO getNamespaceLock(String appId, Env env, String clusterName,
      String namespaceName) {
    return namespaceLockAPI.getNamespaceLockOwner(appId, env, clusterName, namespaceName);
  }

  /**
   * 获取名称空间锁信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 名称空间锁信息
   */
  public LockInfo getNamespaceLockInfo(String appId, Env env, String clusterName,
      String namespaceName) {
    LockInfo lockInfo = new LockInfo();

    NamespaceLockDTO namespaceLockDTO = namespaceLockAPI.getNamespaceLockOwner(appId, env,
        clusterName, namespaceName);
    String lockOwner = namespaceLockDTO == null ? "" : namespaceLockDTO.getDataChangeCreatedBy();
    lockInfo.setLockOwner(lockOwner);

    lockInfo.setEmergencyPublishAllowed(portalConfig.isEmergencyPublishAllowed(env));

    return lockInfo;
  }

}
