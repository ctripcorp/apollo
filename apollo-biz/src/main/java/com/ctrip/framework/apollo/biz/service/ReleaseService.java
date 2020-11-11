package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.GrayReleaseRule;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.NamespaceLock;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.entity.ReleaseHistory;
import com.ctrip.framework.apollo.biz.repository.ReleaseRepository;
import com.ctrip.framework.apollo.biz.utils.ReleaseKeyGenerator;
import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.constants.ReleaseOperation;
import com.ctrip.framework.apollo.common.constants.ReleaseOperationContext;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.GrayReleaseRuleItemTransformer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ReleaseService {

  private static final FastDateFormat TIMESTAMP_FORMAT = FastDateFormat
      .getInstance("yyyyMMddHHmmss");
  private static final Gson gson = new Gson();
  private static final Set<Integer> BRANCH_RELEASE_OPERATIONS = Sets.newHashSet(ReleaseOperation
      .GRAY_RELEASE, ReleaseOperation.MASTER_NORMAL_RELEASE_MERGE_TO_GRAY, ReleaseOperation
      .MATER_ROLLBACK_MERGE_TO_GRAY);
  private static final Pageable FIRST_ITEM = PageRequest.of(0, 1);
  private static final Type OPERATION_CONTEXT_TYPE_REFERENCE = new TypeToken<Map<String, Object>>() {
  }.getType();

  private final ReleaseRepository releaseRepository;
  private final ItemService itemService;
  private final AuditService auditService;
  private final NamespaceLockService namespaceLockService;
  private final NamespaceService namespaceService;
  private final NamespaceBranchService namespaceBranchService;
  private final ReleaseHistoryService releaseHistoryService;
  private final ItemSetService itemSetService;

  public ReleaseService(
      final ReleaseRepository releaseRepository,
      final ItemService itemService,
      final AuditService auditService,
      final NamespaceLockService namespaceLockService,
      final NamespaceService namespaceService,
      final NamespaceBranchService namespaceBranchService,
      final ReleaseHistoryService releaseHistoryService,
      final ItemSetService itemSetService) {
    this.releaseRepository = releaseRepository;
    this.itemService = itemService;
    this.auditService = auditService;
    this.namespaceLockService = namespaceLockService;
    this.namespaceService = namespaceService;
    this.namespaceBranchService = namespaceBranchService;
    this.releaseHistoryService = releaseHistoryService;
    this.itemSetService = itemSetService;
  }

  /**
   * 根据发布id查询发布信息
   *
   * @param releaseId 发布id
   * @return 指定id的发布信息
   */
  public Release findOne(long releaseId) {
    return releaseRepository.findById(releaseId).orElse(null);
  }

  /**
   * 通过发布id查询未废弃的发布信息
   *
   * @param releaseId 发布id
   * @return 指定id未废弃的发布信息
   */
  public Release findActiveOne(long releaseId) {
    return releaseRepository.findByIdAndIsAbandonedFalse(releaseId);
  }

  /**
   * 通过id列表找到发布信息列表
   *
   * @param releaseIds 发布id列表
   * @return 发布信息列表
   */
  public List<Release> findByReleaseIds(Set<Long> releaseIds) {
    Iterable<Release> releases = releaseRepository.findAllById(releaseIds);
    return Lists.newArrayList(releases);
  }

  public List<Release> findByReleaseKeys(Set<String> releaseKeys) {
    return releaseRepository.findByReleaseKeyIn(releaseKeys);
  }

  /**
   * 获取名称空间上一次的发布信息
   *
   * @param namespace 名称空间
   * @return 上一次的发布信息
   */
  public Release findLatestActiveRelease(Namespace namespace) {
    return findLatestActiveRelease(namespace.getAppId(),
        namespace.getClusterName(), namespace.getNamespaceName());

  }

  /**
   * 查询名称空间上一次的发布信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 上一次的发布信息
   */
  public Release findLatestActiveRelease(String appId, String clusterName, String namespaceName) {
    return releaseRepository
        .findFirstByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(appId,
            clusterName, namespaceName);
  }

  /**
   * 获取所有发布信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          分页对象
   * @return 发布信息列表
   */
  public List<Release> findAllReleases(String appId, String clusterName, String namespaceName,
      Pageable page) {
    List<Release> releases = releaseRepository
        .findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(appId, clusterName, namespaceName,
            page);
    if (releases == null) {
      return Collections.emptyList();
    }
    return releases;
  }

  /**
   * 通过应用id、集群名称、名称空间名称按id降序查询未废弃的发布信息列表
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          分页对象
   * @return 未废弃的发布信息列表
   */
  public List<Release> findActiveReleases(String appId, String clusterName, String namespaceName,
      Pageable page) {
    List<Release> releases = releaseRepository
        .findByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(appId,
            clusterName, namespaceName, page);
    if (releases == null) {
      return Collections.emptyList();
    }
    return releases;
  }

  /**
   * 通过通过应用id、集群名称、名称空间名称查询id在指定范围内未废弃的发布信息列表
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param fromReleaseId 开始发布信息id
   * @param toReleaseId   结束发布信息id
   * @return 未废弃的发布信息列表
   */
  private List<Release> findActiveReleasesBetween(String appId, String clusterName,
      String namespaceName, long fromReleaseId, long toReleaseId) {
    List<Release> releases = releaseRepository
        .findByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseAndIdBetweenOrderByIdDesc(
            appId, clusterName, namespaceName, fromReleaseId, toReleaseId);
    if (releases == null) {
      return Collections.emptyList();
    }
    return releases;
  }

  /**
   * 合并分支改变的配置并且发布
   *
   * @param namespace          名称空间
   * @param branchName         分支名称
   * @param releaseName        发布名称
   * @param releaseComment     发布备注
   * @param isEmergencyPublish 是否紧急发布
   * @param changeSets         改变的配置
   * @return 分支发布的发布信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Release mergeBranchChangeSetsAndRelease(Namespace namespace, String branchName,
      String releaseName, String releaseComment, boolean isEmergencyPublish,
      ItemChangeSets changeSets) {

    // 检查锁
    checkLock(namespace, isEmergencyPublish, changeSets.getDataChangeLastModifiedBy());
    // 更新
    itemSetService.updateSet(namespace, changeSets);

    // 上一次的分支发布信息
    Release branchRelease = findLatestActiveRelease(namespace.getAppId(), branchName, namespace
        .getNamespaceName());
    long branchReleaseId = branchRelease == null ? 0 : branchRelease.getId();

    // 名称空间下的属性的配置项
    Map<String, String> operateNamespaceItems = getNamespaceItems(namespace);

    Map<String, Object> operationContext = Maps.newLinkedHashMap();
    operationContext.put(ReleaseOperationContext.SOURCE_BRANCH, branchName);
    operationContext.put(ReleaseOperationContext.BASE_RELEASE_ID, branchReleaseId);
    operationContext.put(ReleaseOperationContext.IS_EMERGENCY_PUBLISH, isEmergencyPublish);

    //master发布
    return masterRelease(namespace, releaseName, releaseComment, operateNamespaceItems,
        changeSets.getDataChangeLastModifiedBy(),
        ReleaseOperation.GRAY_RELEASE_MERGE_TO_MASTER, operationContext);

  }

  /**
   * 发布
   *
   * @param namespace          名称空间
   * @param releaseName        发布名称
   * @param releaseComment     发布备注
   * @param operator           操作者
   * @param isEmergencyPublish 是否紧急发布
   * @return 发布信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Release publish(Namespace namespace, String releaseName, String releaseComment,
      String operator, boolean isEmergencyPublish) {
    //检查锁
    checkLock(namespace, isEmergencyPublish, operator);
    // 操作名称空间属性配置项
    Map<String, String> operateNamespaceItems = getNamespaceItems(namespace);
    // 父名称空间
    Namespace parentNamespace = namespaceService.findParentNamespace(namespace);

    // 分支发布
    if (parentNamespace != null) {
      return publishBranchNamespace(parentNamespace, namespace, operateNamespaceItems,
          releaseName, releaseComment, operator, isEmergencyPublish);
    }
    // 获取子名称空间
    Namespace childNamespace = namespaceService.findChildNamespace(namespace);

    Release previousRelease = null;
    // 查询上一次的发布信息
    if (childNamespace != null) {
      previousRelease = findLatestActiveRelease(namespace);
    }

    // master发布
    Map<String, Object> operationContext = Maps.newLinkedHashMap();
    operationContext.put(ReleaseOperationContext.IS_EMERGENCY_PUBLISH, isEmergencyPublish);
    Release release = masterRelease(namespace, releaseName, releaseComment, operateNamespaceItems,
        operator, ReleaseOperation.NORMAL_RELEASE, operationContext);

    // 合并至分支并且自动发布
    if (childNamespace != null) {
      mergeFromMasterAndPublishBranch(namespace, childNamespace, operateNamespaceItems, releaseName,
          releaseComment, operator, previousRelease, release, isEmergencyPublish);
    }
    return release;
  }

  /**
   * 发布分支名称空间
   *
   * @param parentNamespace     父名称空间
   * @param childNamespace      子名称空间
   * @param childNamespaceItems 子名称空间属性配置项
   * @param releaseName         发布名称
   * @param releaseComment      发布备注
   * @param operator            操作者
   * @param isEmergencyPublish  是否紧急发布
   * @param grayDelKeys         灰度发布的Key
   * @return 发布信息
   */
  private Release publishBranchNamespace(Namespace parentNamespace, Namespace childNamespace,
      Map<String, String> childNamespaceItems, String releaseName, String releaseComment,
      String operator, boolean isEmergencyPublish, Set<String> grayDelKeys) {

    // 父名称空间上一次的发布信息
    Release parentLatestRelease = findLatestActiveRelease(parentNamespace);
    Map<String, String> parentConfigurations = parentLatestRelease != null ? gson.fromJson(
        parentLatestRelease.getConfigurations(), GsonType.CONFIG) : new LinkedHashMap<>();

    // 基发布id
    long baseReleaseId = parentLatestRelease == null ? 0 : parentLatestRelease.getId();

    // 合并后的待发布配置信息
    Map<String, String> configsToPublish = mergeConfiguration(parentConfigurations,
        childNamespaceItems);

    // 移除灰度发布待删除的key
    if (CollectionUtils.isNotEmpty(grayDelKeys)) {
      for (String key : grayDelKeys) {
        configsToPublish.remove(key);
      }
    }
    // 分支发布
    return branchRelease(parentNamespace, childNamespace, releaseName, releaseComment,
        configsToPublish, baseReleaseId, operator, ReleaseOperation.GRAY_RELEASE,
        isEmergencyPublish, childNamespaceItems.keySet());

  }

  /**
   * 删除灰色规则指定的KEY后再发布
   *
   * @param namespace          名称空间
   * @param releaseName        发布名称
   * @param releaseComment     发布备注
   * @param operator           操作者
   * @param isEmergencyPublish 是否紧急发布
   * @param grayDelKeys        灰度发布待删除的规则key
   * @return 发布信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Release grayDeletionPublish(Namespace namespace, String releaseName, String releaseComment,
      String operator, boolean isEmergencyPublish, Set<String> grayDelKeys) {
    // 检查锁
    checkLock(namespace, isEmergencyPublish, operator);
    // 操作名称空间属性配置项
    Map<String, String> operateNamespaceItems = getNamespaceItems(namespace);
    // 父名称空间信息
    Namespace parentNamespace = namespaceService.findParentNamespace(namespace);

    // 发布分支名称空间
    if (parentNamespace != null) {
      return publishBranchNamespace(parentNamespace, namespace, operateNamespaceItems,
          releaseName, releaseComment, operator, isEmergencyPublish, grayDelKeys);
    }
    throw new NotFoundException("Parent namespace not found");
  }

  /**
   * 检查非紧急情况下是否加锁
   *
   * @param namespace          名称空间
   * @param isEmergencyPublish 是否紧急发布
   * @param operator           操作者
   */
  private void checkLock(Namespace namespace, boolean isEmergencyPublish, String operator) {
    if (!isEmergencyPublish) {
      NamespaceLock lock = namespaceLockService.findLock(namespace.getId());
      if (lock != null && lock.getDataChangeCreatedBy().equals(operator)) {
        throw new BadRequestException("Config can not be published by yourself.");
      }
    }
  }

  /**
   * 从master发布并且发布分支
   *
   * @param parentNamespace       父名称空间
   * @param childNamespace        子名称空间
   * @param parentNamespaceItems  父名称空间属性配置项
   * @param releaseName           发布名称
   * @param releaseComment        发布备注
   * @param operator              操作者
   * @param masterPreviousRelease master上一次的发布信息
   * @param parentRelease         父发布信息
   * @param isEmergencyPublish    是否紧急发布
   */
  private void mergeFromMasterAndPublishBranch(Namespace parentNamespace, Namespace childNamespace,
      Map<String, String> parentNamespaceItems, String releaseName, String releaseComment, String
      operator, Release masterPreviousRelease, Release parentRelease, boolean isEmergencyPublish) {
    // 子名称空间上一次的发布信息
    Release childNamespaceLatestActiveRelease = findLatestActiveRelease(childNamespace);

    // 子发布配置信息
    Map<String, String> childReleaseConfiguration;
    // 分支发布的key列表
    Collection<String> branchReleaseKeys;
    if (childNamespaceLatestActiveRelease != null) {
      childReleaseConfiguration = gson.fromJson(childNamespaceLatestActiveRelease
          .getConfigurations(), GsonType.CONFIG);
      branchReleaseKeys = getBranchReleaseKeys(childNamespaceLatestActiveRelease.getId());
    } else {
      // 默认的情况
      childReleaseConfiguration = Collections.emptyMap();
      branchReleaseKeys = null;
    }

    // 父名称空间旧配置信息
    Map<String, String> parentNamespaceOldConfiguration = masterPreviousRelease == null ?
        null : gson.fromJson(masterPreviousRelease.getConfigurations(), GsonType.CONFIG);

    // 子名称空间待发布的配置信息
    Map<String, String> childNamespaceToPublishConfigs =
        calculateChildNamespaceToPublishConfiguration(parentNamespaceOldConfiguration,
            parentNamespaceItems, childReleaseConfiguration, branchReleaseKeys);

    // 比较
    if (!childNamespaceToPublishConfigs.equals(childReleaseConfiguration)) {
      // 分支发布
      branchRelease(parentNamespace, childNamespace, releaseName, releaseComment,
          childNamespaceToPublishConfigs, parentRelease.getId(), operator,
          ReleaseOperation.MASTER_NORMAL_RELEASE_MERGE_TO_GRAY, isEmergencyPublish,
          branchReleaseKeys);
    }

  }

  /**
   * 获取分支发布的key
   *
   * @param releaseId 发布id
   * @return 分支发布的key列表
   */
  private Collection<String> getBranchReleaseKeys(long releaseId) {
    // 发布历史记录
    Page<ReleaseHistory> releaseHistories = releaseHistoryService
        .findByReleaseIdAndOperationInOrderByIdDesc(releaseId, BRANCH_RELEASE_OPERATIONS,
            FIRST_ITEM);

    if (!releaseHistories.hasContent()) {
      return Collections.emptyList();
    }

    // 发布操作上下文
    Map<String, Object> operationContext = gson.fromJson(releaseHistories.getContent().get(0)
        .getOperationContext(), OPERATION_CONTEXT_TYPE_REFERENCE);
    if (operationContext == null || !operationContext.containsKey(ReleaseOperationContext
        .BRANCH_RELEASE_KEYS)) {
      return Collections.emptyList();
    }

    return (Collection<String>) operationContext.get(ReleaseOperationContext.BRANCH_RELEASE_KEYS);
  }

  /**
   * 分支名称空间发布
   *
   * @param parentNamespace     父名称空间
   * @param childNamespace      子名称空间
   * @param childNamespaceItems 字名称空间属性配置项
   * @param releaseName         发布名称
   * @param releaseComment      发布备注
   * @param operator            操作者
   * @param isEmergencyPublish  是否紧急发布
   * @return 发布信息
   */
  private Release publishBranchNamespace(Namespace parentNamespace, Namespace childNamespace,
      Map<String, String> childNamespaceItems,
      String releaseName, String releaseComment,
      String operator, boolean isEmergencyPublish) {
    return publishBranchNamespace(parentNamespace, childNamespace, childNamespaceItems, releaseName,
        releaseComment, operator, isEmergencyPublish, null);
  }

  /**
   * master发布
   *
   * @param namespace        名称空间
   * @param releaseName      发布名称
   * @param releaseComment   发布备注
   * @param configurations   配置列表
   * @param operator         操作者
   * @param releaseOperation 发布操作
   * @param operationContext 操作上下文
   * @return 发布信息
   */
  private Release masterRelease(Namespace namespace, String releaseName, String releaseComment,
      Map<String, String> configurations, String operator, int releaseOperation,
      Map<String, Object> operationContext) {
    // 上一次的发布信息
    Release lastActiveRelease = findLatestActiveRelease(namespace);
    long previousReleaseId = lastActiveRelease == null ? 0 : lastActiveRelease.getId();
    // 创建发布信息
    Release release = createRelease(namespace, releaseName, releaseComment,
        configurations, operator);

    // 创建发布历史
    releaseHistoryService.createReleaseHistory(namespace.getAppId(), namespace.getClusterName(),
        namespace.getNamespaceName(), namespace.getClusterName(),
        release.getId(), previousReleaseId, releaseOperation,
        operationContext, operator);
    return release;
  }

  /**
   * 分支发布
   *
   * @param parentNamespace    父名称空间
   * @param childNamespace     子名称空间
   * @param releaseName        发布名称
   * @param releaseComment     发布备注
   * @param configurations     待发布的配置信息
   * @param baseReleaseId      发布的基id
   * @param operator           操作者
   * @param releaseOperation   发布操作
   * @param isEmergencyPublish 是否紧急发布
   * @param branchReleaseKeys  子名称空间的属性配置项的key值
   * @return 发布信息
   */
  private Release branchRelease(Namespace parentNamespace, Namespace childNamespace,
      String releaseName, String releaseComment, Map<String, String> configurations,
      long baseReleaseId, String operator, int releaseOperation, boolean isEmergencyPublish,
      Collection<String> branchReleaseKeys) {
    // 上一次的发布信息
    Release previousRelease = findLatestActiveRelease(childNamespace.getAppId(),
        childNamespace.getClusterName(), childNamespace.getNamespaceName());
    // 上一次的发布id
    long previousReleaseId = previousRelease == null ? 0 : previousRelease.getId();

    Map<String, Object> releaseOperationContext = Maps.newLinkedHashMap();
    releaseOperationContext.put(ReleaseOperationContext.BASE_RELEASE_ID, baseReleaseId);
    releaseOperationContext.put(ReleaseOperationContext.IS_EMERGENCY_PUBLISH, isEmergencyPublish);
    releaseOperationContext.put(ReleaseOperationContext.BRANCH_RELEASE_KEYS, branchReleaseKeys);

    Release release = createRelease(childNamespace, releaseName, releaseComment, configurations,
        operator);

    // 更新灰度发布规则
    GrayReleaseRule grayReleaseRule = namespaceBranchService.updateRulesReleaseId(childNamespace
            .getAppId(), parentNamespace.getClusterName(), childNamespace.getNamespaceName(),
        childNamespace.getClusterName(), release.getId(), operator);

    // 设置灰度发布规则属性
    if (grayReleaseRule != null) {
      releaseOperationContext.put(ReleaseOperationContext.RULES, GrayReleaseRuleItemTransformer
          .batchTransformFromJSON(grayReleaseRule.getRules()));
    }

    // 创建发布历史记录
    releaseHistoryService.createReleaseHistory(parentNamespace.getAppId(), parentNamespace
            .getClusterName(), parentNamespace.getNamespaceName(), childNamespace.getClusterName(),
        release.getId(), previousReleaseId, releaseOperation, releaseOperationContext, operator);
    return release;
  }

  /**
   * 合并配置
   *
   * @param baseConfigurations  父配置
   * @param coverConfigurations 子配置
   * @return 合并后的配置信息
   */
  private Map<String, String> mergeConfiguration(Map<String, String> baseConfigurations,
      Map<String, String> coverConfigurations) {
    Map<String, String> result = new LinkedHashMap<>();
    // 拷贝父配置信息
    for (Map.Entry<String, String> entry : baseConfigurations.entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }

    // 更新和添加配置信息
    for (Map.Entry<String, String> entry : coverConfigurations.entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  /**
   * 获取名称空间下的属性的配置项
   *
   * @param namespace 名称空间
   * @return 配置项
   */
  private Map<String, String> getNamespaceItems(Namespace namespace) {
    List<Item> items = itemService.findItemsWithOrdered(namespace.getId());
    Map<String, String> configurations = new LinkedHashMap<>();
    for (Item item : items) {
      if (StringUtils.isBlank(item.getKey())) {
        continue;
      }
      configurations.put(item.getKey(), item.getValue());
    }
    return configurations;
  }

  /**
   * 创建发布信息
   *
   * @param namespace      名称空间
   * @param name           名称
   * @param comment        备注
   * @param configurations 配置信息
   * @param operator       操作者
   * @return 构建的发布信息
   */
  private Release createRelease(Namespace namespace, String name, String comment,
      Map<String, String> configurations, String operator) {
    Release release = new Release();
    release.setReleaseKey(ReleaseKeyGenerator.generateReleaseKey(namespace));
    release.setDataChangeCreatedTime(new Date());
    release.setDataChangeCreatedBy(operator);
    release.setDataChangeLastModifiedBy(operator);
    release.setName(name);
    release.setComment(comment);
    release.setAppId(namespace.getAppId());
    release.setClusterName(namespace.getClusterName());
    release.setNamespaceName(namespace.getNamespaceName());
    release.setConfigurations(gson.toJson(configurations));
    // 保存
    release = releaseRepository.save(release);

    // 解锁
    namespaceLockService.unlock(namespace.getId());
    // 添加审记记录
    auditService.audit(Release.class.getSimpleName(), release.getId(), Audit.OP.INSERT,
        release.getDataChangeCreatedBy());
    return release;
  }

  /**
   * 回滚
   *
   * @param releaseId 发布id
   * @param operator  操作者
   * @return 发布信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Release rollback(long releaseId, String operator) {
    Release release = findOne(releaseId);
    // 检查发布信息是否存在与废弃
    if (release == null) {
      throw new NotFoundException("release not found");
    }
    if (release.isAbandoned()) {
      throw new BadRequestException("release is not active");
    }

    String appId = release.getAppId();
    String clusterName = release.getClusterName();
    String namespaceName = release.getNamespaceName();

    PageRequest page = PageRequest.of(0, 2);
    // 查询上两次的发布信息
    List<Release> twoLatestActiveReleases = findActiveReleases(appId, clusterName, namespaceName,
        page);

    if (twoLatestActiveReleases == null || twoLatestActiveReleases.size() < 2) {
      throw new BadRequestException(String.format(
          "Can't rollback namespace(appId=%s, clusterName=%s, namespaceName=%s) because there is only one active release",
          appId, clusterName, namespaceName));
    }

    release.setAbandoned(true);
    release.setDataChangeLastModifiedBy(operator);

    // 保存
    releaseRepository.save(release);

    // 保存发布历史记录
    releaseHistoryService.createReleaseHistory(appId, clusterName,
        namespaceName, clusterName, twoLatestActiveReleases.get(1).getId(),
        release.getId(), ReleaseOperation.ROLLBACK, null, operator);

    // 如果名称空间有子名称空间，则发布子名称空间
    rollbackChildNamespace(appId, clusterName, namespaceName, twoLatestActiveReleases, operator);

    return release;
  }

  /**
   * 指定范围发布id的发布操作回滚
   *
   * @param releaseId   开始的发布id
   * @param toReleaseId 结束的发布id
   * @param operator    操作人
   * @return 发布信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Release rollbackTo(long releaseId, long toReleaseId, String operator) {
    if (releaseId == toReleaseId) {
      throw new BadRequestException("current release equal to target release");
    }

    // 检查发布信息是否存在与废弃
    Release release = findOne(releaseId);
    Release toRelease = findOne(toReleaseId);
    if (release == null || toRelease == null) {
      throw new NotFoundException("release not found");
    }
    if (release.isAbandoned() || toRelease.isAbandoned()) {
      throw new BadRequestException("release is not active");
    }

    String appId = release.getAppId();
    String clusterName = release.getClusterName();
    String namespaceName = release.getNamespaceName();

    //  指定范围发布id未废弃的发布信息列表
    List<Release> releases = findActiveReleasesBetween(appId, clusterName, namespaceName,
        toReleaseId, releaseId);

    for (int i = 0; i < releases.size() - 1; i++) {
      releases.get(i).setAbandoned(true);
      releases.get(i).setDataChangeLastModifiedBy(operator);
    }

    // 批量保存
    releaseRepository.saveAll(releases);
    // 保存发布记录
    releaseHistoryService.createReleaseHistory(appId, clusterName,
        namespaceName, clusterName, toReleaseId,
        release.getId(), ReleaseOperation.ROLLBACK, null, operator);

    // 如果名称空间有子名称空间，则发布子名称空间
    rollbackChildNamespace(appId, clusterName, namespaceName,
        Lists.newArrayList(release, toRelease), operator);

    return release;
  }

  /**
   * 回滚子名称空间
   *
   * @param appId                                 应用id
   * @param clusterName                           集群id
   * @param namespaceName                         名称空间名称
   * @param parentNamespaceTwoLatestActiveRelease 父名称空间上两次发布信息
   * @param operator                              操作者
   */
  private void rollbackChildNamespace(String appId, String clusterName, String namespaceName,
      List<Release> parentNamespaceTwoLatestActiveRelease, String operator) {
    Namespace parentNamespace = namespaceService.findOne(appId, clusterName, namespaceName);
    Namespace childNamespace = namespaceService.findChildNamespace(appId, clusterName,
        namespaceName);
    if (parentNamespace == null || childNamespace == null) {
      return;
    }

    // 子名称空间上一次发布信息
    Release childNamespaceLatestActiveRelease = findLatestActiveRelease(childNamespace);
    // 子发布配置信息
    Map<String, String> childReleaseConfiguration;
    // 分支发布的Key集合
    Collection<String> branchReleaseKeys;

    if (childNamespaceLatestActiveRelease != null) {
      childReleaseConfiguration = gson.fromJson(childNamespaceLatestActiveRelease
          .getConfigurations(), GsonType.CONFIG);
      // 分支发布的key
      branchReleaseKeys = getBranchReleaseKeys(childNamespaceLatestActiveRelease.getId());
    } else {
      childReleaseConfiguration = Collections.emptyMap();
      branchReleaseKeys = null;
    }

    // 丢弃的发布信息
    Release abandonedRelease = parentNamespaceTwoLatestActiveRelease.get(0);
    // 父名称空间上一次的新发布信息
    Release parentNamespaceNewLatestRelease = parentNamespaceTwoLatestActiveRelease.get(1);

    // 父名称空间丢弃的配置
    Map<String, String> parentNamespaceAbandonedConfiguration = gson.fromJson(
        abandonedRelease.getConfigurations(), GsonType.CONFIG);

    // 父名称空间上一次发布信息的新配置
    Map<String, String> parentNamespaceNewLatestConfiguration =
        gson.fromJson(parentNamespaceNewLatestRelease.getConfigurations(), GsonType.CONFIG);

    // 子名称空间的新配置
    Map<String, String> childNamespaceNewConfiguration =
        calculateChildNamespaceToPublishConfiguration(parentNamespaceAbandonedConfiguration,
            parentNamespaceNewLatestConfiguration, childReleaseConfiguration, branchReleaseKeys);

    //compare
    if (!childNamespaceNewConfiguration.equals(childReleaseConfiguration)) {
      // 分支发布
      branchRelease(parentNamespace, childNamespace, TIMESTAMP_FORMAT.format(
          new Date()) + "-master-rollback-merge-to-gray", "", childNamespaceNewConfiguration,
          parentNamespaceNewLatestRelease.getId(), operator,
          ReleaseOperation.MATER_ROLLBACK_MERGE_TO_GRAY, false, branchReleaseKeys);
    }
  }

  /**
   * 计算子名称空间待发布的配置
   *
   * @param parentNamespaceOldConfiguration         父名称空间旧的配置信息
   * @param parentNamespaceNewConfiguration         父名称空间新的配置信息
   * @param childNamespaceLatestActiveConfiguration 子名称空间上一次的配置信息
   * @param branchReleaseKeys                       分支发布的key列表
   * @return 配置信息
   */
  private Map<String, String> calculateChildNamespaceToPublishConfiguration(
      Map<String, String> parentNamespaceOldConfiguration,
      Map<String, String> parentNamespaceNewConfiguration,
      Map<String, String> childNamespaceLatestActiveConfiguration,
      Collection<String> branchReleaseKeys) {

    // 首先,计算子名称空间修改的配置项相应的发布配置
    // 子名称空间修改的配置
    Map<String, String> childNamespaceModifiedConfiguration = calculateBranchModifiedItemsAccordingToRelease(
        parentNamespaceOldConfiguration, childNamespaceLatestActiveConfiguration,
        branchReleaseKeys);

    // 第二,将修改过的子名称空间追加到父名称空间的最新配置
    return mergeConfiguration(parentNamespaceNewConfiguration, childNamespaceModifiedConfiguration);
  }

  /**
   * 计算分支修改配置项相应的发布配置信息
   *
   * @param masterReleaseConfigs master发布配置信息
   * @param branchReleaseConfigs 分支发布配置信息
   * @param branchReleaseKeys    分支发布的key列表
   * @return 配置信息
   */
  private Map<String, String> calculateBranchModifiedItemsAccordingToRelease(
      Map<String, String> masterReleaseConfigs, Map<String, String> branchReleaseConfigs,
      Collection<String> branchReleaseKeys) {

    //  修改的配置信息
    Map<String, String> modifiedConfigs = new LinkedHashMap<>();

    if (MapUtils.isEmpty(branchReleaseConfigs)) {
      return modifiedConfigs;
    }

    // 新的逻辑，根据分支发布键检索修改过的配置
    if (branchReleaseKeys != null) {
      for (String branchReleaseKey : branchReleaseKeys) {
        if (branchReleaseConfigs.containsKey(branchReleaseKey)) {
          modifiedConfigs.put(branchReleaseKey, branchReleaseConfigs.get(branchReleaseKey));
        }
      }

      return modifiedConfigs;
    }

    // 旧的逻辑，通过比较branchReleaseConfigs和masterReleaseConfigs来检索修改过的配置
    if (MapUtils.isEmpty(masterReleaseConfigs)) {
      return branchReleaseConfigs;
    }

    for (Map.Entry<String, String> entry : branchReleaseConfigs.entrySet()) {
      if (!Objects.equals(entry.getValue(), masterReleaseConfigs.get(entry.getKey()))) {
        modifiedConfigs.put(entry.getKey(), entry.getValue());
      }
    }

    return modifiedConfigs;

  }

  /**
   * 批量删除
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param operator      操作者
   * @return 影响的行数
   */
  @Transactional(rollbackFor = Exception.class)
  public int batchDelete(String appId, String clusterName, String namespaceName, String operator) {
    return releaseRepository.batchDelete(appId, clusterName, namespaceName, operator);
  }

}
