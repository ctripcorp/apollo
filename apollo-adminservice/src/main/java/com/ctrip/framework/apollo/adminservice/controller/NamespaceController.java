package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.InputValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class NamespaceController {

  @Autowired
  private NamespaceService namespaceService;

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}/namespaces", method = RequestMethod.POST)
  public NamespaceDTO create(@PathVariable("appId") String appId,
                             @PathVariable("clusterName") String clusterName, @RequestBody NamespaceDTO dto) {
    if (!InputValidator.isValidClusterNamespace(dto.getNamespaceName())) {
      throw new BadRequestException(String.format("Namespace格式错误: %s", InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE));
    }
    Namespace entity = BeanUtils.transfrom(Namespace.class, dto);
    Namespace managedEntity = namespaceService.findOne(appId, clusterName, entity.getNamespaceName());
    if (managedEntity != null) {
      throw new BadRequestException("namespace already exist.");
    }

    entity = namespaceService.save(entity);

    dto = BeanUtils.transfrom(NamespaceDTO.class, entity);
    return dto;
  }

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName:.+}", method = RequestMethod.DELETE)
  public void delete(@PathVariable("appId") String appId,
                     @PathVariable("clusterName") String clusterName,
                     @PathVariable("namespaceName") String namespaceName, @RequestParam String operator) {
    Namespace entity = namespaceService.findOne(appId, clusterName, namespaceName);
    if (entity == null) throw new NotFoundException(
            String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));

    namespaceService.deleteNamespace(entity, operator);
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces", method = RequestMethod.GET)
  public List<NamespaceDTO> find(@PathVariable("appId") String appId,
                                 @PathVariable("clusterName") String clusterName) {
    List<Namespace> groups = namespaceService.findNamespaces(appId, clusterName);
    return BeanUtils.batchTransform(NamespaceDTO.class, groups);
  }

  @RequestMapping(value = "/namespaces/{namespaceId}", method = RequestMethod.GET)
  public NamespaceDTO get(@PathVariable("namespaceId") Long namespaceId) {
    Namespace namespace = namespaceService.findOne(namespaceId);
    if (namespace == null)
      throw new NotFoundException(String.format("namespace not found for %s", namespaceId));
    return BeanUtils.transfrom(NamespaceDTO.class, namespace);
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName:.+}", method = RequestMethod.GET)
  public NamespaceDTO get(@PathVariable("appId") String appId,
                          @PathVariable("clusterName") String clusterName,
                          @PathVariable("namespaceName") String namespaceName) {
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace == null) throw new NotFoundException(
            String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
    return BeanUtils.transfrom(NamespaceDTO.class, namespace);
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/associated-public-namespace",
      method = RequestMethod.GET)
  public NamespaceDTO findPublicNamespaceForAssociatedNamespace(@PathVariable String appId,
                                                                @PathVariable String clusterName,
                                                                @PathVariable String namespaceName) {
    Namespace namespace = namespaceService.findPublicNamespaceForAssociatedNamespace(clusterName, namespaceName);

    if (namespace == null) {
      throw new NotFoundException(String.format("public namespace not found. namespace:%s", namespaceName));
    }

    return BeanUtils.transfrom(NamespaceDTO.class, namespace);
  }

  /**
   * cluster -> cluster has not published namespaces?
   */
  @RequestMapping(value = "/apps/{appId}/namespaces/publish_info", method = RequestMethod.GET)
  public Map<String, Boolean> namespacePublishInfo(@PathVariable String appId) {
    return namespaceService.namespacePublishInfo(appId);
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/isExisted", method = RequestMethod.GET)
  public boolean findExisted(@PathVariable("appId") String appId,
		                     @PathVariable("clusterName") String clusterName,
                             @PathVariable("namespaceName") String namespaceName) {  
    return namespaceService.findNamespaceExited(appId, clusterName, namespaceName);
  }
}
