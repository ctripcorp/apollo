package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.NamespaceLock;
import com.ctrip.framework.apollo.biz.service.NamespaceLockService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.common.dto.NamespaceLockDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 名称空间锁 Controller层
 */
@RestController
public class NamespaceLockController {

  private final NamespaceLockService namespaceLockService;
  private final NamespaceService namespaceService;
  private final BizConfig bizConfig;

  public NamespaceLockController(
      final NamespaceLockService namespaceLockService,
      final NamespaceService namespaceService,
      final BizConfig bizConfig) {
    this.namespaceLockService = namespaceLockService;
    this.namespaceService = namespaceService;
    this.bizConfig = bizConfig;
  }

  /**
   * 获取名称空间锁信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 名称空间锁信息
   */
  @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/lock")
  public NamespaceLockDTO getNamespaceLockOwner(@PathVariable String appId,
      @PathVariable String clusterName, @PathVariable String namespaceName) {
    //  指定的名称空间
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace == null) {
      throw new BadRequestException("namespace not exist.");
    }

    // 是否关闭名称空间锁
    if (bizConfig.isNamespaceLockSwitchOff()) {
      return null;
    }

    // 获取名称空间锁信息
    NamespaceLock lock = namespaceLockService.findLock(namespace.getId());

    if (lock == null) {
      return null;
    }

    return BeanUtils.transform(NamespaceLockDTO.class, lock);
  }

}
