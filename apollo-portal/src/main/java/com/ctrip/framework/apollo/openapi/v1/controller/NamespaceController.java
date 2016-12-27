package com.ctrip.framework.apollo.openapi.v1.controller;


import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceLockDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceLockDTO;
import com.ctrip.framework.apollo.openapi.util.OpenApiBeanUtils;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.service.NamespaceLockService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("openapiNamespaceController")
@RequestMapping("/openapi/v1/envs/{env}")
public class NamespaceController {

  @Autowired
  private NamespaceLockService namespaceLockService;
  @Autowired
  private NamespaceService namespaceService;

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces", method = RequestMethod.GET)
  public List<OpenNamespaceDTO> findNamespaces(@PathVariable String appId, @PathVariable String env,
                                               @PathVariable String clusterName) {

    return OpenApiBeanUtils
        .batchTransformFromNamespaceBOs(namespaceService.findNamespaceBOs(appId, Env
            .fromString(env), clusterName));
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName:.+}", method = RequestMethod.GET)
  public OpenNamespaceDTO loadNamespace(@PathVariable String appId, @PathVariable String env,
                                        @PathVariable String clusterName, @PathVariable String
                                            namespaceName) {
    NamespaceBO namespaceBO = namespaceService.loadNamespaceBO(appId, Env.fromString
        (env), clusterName, namespaceName);
    if (namespaceBO == null) {
      return null;
    }
    return OpenApiBeanUtils.transformFromNamespaceBO(namespaceBO);
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/lock", method = RequestMethod.GET)
  public OpenNamespaceLockDTO getNamespaceLock(@PathVariable String appId, @PathVariable String env,
                                               @PathVariable String clusterName, @PathVariable
                                                   String namespaceName) {

    NamespaceDTO namespace = namespaceService.loadNamespaceBaseInfo(appId, Env
        .fromString(env), clusterName, namespaceName);
    NamespaceLockDTO lockDTO = namespaceLockService.getNamespaceLock(appId, Env
        .fromString(env), clusterName, namespaceName);
    return OpenApiBeanUtils.transformFromNamespaceLockDTO(namespace.getNamespaceName(), lockDTO);
  }

}
