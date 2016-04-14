package com.ctrip.apollo.portal.controller;

import com.google.common.base.Strings;

import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.entity.ClusterNavTree;
import com.ctrip.apollo.portal.service.AppService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apps")
public class AppController {

  @Autowired
  private AppService appService;

  @RequestMapping("/{appId}/navtree")
  public ClusterNavTree nav(@PathVariable String appId) {
    if (Strings.isNullOrEmpty(appId)) {
      throw new IllegalArgumentException("app id can not be empty.");
    }

    return appService.buildClusterNavTree(appId);
  }

  @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json"})
  public ResponseEntity create(@RequestBody AppDTO app) {
    if (isInvalidApp(app)){
      return ResponseEntity.badRequest().build();
    }
    if (appService.save(app)){
      return ResponseEntity.ok().build();
    }else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  private boolean isInvalidApp(AppDTO app) {
    return StringUtils.isContainEmpty(app.getName(), app.getAppId(), app.getOwnerEmail(), app.getOwnerName());
  }



}

