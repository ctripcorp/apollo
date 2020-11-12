package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.NamespaceLockDTO;
import com.ctrip.framework.apollo.portal.entity.vo.LockInfo;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.NamespaceLockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 名称空间锁 Controller
 */
@RestController
public class NamespaceLockController {

  private final NamespaceLockService namespaceLockService;

  public NamespaceLockController(final NamespaceLockService namespaceLockService) {
    this.namespaceLockService = namespaceLockService;
  }

  /**
   * 获取名称空间锁
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 获取的名称空间锁信息
   */
  @Deprecated
  @GetMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/lock")
  public NamespaceLockDTO getNamespaceLock(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName) {
    return namespaceLockService
        .getNamespaceLock(appId, Env.valueOf(env), clusterName, namespaceName);
  }

  /**
   * 获取名称空间锁
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 获取的名称空间锁信息
   */
  @GetMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/lock-info")
  public LockInfo getNamespaceLockInfo(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName) {
    return namespaceLockService.getNamespaceLockInfo(appId, Env.valueOf(env), clusterName,
        namespaceName);

  }


}
