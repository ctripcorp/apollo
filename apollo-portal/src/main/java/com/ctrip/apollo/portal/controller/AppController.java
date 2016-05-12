package com.ctrip.apollo.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import com.ctrip.apollo.common.http.MultiResponseEntity;
import com.ctrip.apollo.common.http.RichResponseEntity;
import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.exception.BadRequestException;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.PortalSettings;
import com.ctrip.apollo.portal.entity.EnvNavNode;
import com.ctrip.apollo.portal.service.AppService;

import java.util.List;

@RestController
@RequestMapping("/apps")
public class AppController {

  @Autowired
  private AppService appService;

  @Autowired
  private PortalSettings portalSettings;

  @RequestMapping("/envs/{env}")
  public List<AppDTO> findAllApp(@PathVariable String env){
    if (StringUtils.isEmpty(env)){
      throw new BadRequestException("env can not be empty");
    }
    return appService.findAll(Env.valueOf(env));
  }

  @RequestMapping("/{appId}/navtree")
  public MultiResponseEntity<EnvNavNode> nav(@PathVariable String appId) {

    if (StringUtils.isEmpty(appId)) {
      throw new BadRequestException("app id can not be empty.");
    }
    MultiResponseEntity<EnvNavNode> response = MultiResponseEntity.instance();
    List<Env> envs = portalSettings.getEnvs();
    for (Env env : envs) {
      try {
        response.addResponseEntity(RichResponseEntity.ok(appService.createEnvNavNode(env, appId)));
      } catch (Exception e) {
        response.addResponseEntity(RichResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR,
                                                            "load env:" + env.name() + " cluster error." + e
                                                                .getMessage()));
      }
    }
    return response;
  }

  @RequestMapping(value = "/envs/{env}", method = RequestMethod.POST, consumes = {"application/json"})
  public ResponseEntity<Void> create(@PathVariable String env, @RequestBody AppDTO app) {
    if (isInvalidApp(app)){
      throw new BadRequestException("request payload contains empty");
    }
    if ("ALL".equals(env)){
      appService.createAppInAllEnvs(app);
    } else {
      appService.createApp(Env.valueOf(env), app);
    }
    return ResponseEntity.ok().build();
  }

  @RequestMapping(value = "/{appId}", method = RequestMethod.GET)
  public AppDTO load(@PathVariable String appId){
    if (StringUtils.isEmpty(appId)){
      throw new BadRequestException("app id can not be empty.");
    }
    return appService.load(appId);
  }

  @RequestMapping(value = "/{appId}/miss_envs")
  public MultiResponseEntity<Env> findMissEnvs(@PathVariable String appId) {
    MultiResponseEntity<Env> response = MultiResponseEntity.instance();
    for (Env env : portalSettings.getEnvs()) {
      try {
        appService.load(env, appId);
      } catch (Exception e) {
        if (e instanceof HttpClientErrorException &&
            ((HttpClientErrorException)e).getStatusCode() == HttpStatus.NOT_FOUND){
          response.addResponseEntity(RichResponseEntity.ok(env));
        } else {
          response.addResponseEntity(RichResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR,
                                                              String
                                                                  .format("load appId:%s from env %s error.", appId,
                                                                          env)
                                                              + e.getMessage()));
        }
      }

    }

    return response;

  }

  private boolean isInvalidApp(AppDTO app) {
    return StringUtils.isContainEmpty(app.getName(), app.getAppId(), app.getOwnerEmail(), app.getOwnerName());
  }
}

