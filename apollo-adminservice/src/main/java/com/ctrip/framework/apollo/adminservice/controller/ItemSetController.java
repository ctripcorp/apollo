package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.adminservice.aop.PreAcquireNamespaceLock;
import com.ctrip.framework.apollo.biz.service.ItemSetService;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置项变化集合 Controller层
 */
@RestController
public class ItemSetController {

  private final ItemSetService itemSetService;

  public ItemSetController(final ItemSetService itemSetService) {
    this.itemSetService = itemSetService;
  }

  /**
   * 更新配置项
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param changeSet     改变的配置项
   * @return 配置项创建、更新、删除的列表数据
   */
  @PreAcquireNamespaceLock
  @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/itemset")
  public ResponseEntity<Void> create(@PathVariable String appId, @PathVariable String clusterName,
      @PathVariable String namespaceName, @RequestBody ItemChangeSets changeSet) {

    // 更新配置项
    itemSetService.updateSet(appId, clusterName, namespaceName, changeSet);
    return ResponseEntity.status(HttpStatus.OK).build();
  }


}
