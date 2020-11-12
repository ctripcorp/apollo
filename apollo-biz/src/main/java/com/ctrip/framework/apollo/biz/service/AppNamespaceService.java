package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.repository.AppNamespaceRepository;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 应用名称空间 Service层
 */
@Slf4j
@Service
public class AppNamespaceService {

  private final AppNamespaceRepository appNamespaceRepository;
  private final NamespaceService namespaceService;
  private final ClusterService clusterService;
  private final AuditService auditService;

  public AppNamespaceService(
      final AppNamespaceRepository appNamespaceRepository,
      final @Lazy NamespaceService namespaceService,
      final @Lazy ClusterService clusterService,
      final AuditService auditService) {
    this.appNamespaceRepository = appNamespaceRepository;
    this.namespaceService = namespaceService;
    this.clusterService = clusterService;
    this.auditService = auditService;
  }

  /**
   * 应用名称空间是否唯一.
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return true, 唯一，否则，名称已经存在
   */
  public boolean isAppNamespaceNameUnique(String appId, String namespaceName) {
    Objects.requireNonNull(appId, "AppId must not be null");
    Objects.requireNonNull(namespaceName, "Namespace must not be null");
    return Objects.isNull(appNamespaceRepository.findByAppIdAndName(appId, namespaceName));
  }

  /**
   * 查询指定应用名称空间名称为公有的应用名称空间.
   *
   * @param namespaceName 名称空间名称
   * @return 应用名称空间
   */
  public AppNamespace findPublicNamespaceByName(String namespaceName) {
    Preconditions.checkArgument(namespaceName != null, "Namespace must not be null");
    return appNamespaceRepository.findByNameAndIsPublicTrue(namespaceName);
  }

  /**
   * 根据应用id查询应用名称空间列表.
   *
   * @param appId 应用id
   * @return 应用名称空间列表
   */
  public List<AppNamespace> findByAppId(String appId) {
    return appNamespaceRepository.findByAppId(appId);
  }

  /**
   * 查询指定应用名称空间名称列表为公共的应用名称空间列表.
   *
   * @param namespaceNames 应用名称空间名称列表
   * @return 公共的应用名称空间列表
   */
  public List<AppNamespace> findPublicNamespacesByNames(Set<String> namespaceNames) {
    if (CollectionUtils.isEmpty(namespaceNames)) {
      return Collections.emptyList();
    }
    return appNamespaceRepository.findByNameInAndIsPublicTrue(namespaceNames);
  }

  /**
   * 查询指定应用id私有的应用名称空间.
   *
   * @param appId 应用id
   * @return 私有的应用名称空间列表
   */
  public List<AppNamespace> findPrivateAppNamespace(String appId) {
    return appNamespaceRepository.findByAppIdAndIsPublic(appId, false);
  }

  /**
   * 指定应用下的指定名称空间.
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 应用名称空间
   */
  public AppNamespace findOne(String appId, String namespaceName) {
    Preconditions.checkArgument(!StringUtils.isContainEmpty(appId, namespaceName),
        "appId or Namespace must not be null");
    return appNamespaceRepository.findByAppIdAndName(appId, namespaceName);
  }

  /**
   * 查询指定应用下的指定名称空间列表.
   *
   * @param appId          应用id
   * @param namespaceNames 名称空间列表
   * @return 应用名称空间列表
   */
  public List<AppNamespace> findByAppIdAndNamespaces(String appId, Set<String> namespaceNames) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(appId), "appId must not be null");
    if (CollectionUtils.isEmpty(namespaceNames)) {
      return Collections.emptyList();
    }
    return appNamespaceRepository.findByAppIdAndNameIn(appId, namespaceNames);
  }

  /**
   * 创建默认的应用名称空间.
   *
   * @param appId    应用id
   * @param createBy 创建者
   */
  @Transactional(rollbackFor = Exception.class)
  public void createDefaultAppNamespace(String appId, String createBy) {
    if (!isAppNamespaceNameUnique(appId, ConfigConsts.NAMESPACE_APPLICATION)) {
      throw new ServiceException("appnamespace not unique");
    }
    AppNamespace appNs = new AppNamespace();
    appNs.setAppId(appId);
    appNs.setName(ConfigConsts.NAMESPACE_APPLICATION);
    appNs.setComment("default app namespace");
    appNs.setFormat(ConfigFileFormat.Properties.getValue());
    appNs.setDataChangeCreatedBy(createBy);
    appNs.setDataChangeLastModifiedBy(createBy);

    // 创建默认的应用名称空间
    appNamespaceRepository.save(appNs);
    // 记录日志审计信息
    auditService.audit(AppNamespace.class.getSimpleName(), appNs.getId(), Audit.OP.INSERT,
        createBy);
  }

  /**
   * 创建应用名称空间
   *
   * @param appNamespace 应用名称空间实体
   * @return 应用名称空间信息
   */
  @Transactional(rollbackFor = Exception.class)
  public AppNamespace createAppNamespace(AppNamespace appNamespace) {
    String createBy = appNamespace.getDataChangeCreatedBy();
    // 校验唯一
    if (!isAppNamespaceNameUnique(appNamespace.getAppId(), appNamespace.getName())) {
      throw new ServiceException("appnamespace not unique");
    }
    //protection
    appNamespace.setId(0);
    appNamespace.setDataChangeCreatedBy(createBy);
    appNamespace.setDataChangeLastModifiedBy(createBy);

    // 保存
    appNamespace = appNamespaceRepository.save(appNamespace);
    // 为应用名称空间在所有集群创建名称空间
    createNamespaceForAppNamespaceInAllCluster(appNamespace.getAppId(), appNamespace.getName(),
        createBy);
    // 记录日志审计信息
    auditService.audit(AppNamespace.class.getSimpleName(), appNamespace.getId(), Audit.OP.INSERT,
        createBy);
    return appNamespace;
  }

  /**
   * 更新应用名称空间
   *
   * @param appNamespace 应用名称空间
   * @return 应用名称空间
   */
  public AppNamespace update(AppNamespace appNamespace) {
    // 新的名称空间覆盖旧的名称空间
    AppNamespace managedNs = appNamespaceRepository.findByAppIdAndName(appNamespace.getAppId(),
        appNamespace.getName());
    BeanUtils.copyEntityProperties(appNamespace, managedNs);
    // 保存
    managedNs = appNamespaceRepository.save(managedNs);
    // 记录日志审计信息
    auditService.audit(AppNamespace.class.getSimpleName(), managedNs.getId(), Audit.OP.UPDATE,
        managedNs.getDataChangeLastModifiedBy());
    return managedNs;
  }

  /**
   * 为应用名称空间在所有集群创建名称空间
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param createBy      创建者
   */
  public void createNamespaceForAppNamespaceInAllCluster(String appId, String namespaceName,
      String createBy) {
    // 找到父集群信息列表
    List<Cluster> clusters = clusterService.findParentClusters(appId);

    // 在所有集群中添加名称空间
    for (Cluster cluster : clusters) {
      // 校验名称空间唯一
      if (!namespaceService.isNamespaceUnique(appId, cluster.getName(), namespaceName)) {
        continue;
      }
      Namespace namespace = new Namespace();
      namespace.setClusterName(cluster.getName());
      namespace.setAppId(appId);
      namespace.setNamespaceName(namespaceName);
      namespace.setDataChangeCreatedBy(createBy);
      namespace.setDataChangeLastModifiedBy(createBy);
      // 保存
      namespaceService.save(namespace);
    }
  }

  /**
   * 批量删除
   *
   * @param appId    应用id
   * @param operator 操作者
   */
  @Transactional(rollbackFor = Exception.class)
  public void batchDelete(String appId, String operator) {
    appNamespaceRepository.batchDeleteByAppId(appId, operator);
  }

  /**
   * 删除应用名称空间
   *
   * @param appNamespace 应用名称空间实体
   * @param operator     操作者
   */
  @Transactional(rollbackFor = Exception.class)
  public void deleteAppNamespace(AppNamespace appNamespace, String operator) {
    String appId = appNamespace.getAppId();
    String namespaceName = appNamespace.getName();

    log.info("{} is deleting AppNamespace, appId: {}, namespace: {}", operator, appId,
        namespaceName);

    // 1. 删除名称空间列表
    // 找到指定应用id，指定名称空间的名称空间列表信息
    List<Namespace> namespaces = namespaceService.findByAppIdAndNamespaceName(appId, namespaceName);

    // 批量删除名称空间
    if (namespaces != null) {
      for (Namespace namespace : namespaces) {
        namespaceService.deleteNamespace(namespace, operator);
      }
    }

    // 2.删除应用的名称空间
    appNamespaceRepository.delete(appId, namespaceName, operator);
  }
}
