package com.ctrip.apollo.portal.controller;


import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.core.dto.ReleaseDTO;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.entity.form.NamespaceModifyModel;
import com.ctrip.apollo.portal.entity.NamespaceVO;
import com.ctrip.apollo.portal.entity.SimpleMsg;
import com.ctrip.apollo.portal.entity.form.NamespaceReleaseModel;
import com.ctrip.apollo.portal.service.ConfigService;
import com.ctrip.apollo.portal.service.txtresolver.TextResolverResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("")
public class ConfigController {

  @Autowired
  private ConfigService configService;

  @RequestMapping("/apps/{appId}/env/{env}/clusters/{clusterName}/namespaces")
  public List<NamespaceVO> findNamespaces(@PathVariable String appId, @PathVariable String env,
                                          @PathVariable String clusterName) {
    if (StringUtils.isContainEmpty(appId, env, clusterName)) {
      throw new IllegalArgumentException("app id and cluster name can not be empty");
    }

    return configService.findNampspaces(appId, Apollo.Env.valueOf(env), clusterName);
  }

  @RequestMapping(value = "/namespace/modify",method = RequestMethod.POST, consumes = {"application/json"})
  public ResponseEntity<SimpleMsg> modifyConfigs(@RequestBody NamespaceModifyModel model) {

    if (model == null || model.isInvalid()){
      return ResponseEntity.badRequest().body(new SimpleMsg("form data exception."));
    }

    TextResolverResult result = configService.resolveConfigText(model);

    if (result.isResolveSuccess()) {
      return ResponseEntity.ok().body(new SimpleMsg("success"));
    } else {
      return ResponseEntity.badRequest().body(new SimpleMsg(result.getMsg()));
    }
  }

  @RequestMapping(value = "/namespace/release", method = RequestMethod.POST, consumes = {"application/json"})
  public ResponseEntity<SimpleMsg> release(@RequestBody NamespaceReleaseModel model){
    if (model == null || model.isInvalid()){
      return ResponseEntity.badRequest().body(new SimpleMsg("form data exception."));
    }

    ReleaseDTO release = configService.release(model);

    if (release == null){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SimpleMsg("oops! some error in server."));
    }else {
      return ResponseEntity.ok().body(new SimpleMsg("success"));
    }
  }

}
