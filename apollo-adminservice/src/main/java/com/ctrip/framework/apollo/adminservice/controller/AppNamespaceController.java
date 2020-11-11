package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.service.AppNamespaceService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.common.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应用名称空间 Controller
 */
@RestController
public class AppNamespaceController {

  private final AppNamespaceService appNamespaceService;
  private final NamespaceService namespaceService;

  public AppNamespaceController(
      final AppNamespaceService appNamespaceService,
      final NamespaceService namespaceService) {
    this.appNamespaceService = appNamespaceService;
    this.namespaceService = namespaceService;
  }

  /**
   * 创建应用名称空间
   *
   * @param appNamespace   应用名称空间
   * @param silentCreation 是否无声的为所有集群创建应用名称空间
   * @return 应用名称空间信息
   */
  @PostMapping("/apps/{appId}/appnamespaces")
  public AppNamespaceDTO create(@RequestBody AppNamespaceDTO appNamespace,
      @RequestParam(defaultValue = "false") boolean silentCreation) {

    AppNamespace entity = BeanUtils.transform(AppNamespace.class, appNamespace);
    AppNamespace managedEntity = appNamespaceService.findOne(entity.getAppId(), entity.getName());

    if (managedEntity == null) {
      if (StringUtils.isBlank(entity.getFormat())) {
        entity.setFormat(ConfigFileFormat.Properties.getValue());
      }
      // 创建名称空间
      entity = appNamespaceService.createAppNamespace(entity);
    } else if (silentCreation) {
      // 为应用名称空间在所有集群创建名称空间
      appNamespaceService.createNamespaceForAppNamespaceInAllCluster(appNamespace.getAppId(),
          appNamespace.getName(), appNamespace.getDataChangeCreatedBy());
      entity = managedEntity;
    } else {
      throw new BadRequestException("app namespaces already exist.");
    }

    return BeanUtils.transform(AppNamespaceDTO.class, entity);
  }


  /**
   * 删除应用名称空间
   *
   * @param appId         应用id
   * @param namespaceName 应用名称空间名称
   * @param operator      操作者
   */
  @DeleteMapping("/apps/{appId}/appnamespaces/{namespaceName:.+}")
  public void delete(@PathVariable("appId") String appId,
      @PathVariable("namespaceName") String namespaceName,
      @RequestParam String operator) {
    AppNamespace entity = appNamespaceService.findOne(appId, namespaceName);
    if (entity == null) {
      throw new BadRequestException(
          "app namespace not found for appId: " + appId + " namespace: " + namespaceName);
    }
    appNamespaceService.deleteAppNamespace(entity, operator);
  }

  /**
   * 查询公有应用名称空间的所有名称空间
   *
   * @param publicNamespaceName 公有应用名称空间名称
   * @param pageable            分页对象
   * @return 名称空间列表
   */
  @GetMapping("/appnamespaces/{publicNamespaceName}/namespaces")
  public List<NamespaceDTO> findPublicAppNamespaceAllNamespaces(
      @PathVariable String publicNamespaceName, Pageable pageable) {

    List<Namespace> namespaces = namespaceService.findPublicAppNamespaceAllNamespaces(
        publicNamespaceName, pageable);
    return BeanUtils.batchTransform(NamespaceDTO.class, namespaces);
  }

  /**
   * 统计指定的公有应用名称空间关联的名称空间数量
   *
   * @param publicNamespaceName 公有应用名称空间名称
   * @return 指定的公有应用名称空间关联的名称空间数量
   */
  @GetMapping("/appnamespaces/{publicNamespaceName}/associated-namespaces/count")
  public int countPublicAppNamespaceAssociatedNamespaces(@PathVariable String publicNamespaceName) {
    return namespaceService.countPublicAppNamespaceAssociatedNamespaces(publicNamespaceName);
  }

  /**
   * 根据应用id查询应用名称空间列表
   *
   * @param appId 应用id
   * @return 应用名称空间列表
   */
  @GetMapping("/apps/{appId}/appnamespaces")
  public List<AppNamespaceDTO> getAppNamespaces(@PathVariable("appId") String appId) {

    List<AppNamespace> appNamespaces = appNamespaceService.findByAppId(appId);
    return BeanUtils.batchTransform(AppNamespaceDTO.class, appNamespaces);
  }
}
