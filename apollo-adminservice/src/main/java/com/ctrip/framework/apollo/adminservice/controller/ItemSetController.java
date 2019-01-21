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

@RestController
public class ItemSetController {

  private final ItemSetService itemSetService;

  public ItemSetController(final ItemSetService itemSetService) {
    this.itemSetService = itemSetService;
  }

  @PreAcquireNamespaceLock
  @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/itemset")
  public ResponseEntity<Void> create(@PathVariable String appId, @PathVariable String clusterName,
                                     @PathVariable String namespaceName, @RequestBody ItemChangeSets changeSet) {

    itemSetService.updateSet(appId, clusterName, namespaceName, changeSet);

    return ResponseEntity.status(HttpStatus.OK).build();
  }


}
