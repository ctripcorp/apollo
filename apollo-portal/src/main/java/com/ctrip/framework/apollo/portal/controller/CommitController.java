package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.CommitDTO;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.service.CommitService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class CommitController {

  @Autowired
  private CommitService commitService;

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/commits", method = RequestMethod.GET)
  public List<CommitDTO> find(@PathVariable String appId, @PathVariable String env,
                              @PathVariable String clusterName, @PathVariable String namespaceName,
                              @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

    RequestPrecondition.checkNumberPositive(size);
    RequestPrecondition.checkNumberNotNegative(page);

    return commitService.find(appId, Env.valueOf(env), clusterName, namespaceName, page, size);

  }

}
