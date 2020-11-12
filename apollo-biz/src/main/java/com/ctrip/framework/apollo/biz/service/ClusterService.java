package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.repository.ClusterRepository;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.ConfigConsts;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 集群 Service层
 */
@Service
public class ClusterService {

  private final ClusterRepository clusterRepository;
  private final AuditService auditService;
  private final NamespaceService namespaceService;

  public ClusterService(
      final ClusterRepository clusterRepository,
      final AuditService auditService,
      final @Lazy NamespaceService namespaceService) {
    this.clusterRepository = clusterRepository;
    this.auditService = auditService;
    this.namespaceService = namespaceService;
  }


  /**
   * 集群名称是否唯一
   *
   * @param appId       应用id
   * @param clusterName 名称空间名称
   * @return true, 唯一，否则，false,名称已经存在
   */
  public boolean isClusterNameUnique(String appId, String clusterName) {
    Objects.requireNonNull(appId, "AppId must not be null");
    Objects.requireNonNull(clusterName, "ClusterName must not be null");
    return Objects.isNull(clusterRepository.findByAppIdAndName(appId, clusterName));
  }

  /**
   * 通过应用id和名称查询集群信息
   *
   * @param appId 应用id
   * @param name  集群名称
   * @return 集群信息
   */
  public Cluster findOne(String appId, String name) {
    return clusterRepository.findByAppIdAndName(appId, name);
  }

  /**
   * 查询指定集群id的集群信息
   *
   * @param clusterId 集群id
   * @return 集群信息
   */
  public Cluster findOne(long clusterId) {
    return clusterRepository.findById(clusterId).orElse(null);
  }

  /**
   * 查询应用下的集群列表
   *
   * @param appId 应用id
   * @return 集群列表
   */
  public List<Cluster> findParentClusters(String appId) {
    if (StringUtils.isBlank(appId)) {
      return Collections.emptyList();
    }

    // 找到指定应用id指定父集群id的集群信息
    List<Cluster> clusters = clusterRepository.findByAppIdAndParentClusterId(appId, 0L);
    if (clusters == null) {
      return Collections.emptyList();
    }

    // 确保父群集在分支群集之前
    Collections.sort(clusters);
    return clusters;
  }

  /**
   * 保存应用名称空间的集群实例
   *
   * @param entity 集群实体
   * @return 保存后的集群信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Cluster saveWithInstanceOfAppNamespaces(Cluster entity) {
    // 保存应用名称空间没有实例的集群信息
    Cluster savedCluster = saveWithoutInstanceOfAppNamespaces(entity);
    // 实例化应用名称空间
    namespaceService.instanceOfAppNamespaces(savedCluster.getAppId(), savedCluster.getName(),
        savedCluster.getDataChangeCreatedBy());
    return savedCluster;
  }

  /**
   * 保存应用名称空间没有实例的集群信息
   *
   * @param entity 集群实例
   * @return
   */
  @Transactional(rollbackFor = Exception.class)
  public Cluster saveWithoutInstanceOfAppNamespaces(Cluster entity) {
    // 判断集群名称唯一
    if (!isClusterNameUnique(entity.getAppId(), entity.getName())) {
      throw new BadRequestException("cluster not unique");
    }
    //protection
    entity.setId(0);
    // 保存集群信息
    Cluster cluster = clusterRepository.save(entity);
    // 记录日志审计信息
    auditService.audit(Cluster.class.getSimpleName(), cluster.getId(), Audit.OP.INSERT,
        cluster.getDataChangeCreatedBy());

    return cluster;
  }

  /**
   * 删除集群信息
   *
   * @param id       集群id
   * @param operator 操作者
   */
  @Transactional(rollbackFor = Exception.class)
  public void delete(long id, String operator) {
    Cluster cluster = clusterRepository.findById(id).orElse(null);
    if (cluster == null) {
      throw new BadRequestException("cluster not exist");
    }

    // 删除关联的名称空间
    namespaceService.deleteByAppIdAndClusterName(cluster.getAppId(), cluster.getName(), operator);

    cluster.setDeleted(true);
    cluster.setDataChangeLastModifiedBy(operator);
    // 逻辑删除集群信息
    clusterRepository.save(cluster);
    // 记录日志审计信息
    auditService.audit(Cluster.class.getSimpleName(), id, Audit.OP.DELETE, operator);
  }

  /**
   * 更新集群信息
   *
   * @param cluster 集群信息
   * @return 更新后的集群信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Cluster update(Cluster cluster) {

    // 用新的属性替换旧的属性并保存
    Cluster managedCluster = clusterRepository.findByAppIdAndName(cluster.getAppId(), cluster
        .getName());
    BeanUtils.copyEntityProperties(cluster, managedCluster);
    managedCluster = clusterRepository.save(managedCluster);

    // 记录日志审计信息
    auditService.audit(Cluster.class.getSimpleName(), managedCluster.getId(), Audit.OP.UPDATE,
        managedCluster.getDataChangeLastModifiedBy());

    return managedCluster;
  }

  /**
   * 创建默认的集群
   *
   * @param appId    应用id
   * @param createBy 创建者
   */
  @Transactional(rollbackFor = Exception.class)
  public void createDefaultCluster(String appId, String createBy) {
    // 检查名称是否唯一
    if (!isClusterNameUnique(appId, ConfigConsts.CLUSTER_NAME_DEFAULT)) {
      throw new ServiceException("cluster not unique");
    }
    Cluster cluster = new Cluster();
    cluster.setName(ConfigConsts.CLUSTER_NAME_DEFAULT);
    cluster.setAppId(appId);
    cluster.setDataChangeCreatedBy(createBy);
    cluster.setDataChangeLastModifiedBy(createBy);

    // 保存默认的集群信息
    clusterRepository.save(cluster);
    // 记录日志审计信息
    auditService.audit(Cluster.class.getSimpleName(), cluster.getId(), Audit.OP.INSERT, createBy);
  }

  /**
   * 查找父集群下的子集群列表信息
   *
   * @param appId             应用id
   * @param parentClusterName 父集群名称
   * @return 父集群下的子集群列表信息
   */
  public List<Cluster> findChildClusters(String appId, String parentClusterName) {
    // 父集群信息
    Cluster parentCluster = findOne(appId, parentClusterName);
    if (parentCluster == null) {
      throw new BadRequestException("parent cluster not exist");
    }
    // 子集群信息
    return clusterRepository.findByParentClusterId(parentCluster.getId());
  }

  /**
   * 查找指定应用的集群列表信息
   *
   * @param appId 应用id
   * @return 集群列表信息
   */
  public List<Cluster> findClusters(String appId) {

    // 应用下的集群信息列表
    List<Cluster> clusters = clusterRepository.findByAppId(appId);
    if (clusters == null) {
      return Collections.emptyList();
    }

    // 确保父群集在分支群集之前
    Collections.sort(clusters);

    return clusters;
  }
}
