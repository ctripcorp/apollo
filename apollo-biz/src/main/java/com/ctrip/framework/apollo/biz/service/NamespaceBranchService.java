package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.entity.GrayReleaseRule;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.repository.GrayReleaseRuleRepository;
import com.ctrip.framework.apollo.common.constants.NamespaceBranchStatus;
import com.ctrip.framework.apollo.common.constants.ReleaseOperation;
import com.ctrip.framework.apollo.common.constants.ReleaseOperationContext;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.GrayReleaseRuleItemTransformer;
import com.ctrip.framework.apollo.common.utils.UniqueKeyGenerator;
import com.google.common.collect.Maps;
import java.util.Map;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 名称空间分支 Service层
 */
@Service
public class NamespaceBranchService {

  private final AuditService auditService;
  private final GrayReleaseRuleRepository grayReleaseRuleRepository;
  private final ClusterService clusterService;
  private final ReleaseService releaseService;
  private final NamespaceService namespaceService;
  private final ReleaseHistoryService releaseHistoryService;

  public NamespaceBranchService(
      final AuditService auditService,
      final GrayReleaseRuleRepository grayReleaseRuleRepository,
      final ClusterService clusterService,
      final @Lazy ReleaseService releaseService,
      final NamespaceService namespaceService,
      final ReleaseHistoryService releaseHistoryService) {
    this.auditService = auditService;
    this.grayReleaseRuleRepository = grayReleaseRuleRepository;
    this.clusterService = clusterService;
    this.releaseService = releaseService;
    this.namespaceService = namespaceService;
    this.releaseHistoryService = releaseHistoryService;
  }

  /**
   * 创建名称空间分支(子名称空间)
   *
   * @param appId             应用名称
   * @param parentClusterName 父集群名称
   * @param namespaceName     名称空间名称
   * @param operator          操作者
   * @return 创建的分支子名称空间信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Namespace createBranch(String appId, String parentClusterName, String namespaceName,
      String operator) {
    // 子名称空间
    Namespace childNamespace = findBranch(appId, parentClusterName, namespaceName);
    if (childNamespace != null) {
      throw new BadRequestException("namespace already has branch");
    }

    // 查询父集群
    Cluster parentCluster = clusterService.findOne(appId, parentClusterName);
    if (parentCluster == null || parentCluster.getParentClusterId() != 0) {
      throw new BadRequestException("cluster not exist or illegal cluster");
    }

    // 构建子集群
    Cluster childCluster = createChildCluster(appId, parentCluster, namespaceName, operator);
    Cluster createdChildCluster = clusterService.saveWithoutInstanceOfAppNamespaces(childCluster);

    // 创建子集群名称空间
    childNamespace = createNamespaceBranch(appId, createdChildCluster.getName(), namespaceName,
        operator);
    return namespaceService.save(childNamespace);
  }

  /**
   * 查找名称空间分支(子名称空间)
   *
   * @param appId             应用名称
   * @param parentClusterName 父集群名称
   * @param namespaceName     名称空间名称
   * @return 名称空间信息
   */
  public Namespace findBranch(String appId, String parentClusterName, String namespaceName) {
    return namespaceService.findChildNamespace(appId, parentClusterName, namespaceName);
  }

  /**
   * 查询名称空间分支(子名称空间)灰度发布规则
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @return 灰度发布规则信息
   */
  public GrayReleaseRule findBranchGrayRules(String appId, String clusterName, String namespaceName,
      String branchName) {
    return grayReleaseRuleRepository
        .findTopByAppIdAndClusterNameAndNamespaceNameAndBranchNameOrderByIdDesc(appId, clusterName,
            namespaceName, branchName);
  }

  /**
   * 更新名称空间分支(子名称空间)灰度发布规则
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @param newRules      新的灰度发布规则
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateBranchGrayRules(String appId, String clusterName, String namespaceName,
      String branchName, GrayReleaseRule newRules) {
    // 更新分支灰度发布规则
    doUpdateBranchGrayRules(appId, clusterName, namespaceName, branchName, newRules, true,
        ReleaseOperation.APPLY_GRAY_RULES);
  }

  /**
   * 更新名称空间分支(子名称空间)灰度发布规则
   *
   * @param appId                应用id
   * @param clusterName          集群名称
   * @param namespaceName        名称空间名称
   * @param branchName           分支名称
   * @param newRules             新规则
   * @param recordReleaseHistory 是否记录发布历史
   * @param releaseOperation     发布操作
   */
  private void doUpdateBranchGrayRules(String appId, String clusterName, String namespaceName,
      String branchName, GrayReleaseRule newRules, boolean recordReleaseHistory,
      int releaseOperation) {
    // 上一次的灰度发布规则
    GrayReleaseRule oldRules = grayReleaseRuleRepository
        .findTopByAppIdAndClusterNameAndNamespaceNameAndBranchNameOrderByIdDesc(appId, clusterName,
            namespaceName, branchName);
    // 上一次的分支发布信息
    Release latestBranchRelease = releaseService
        .findLatestActiveRelease(appId, branchName, namespaceName);

    long latestBranchReleaseId = latestBranchRelease != null ? latestBranchRelease.getId() : 0;

    newRules.setReleaseId(latestBranchReleaseId);

    // 保存新的灰度发布规则
    grayReleaseRuleRepository.save(newRules);

    // 删除旧的规则
    if (oldRules != null) {
      grayReleaseRuleRepository.delete(oldRules);
    }

    if (recordReleaseHistory) {
      Map<String, Object> releaseOperationContext = Maps.newHashMap();
      releaseOperationContext.put(ReleaseOperationContext.RULES, GrayReleaseRuleItemTransformer
          .batchTransformFromJSON(newRules.getRules()));
      if (oldRules != null) {
        releaseOperationContext.put(ReleaseOperationContext.OLD_RULES,
            GrayReleaseRuleItemTransformer.batchTransformFromJSON(oldRules.getRules()));
      }

      // 记录发布历史
      releaseHistoryService.createReleaseHistory(appId, clusterName, namespaceName, branchName,
          latestBranchReleaseId,
          latestBranchReleaseId, releaseOperation, releaseOperationContext,
          newRules.getDataChangeLastModifiedBy());
    }
  }

  /**
   * 更新灰度发布规则.
   *
   * @param appId           应用id
   * @param clusterName     集群名称
   * @param namespaceName   名称空间名称
   * @param branchName      分支名称
   * @param latestReleaseId 最后的发布id
   * @param operator        操作者
   * @return 灰度发布规则
   */
  @Transactional(rollbackFor = Exception.class)
  public GrayReleaseRule updateRulesReleaseId(String appId, String clusterName,
      String namespaceName, String branchName,
      long latestReleaseId, String operator) {
    GrayReleaseRule oldRules = grayReleaseRuleRepository.
        findTopByAppIdAndClusterNameAndNamespaceNameAndBranchNameOrderByIdDesc(appId, clusterName,
            namespaceName, branchName);

    if (oldRules == null) {
      return null;
    }

    GrayReleaseRule newRules = new GrayReleaseRule();
    newRules.setBranchStatus(NamespaceBranchStatus.ACTIVE);
    newRules.setReleaseId(latestReleaseId);
    newRules.setRules(oldRules.getRules());
    newRules.setAppId(oldRules.getAppId());
    newRules.setClusterName(oldRules.getClusterName());
    newRules.setNamespaceName(oldRules.getNamespaceName());
    newRules.setBranchName(oldRules.getBranchName());
    newRules.setDataChangeCreatedBy(operator);
    newRules.setDataChangeLastModifiedBy(operator);

    //保存新的灰度发布规则,删除旧的规则
    grayReleaseRuleRepository.save(newRules);
    grayReleaseRuleRepository.delete(oldRules);

    return newRules;
  }

  /**
   * 删除名称空间分支(子名称空间)
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @param branchStatus  分支状态
   * @param operator      操作人
   */
  @Transactional(rollbackFor = Exception.class)
  public void deleteBranch(String appId, String clusterName, String namespaceName,
      String branchName, int branchStatus, String operator) {

    // 待删除的集群信息
    Cluster toDeleteCluster = clusterService.findOne(appId, branchName);
    if (toDeleteCluster == null) {
      return;
    }

    // 分支上一次的发布信息
    Release latestBranchRelease = releaseService
        .findLatestActiveRelease(appId, branchName, namespaceName);

    long latestBranchReleaseId = latestBranchRelease != null ? latestBranchRelease.getId() : 0;

    //update branch rules
    GrayReleaseRule deleteRule = new GrayReleaseRule();
    deleteRule.setRules("[]");
    deleteRule.setAppId(appId);
    deleteRule.setClusterName(clusterName);
    deleteRule.setNamespaceName(namespaceName);
    deleteRule.setBranchName(branchName);
    deleteRule.setBranchStatus(branchStatus);
    deleteRule.setDataChangeLastModifiedBy(operator);
    deleteRule.setDataChangeCreatedBy(operator);

    // 更新分支灰度发布规则
    doUpdateBranchGrayRules(appId, clusterName, namespaceName, branchName, deleteRule, false, -1);

    // 删除分支集群信息
    clusterService.delete(toDeleteCluster.getId(), operator);

    int releaseOperation = branchStatus == NamespaceBranchStatus.MERGED ? ReleaseOperation
        .GRAY_RELEASE_DELETED_AFTER_MERGE : ReleaseOperation.ABANDON_GRAY_RELEASE;

    // 发布历史记录
    releaseHistoryService.createReleaseHistory(appId, clusterName, namespaceName, branchName,
        latestBranchReleaseId, latestBranchReleaseId, releaseOperation, null, operator);

    // 记录日志审计信息
    auditService.audit("Branch", toDeleteCluster.getId(), Audit.OP.DELETE, operator);
  }

  /**
   * 构建子集群
   *
   * @param appId         应用id
   * @param parentCluster 父集群
   * @param namespaceName 名称空间名称
   * @param operator      操作人
   * @return 集群信息
   */
  private Cluster createChildCluster(String appId, Cluster parentCluster,
      String namespaceName, String operator) {

    Cluster childCluster = new Cluster();
    childCluster.setAppId(appId);
    childCluster.setParentClusterId(parentCluster.getId());
    childCluster.setName(UniqueKeyGenerator.generate(appId, parentCluster.getName(),
        namespaceName));
    childCluster.setDataChangeCreatedBy(operator);
    childCluster.setDataChangeLastModifiedBy(operator);
    return childCluster;
  }


  /**
   * 构建名称空间分支(子名称空间)
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param operator      操作人
   * @return 名称空间信息
   */
  private Namespace createNamespaceBranch(String appId, String clusterName, String namespaceName,
      String operator) {
    Namespace childNamespace = new Namespace();
    childNamespace.setAppId(appId);
    childNamespace.setClusterName(clusterName);
    childNamespace.setNamespaceName(namespaceName);
    childNamespace.setDataChangeLastModifiedBy(operator);
    childNamespace.setDataChangeCreatedBy(operator);
    return childNamespace;
  }

}
