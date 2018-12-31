package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.service.ClusterService;
import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.InputValidator;

import com.ctrip.framework.apollo.core.ConfigConsts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClusterController {

  @Autowired
  private ClusterService clusterService;

  @RequestMapping(path = "/apps/{appId}/clusters", method = RequestMethod.POST)
  public ClusterDTO create(@PathVariable("appId") String appId,
                           @RequestParam(value = "autoCreatePrivateNamespace", defaultValue = "true") boolean autoCreatePrivateNamespace,
                           @RequestBody ClusterDTO dto) {
    if (!InputValidator.isValidClusterNamespace(dto.getName())) {
      throw new BadRequestException(String.format("Cluster格式错误: %s", InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE));
    }

    Cluster entity = BeanUtils.transform(Cluster.class, dto);
    Cluster managedEntity = clusterService.findOne(appId, entity.getName());
    if (managedEntity != null) {
      throw new BadRequestException("cluster already exist.");
    }

    if (autoCreatePrivateNamespace) {
      entity = clusterService.saveWithInstanceOfAppNamespaces(entity);
    } else {
      entity = clusterService.saveWithoutInstanceOfAppNamespaces(entity);
    }

    dto = BeanUtils.transform(ClusterDTO.class, entity);
    return dto;
  }

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName:.+}", method = RequestMethod.DELETE)
  public void delete(@PathVariable("appId") String appId,
                     @PathVariable("clusterName") String clusterName, @RequestParam String operator) {

    Cluster entity = clusterService.findOne(appId, clusterName);

    if (entity == null) {
      throw new NotFoundException("cluster not found for clusterName " + clusterName);
    }

    if(ConfigConsts.CLUSTER_NAME_DEFAULT.equals(entity.getName())){
      throw new BadRequestException("can not delete default cluster!");
    }

    clusterService.delete(entity.getId(), operator);
  }

  @RequestMapping(value = "/apps/{appId}/clusters", method = RequestMethod.GET)
  public List<ClusterDTO> find(@PathVariable("appId") String appId) {
    List<Cluster> clusters = clusterService.findParentClusters(appId);
    return BeanUtils.batchTransform(ClusterDTO.class, clusters);
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName:.+}", method = RequestMethod.GET)
  public ClusterDTO get(@PathVariable("appId") String appId,
                        @PathVariable("clusterName") String clusterName) {
    Cluster cluster = clusterService.findOne(appId, clusterName);
    if (cluster == null) {
      throw new NotFoundException("cluster not found for name " + clusterName);
    }
    return BeanUtils.transform(ClusterDTO.class, cluster);
  }

  @RequestMapping(value = "/apps/{appId}/cluster/{clusterName}/unique", method = RequestMethod.GET)
  public boolean isAppIdUnique(@PathVariable("appId") String appId,
                               @PathVariable("clusterName") String clusterName) {
    return clusterService.isClusterNameUnique(appId, clusterName);
  }
}
