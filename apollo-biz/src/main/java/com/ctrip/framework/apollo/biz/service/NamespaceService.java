package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.message.MessageSender;
import com.ctrip.framework.apollo.biz.message.Topics;
import com.ctrip.framework.apollo.biz.repository.NamespaceRepository;
import com.ctrip.framework.apollo.biz.utils.ReleaseMessageKeyGenerator;
import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.constants.NamespaceBranchStatus;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 名称空间 Service层
 */
@Service
public class NamespaceService {

  private static final Gson GSON = new Gson();

  private final NamespaceRepository namespaceRepository;
  private final AuditService auditService;
  private final AppNamespaceService appNamespaceService;
  private final ItemService itemService;
  private final CommitService commitService;
  private final ReleaseService releaseService;
  private final ClusterService clusterService;
  private final NamespaceBranchService namespaceBranchService;
  private final ReleaseHistoryService releaseHistoryService;
  private final NamespaceLockService namespaceLockService;
  private final InstanceService instanceService;
  private final MessageSender messageSender;

  public NamespaceService(
      final ReleaseHistoryService releaseHistoryService,
      final NamespaceRepository namespaceRepository,
      final AuditService auditService,
      final @Lazy AppNamespaceService appNamespaceService,
      final MessageSender messageSender,
      final @Lazy ItemService itemService,
      final CommitService commitService,
      final @Lazy ReleaseService releaseService,
      final @Lazy ClusterService clusterService,
      final @Lazy NamespaceBranchService namespaceBranchService,
      final NamespaceLockService namespaceLockService,
      final InstanceService instanceService) {
    this.releaseHistoryService = releaseHistoryService;
    this.namespaceRepository = namespaceRepository;
    this.auditService = auditService;
    this.appNamespaceService = appNamespaceService;
    this.messageSender = messageSender;
    this.itemService = itemService;
    this.commitService = commitService;
    this.releaseService = releaseService;
    this.clusterService = clusterService;
    this.namespaceBranchService = namespaceBranchService;
    this.namespaceLockService = namespaceLockService;
    this.instanceService = instanceService;
  }

  /**
   * 查询指定的名称空间
   *
   * @param namespaceId 名称空间id
   * @return 名称空间信息
   */
  public Namespace findOne(Long namespaceId) {
    return namespaceRepository.findById(namespaceId).orElse(null);
  }

  /**
   * 查询指定的名称空间
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 名称空间
   */
  public Namespace findOne(String appId, String clusterName, String namespaceName) {
    return namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, clusterName,
        namespaceName);
  }

  /**
   * 查询关联的名称空间中的公有名称空间
   *
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 公有的名称空间信息
   */
  public Namespace findPublicNamespaceForAssociatedNamespace(String clusterName,
      String namespaceName) {
    // 公有的应用名称空间
    AppNamespace appNamespace = appNamespaceService.findPublicNamespaceByName(namespaceName);
    if (appNamespace == null) {
      throw new BadRequestException("namespace not exist");
    }

    String appId = appNamespace.getAppId();

    Namespace namespace = findOne(appId, clusterName, namespaceName);

    // 默认集群的名称空间
    if (Objects.equals(clusterName, ConfigConsts.CLUSTER_NAME_DEFAULT)) {
      return namespace;
    }

    // 自定义集群的名称空间不存在，返回默认的名称空间
    //return default cluster's namespace
    if (namespace == null) {
      return findOne(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespaceName);
    }

    //自定义集群的名称空间存在并且已经发布了.返回自定义集群的名称空间
    //return custom cluster's namespace
    Release latestActiveRelease = releaseService.findLatestActiveRelease(namespace);
    if (latestActiveRelease != null) {
      return namespace;
    }

    // 默认的名称空间
    Namespace defaultNamespace = findOne(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespaceName);

    // 自定义集群的名称空间存在但是没有发布并且默认集群 的名称空间不存在，返回自定义的名称空间.
    if (defaultNamespace == null) {
      return namespace;
    }

    // 自定义集群的名称空间存在但是没有发布并且默认集群的名称空间存在但是已经发布了，返回默认集群的名称空间
    Release defaultNamespaceLatestActiveRelease = releaseService
        .findLatestActiveRelease(defaultNamespace);
    if (defaultNamespaceLatestActiveRelease != null) {
      return defaultNamespace;
    }

    // 自定义集群的名称空间存在但是没有发布并且默认集群的名称空间存在但是没有发布了，返回自定义集群的名称空间
    return namespace;
  }

  /**
   * 查询公有应用名称空间的所有名称空间
   *
   * @param namespaceName 公有应用名称空间名称
   * @param page          分页对象
   * @return 名称空间列表
   */
  public List<Namespace> findPublicAppNamespaceAllNamespaces(String namespaceName, Pageable page) {
    // 查询指定应用名称空间名称为公有的应用名称空间
    AppNamespace publicAppNamespace = appNamespaceService.findPublicNamespaceByName(namespaceName);

    if (publicAppNamespace == null) {
      throw new BadRequestException(
          String.format("Public appNamespace not exists. NamespaceName = %s", namespaceName));
    }

    List<Namespace> namespaces = namespaceRepository.findByNamespaceName(namespaceName, page);
    // 过滤子名称空间
    return filterChildNamespace(namespaces);
  }

  /**
   * 过滤子名称空间
   *
   * @param namespaces 名称空间列表
   * @return 非子名称空间
   */
  private List<Namespace> filterChildNamespace(List<Namespace> namespaces) {

    List<Namespace> result = new LinkedList<>();
    if (CollectionUtils.isEmpty(namespaces)) {
      return result;
    }

    for (Namespace namespace : namespaces) {
      if (!isChildNamespace(namespace)) {
        result.add(namespace);
      }
    }
    return result;
  }

  /**
   * 统计指定的公有应用名称空间关联的名称空间数量
   *
   * @param publicNamespaceName 公有的名称空间名称
   * @return 指定的公有应用名称空间关联的名称空间数量
   */
  public int countPublicAppNamespaceAssociatedNamespaces(String publicNamespaceName) {
    // 公有的应用名称空间
    AppNamespace publicAppNamespace = appNamespaceService.findPublicNamespaceByName(
        publicNamespaceName);

    if (publicAppNamespace == null) {
      throw new BadRequestException(
          String.format("Public appNamespace not exists. NamespaceName = %s", publicNamespaceName));
    }

    // 统计查询指定名称空间名称并且应用Id不为指定应用Id的数量
    return namespaceRepository
        .countByNamespaceNameAndAppIdNot(publicNamespaceName, publicAppNamespace.getAppId());
  }

  /**
   * 通过应用id和集群名称查询名称份之间
   *
   * @param appId       应用id
   * @param clusterName 集群名称空间
   * @return 名称空间列表
   */
  public List<Namespace> findNamespaces(String appId, String clusterName) {
    List<Namespace> namespaces = namespaceRepository.findByAppIdAndClusterNameOrderByIdAsc(appId,
        clusterName);
    return namespaces == null ? Collections.emptyList() : namespaces;
  }

  /**
   * 找到指定应用id，指定名称空间的名称空间列表信息
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 名称空间列表信息
   */
  public List<Namespace> findByAppIdAndNamespaceName(String appId, String namespaceName) {
    return namespaceRepository.findByAppIdAndNamespaceNameOrderByIdAsc(appId, namespaceName);
  }

  /**
   * 通过应用id、父集群名称、名称空间名称查询子名称空间
   *
   * @param appId             应用id
   * @param parentClusterName 父集群名称
   * @param namespaceName     名称空间名称
   * @return 子名称空间
   */
  public Namespace findChildNamespace(String appId, String parentClusterName,
      String namespaceName) {
    // 查询子名称空间列表
    List<Namespace> namespaces = findByAppIdAndNamespaceName(appId, namespaceName);
    if (CollectionUtils.isEmpty(namespaces) || namespaces.size() == 1) {
      return null;
    }

    // 查询子集群列表
    List<Cluster> childClusters = clusterService.findChildClusters(appId, parentClusterName);
    if (CollectionUtils.isEmpty(childClusters)) {
      return null;
    }

    Set<String> childClusterNames = childClusters.stream().map(Cluster::getName)
        .collect(Collectors.toSet());
    //子名称空间是子集群和子名称空间的交集
    for (Namespace namespace : namespaces) {
      if (childClusterNames.contains(namespace.getClusterName())) {
        return namespace;
      }
    }
    return null;
  }

  /**
   * 通过父名称空间查询子名称空间
   *
   * @param parentNamespace 父名称空间
   * @return 子名称空间
   */
  public Namespace findChildNamespace(Namespace parentNamespace) {
    String appId = parentNamespace.getAppId();
    String parentClusterName = parentNamespace.getClusterName();
    String namespaceName = parentNamespace.getNamespaceName();

    return findChildNamespace(appId, parentClusterName, namespaceName);
  }

  /**
   * 寻找父名称空间
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 名称空间信息
   */
  public Namespace findParentNamespace(String appId, String clusterName, String namespaceName) {
    return findParentNamespace(new Namespace(appId, clusterName, namespaceName));
  }

  /**
   * 获取名称空间父名称空间
   *
   * @param namespace 名称空间对象
   * @return 父名称空间
   */
  public Namespace findParentNamespace(Namespace namespace) {
    String appId = namespace.getAppId();
    String namespaceName = namespace.getNamespaceName();

    Cluster cluster = clusterService.findOne(appId, namespace.getClusterName());
    if (cluster != null && cluster.getParentClusterId() > 0) {
      Cluster parentCluster = clusterService.findOne(cluster.getParentClusterId());
      return findOne(appId, parentCluster.getName(), namespaceName);
    }
    return null;
  }

  /**
   * 是否为子名称空间
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return true, 是子名称空间，否则，不为子名称空间
   */
  public boolean isChildNamespace(String appId, String clusterName, String namespaceName) {
    return isChildNamespace(new Namespace(appId, clusterName, namespaceName));
  }

  /**
   * 是否为子名称空间
   *
   * @param namespace 名称空间实体
   * @return true, 是子名称空间，否则，不为子名称空间
   */
  public boolean isChildNamespace(Namespace namespace) {
    return findParentNamespace(namespace) != null;
  }

  /**
   * 名称空间是否唯一
   *
   * @param appId     应用id
   * @param cluster   集群名称
   * @param namespace 名称空间名称
   * @return true, 唯一，否则，false,名称空间已经存在
   */
  public boolean isNamespaceUnique(String appId, String cluster, String namespace) {
    Objects.requireNonNull(appId, "AppId must not be null");
    Objects.requireNonNull(cluster, "Cluster must not be null");
    Objects.requireNonNull(namespace, "Namespace must not be null");
    return Objects.isNull(
        namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, cluster, namespace));
  }

  /**
   * 通过应用id和集群名称删除名称空间
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @param operator    操作者
   */
  @Transactional(rollbackFor = Exception.class)
  public void deleteByAppIdAndClusterName(String appId, String clusterName, String operator) {
    List<Namespace> toDeleteNamespaces = findNamespaces(appId, clusterName);
    // 批量删除
    for (Namespace namespace : toDeleteNamespaces) {
      deleteNamespace(namespace, operator);
    }
  }

  /**
   * 删除名称空间
   *
   * @param namespace 名称空间实体
   * @param operator  操作者
   * @return 删除的名称空间信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Namespace deleteNamespace(Namespace namespace, String operator) {
    String appId = namespace.getAppId();
    String clusterName = namespace.getClusterName();
    String namespaceName = namespace.getNamespaceName();

    // 删除名称空间属性的配置项
    itemService.batchDelete(namespace.getId(), operator);
    // 删除提交记录
    commitService.batchDelete(appId, clusterName, namespace.getNamespaceName(), operator);

    // 只要父命名空间存在，子命名空间发布就应该保留，因为父命名空间的发布历史记录需要它们
    // 删除子名称空间的发布信息
    if (!isChildNamespace(namespace)) {
      releaseService.batchDelete(appId, clusterName, namespace.getNamespaceName(), operator);
    }

    // 删除子名称空间
    Namespace childNamespace = findChildNamespace(namespace);
    if (childNamespace != null) {
      namespaceBranchService.deleteBranch(appId, clusterName, namespaceName,
          childNamespace.getClusterName(), NamespaceBranchStatus.DELETED, operator);
      //delete child namespace's releases. Notice: delete child namespace will not delete child namespace's releases
      releaseService.batchDelete(appId, childNamespace.getClusterName(), namespaceName, operator);
    }

    // 删除发布记录
    releaseHistoryService.batchDelete(appId, clusterName, namespaceName, operator);
    // 指删除实例配置
    instanceService.batchDeleteInstanceConfig(appId, clusterName, namespaceName);
    // 解锁
    namespaceLockService.unlock(namespace.getId());

    namespace.setDeleted(true);
    namespace.setDataChangeLastModifiedBy(operator);
    // 记录日志审计信息
    auditService.audit(Namespace.class.getSimpleName(), namespace.getId(), Audit.OP.DELETE,
        operator);
    // 逻辑删除
    Namespace deleted = namespaceRepository.save(namespace);

    //发布发布消息以在配置服务中进行一些清理，例如更新缓存
    messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName,
        namespaceName), Topics.APOLLO_RELEASE_TOPIC);
    return deleted;
  }

  /**
   * 保存名称空间
   *
   * @param entity 名称空间实体
   * @return 保存后的名称空间信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Namespace save(Namespace entity) {
    // 判断名称空间是否唯一
    if (!isNamespaceUnique(entity.getAppId(), entity.getClusterName(), entity.getNamespaceName())) {
      throw new ServiceException("namespace not unique");
    }
    //protection
    entity.setId(0);
    // 保存
    Namespace namespace = namespaceRepository.save(entity);
    // 记录日志审计信息
    auditService.audit(Namespace.class.getSimpleName(), namespace.getId(), Audit.OP.INSERT,
        namespace.getDataChangeCreatedBy());

    return namespace;
  }

  /**
   * 更新名称空间
   *
   * @param namespace 名称空间实体信息
   * @return 名称空间信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Namespace update(Namespace namespace) {
    // 管理的名称空间信息
    Namespace managedNamespace = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(
        namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName());

    // 拷贝新的名称空间，并保存
    BeanUtils.copyEntityProperties(namespace, managedNamespace);
    managedNamespace = namespaceRepository.save(managedNamespace);

    // 记录日志审计信息
    auditService.audit(Namespace.class.getSimpleName(), managedNamespace.getId(), Audit.OP.UPDATE,
        managedNamespace.getDataChangeLastModifiedBy());
    return managedNamespace;
  }

  /**
   * 实例化应用名称空间
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @param createBy    创建者
   */
  @Transactional(rollbackFor = Exception.class)
  public void instanceOfAppNamespaces(String appId, String clusterName, String createBy) {

    //应用下的名称空间列表
    List<AppNamespace> appNamespaces = appNamespaceService.findByAppId(appId);

    // 保存名称空间
    for (AppNamespace appNamespace : appNamespaces) {
      Namespace ns = new Namespace();
      ns.setAppId(appId);
      ns.setClusterName(clusterName);
      ns.setNamespaceName(appNamespace.getName());
      ns.setDataChangeCreatedBy(createBy);
      ns.setDataChangeLastModifiedBy(createBy);
      namespaceRepository.save(ns);
      // 记录日志审计信息
      auditService.audit(Namespace.class.getSimpleName(), ns.getId(), Audit.OP.INSERT, createBy);
    }

  }

  /**
   * 名称空间发布信息
   *
   * @param appId 应用id
   * @return 名称空间发布信息
   */
  public Map<String, Boolean> namespacePublishInfo(String appId) {
    //找到集群列表
    List<Cluster> clusters = clusterService.findParentClusters(appId);
    if (CollectionUtils.isEmpty(clusters)) {
      throw new BadRequestException("app not exist");
    }

    Map<String, Boolean> clusterHasNotPublishedItems = Maps.newHashMap();

    for (Cluster cluster : clusters) {
      String clusterName = cluster.getName();
      //获取名称空间列表
      List<Namespace> namespaces = findNamespaces(appId, clusterName);

      // 遍历名称空间，设置名称空间是否未发布
      for (Namespace namespace : namespaces) {
        boolean isNamespaceNotPublished = isNamespaceNotPublished(namespace);
        if (isNamespaceNotPublished) {
          clusterHasNotPublishedItems.put(clusterName, true);
          break;
        }
      }
      clusterHasNotPublishedItems.putIfAbsent(clusterName, false);
    }
    return clusterHasNotPublishedItems;
  }

  /**
   * 名称空间是否未发布
   *
   * @param namespace 名称空间信息
   * @return true, 名称空间已发布，否则，未发布，false
   */
  private boolean isNamespaceNotPublished(Namespace namespace) {

    // 名称空间上一次发布信息
    Release latestRelease = releaseService.findLatestActiveRelease(namespace);
    long namespaceId = namespace.getId();

    if (latestRelease == null) {
      // 通过名称空间Id最新的配置项信息
      Item lastItem = itemService.findLastOne(namespaceId);
      return lastItem != null;
    }

    Date lastPublishTime = latestRelease.getDataChangeLastModifiedTime();
    // 最后修改时间的属性配置项信息
    List<Item> itemsModifiedAfterLastPublish = itemService.findItemsModifiedAfterDate(namespaceId,
        lastPublishTime);

    if (CollectionUtils.isEmpty(itemsModifiedAfterLastPublish)) {
      return false;
    }

    Map<String, String> publishedConfiguration = GSON.fromJson(latestRelease.getConfigurations(),
        GsonType.CONFIG);

    // 如果在最后修改时间的属性配置项信息和上一次发布信息匹配，说明配置项已经发布了
    for (Item item : itemsModifiedAfterLastPublish) {
      if (!Objects.equals(item.getValue(), publishedConfiguration.get(item.getKey()))) {
        return true;
      }
    }
    return false;
  }
}