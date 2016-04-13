package com.ctrip.apollo.adminservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.service.ItemSetService;
import com.ctrip.apollo.core.dto.ItemChangeSets;

@RestController
public class ItemSetController {

  @Autowired
  private ItemSetService itemSetService;

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/itemset", method = RequestMethod.POST)
  public ResponseEntity<Void> create(@PathVariable String appId, @PathVariable String clusterName,
                                     @PathVariable String namespaceName, @RequestBody ItemChangeSets changeSet) {
    itemSetService.updateSet(appId, clusterName, namespaceName, changeSet);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
