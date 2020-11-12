package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleDTO;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.ItemsComparator;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.tracer.Tracer;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 名称空间分支 Service
 */
@Service
public class NamespaceBranchService {

  private final ItemsComparator itemsComparator;
  private final UserInfoHolder userInfoHolder;
  private final NamespaceService namespaceService;
  private final ItemService itemService;
  private final AdminServiceAPI.NamespaceBranchAPI namespaceBranchAPI;
  private final ReleaseService releaseService;

  public NamespaceBranchService(
      final ItemsComparator itemsComparator,
      final UserInfoHolder userInfoHolder,
      final NamespaceService namespaceService,
      final ItemService itemService,
      final AdminServiceAPI.NamespaceBranchAPI namespaceBranchAPI,
      final ReleaseService releaseService) {
    this.itemsComparator = itemsComparator;
    this.userInfoHolder = userInfoHolder;
    this.namespaceService = namespaceService;
    this.itemService = itemService;
    this.namespaceBranchAPI = namespaceBranchAPI;
    this.releaseService = releaseService;
  }

  /**
   * 创建分支的子名称空间
   *
   * @param appId         应用名称
   * @param env           环境
   * @param namespaceName 名称空间名称
   * @return 创建的分支子名称空间信息
   */
  @Transactional(rollbackFor = Exception.class)
  public NamespaceDTO createBranch(String appId, Env env, String parentClusterName,
      String namespaceName) {
    String operator = userInfoHolder.getUser().getUserId();
    return createBranch(appId, env, parentClusterName, namespaceName, operator);
  }

  /**
   * 创建分支的子名称空间
   *
   * @param appId         应用名称
   * @param env           环境
   * @param namespaceName 名称空间名称
   * @param operator      操作者
   * @return 创建的分支子名称空间信息
   */
  @Transactional(rollbackFor = Exception.class)
  public NamespaceDTO createBranch(String appId, Env env, String parentClusterName,
      String namespaceName, String operator) {
    NamespaceDTO createdBranch = namespaceBranchAPI
        .createBranch(appId, env, parentClusterName, namespaceName,
            operator);

    Tracer.logEvent(TracerEventType.CREATE_GRAY_RELEASE, String.format("%s+%s+%s+%s", appId, env,
        parentClusterName, namespaceName));
    return createdBranch;

  }

  /**
   * 查询名称空间分支（子名称空间）灰度发布规则信息
   *
   * @param appId         应用名称
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @return 灰度发布规则信息
   */
  public GrayReleaseRuleDTO findBranchGrayRules(String appId, Env env, String clusterName,
      String namespaceName, String branchName) {
    return namespaceBranchAPI.findBranchGrayRules(appId, env, clusterName, namespaceName,
        branchName);
  }

  /**
   * 更新名称空间分支(子名称空间)灰度发布规则
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @param rules         灰度发布规则信息
   */
  public void updateBranchGrayRules(String appId, Env env, String clusterName, String namespaceName,
      String branchName, GrayReleaseRuleDTO rules) {

    String operator = userInfoHolder.getUser().getUserId();
    updateBranchGrayRules(appId, env, clusterName, namespaceName, branchName, rules, operator);
  }

  /**
   * 更新名称空间分支(子名称空间)灰度发布规则
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @param rules         灰度发布规则信息
   * @param operator      操作者
   */
  public void updateBranchGrayRules(String appId, Env env, String clusterName, String namespaceName,
      String branchName, GrayReleaseRuleDTO rules, String operator) {
    rules.setDataChangeCreatedBy(operator);
    rules.setDataChangeLastModifiedBy(operator);

    namespaceBranchAPI.updateBranchGrayRules(appId, env, clusterName, namespaceName, branchName,
        rules);

    Tracer.logEvent(TracerEventType.UPDATE_GRAY_RELEASE_RULE,
        String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
  }

  /**
   * 删除分支
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   */
  public void deleteBranch(String appId, Env env, String clusterName, String namespaceName,
      String branchName) {

    String operator = userInfoHolder.getUser().getUserId();
    deleteBranch(appId, env, clusterName, namespaceName, branchName, operator);
  }

  /**
   * 删除分支
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @param operator      操作人
   */
  public void deleteBranch(String appId, Env env, String clusterName, String namespaceName,
      String branchName, String operator) {
    namespaceBranchAPI.deleteBranch(appId, env, clusterName, namespaceName, branchName, operator);

    Tracer.logEvent(TracerEventType.DELETE_GRAY_RELEASE,
        String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
  }

  /**
   * 全量发布
   *
   * @param appId              应用id
   * @param env                环境
   * @param clusterName        集群名称
   * @param namespaceName      名称空间名称
   * @param branchName         分支名
   * @param title              标题
   * @param comment            备注
   * @param isEmergencyPublish 是否紧急发布
   * @param deleteBranch       是否删除分支
   * @return 全量发布信息
   */
  public ReleaseDTO merge(String appId, Env env, String clusterName, String namespaceName,
      String branchName, String title, String comment, boolean isEmergencyPublish,
      boolean deleteBranch) {
    String operator = userInfoHolder.getUser().getUserId();
    return merge(appId, env, clusterName, namespaceName, branchName, title, comment,
        isEmergencyPublish, deleteBranch, operator);
  }

  /**
   * 全量发布
   *
   * @param appId              应用id
   * @param env                环境
   * @param clusterName        集群名称
   * @param namespaceName      名称空间名称
   * @param branchName         分支名
   * @param title              标题
   * @param comment            备注
   * @param isEmergencyPublish 是否紧急发布
   * @param deleteBranch       是否删除分支
   * @param operator           操作人
   * @return 全量发布信息
   */
  public ReleaseDTO merge(String appId, Env env, String clusterName, String namespaceName,
      String branchName, String title, String comment, boolean isEmergencyPublish,
      boolean deleteBranch, String operator) {

    ItemChangeSets changeSets = calculateBranchChangeSet(appId, env, clusterName, namespaceName,
        branchName, operator);

    ReleaseDTO mergedResult = releaseService.updateAndPublish(appId, env, clusterName,
        namespaceName, title, comment, branchName, isEmergencyPublish, deleteBranch, changeSets);

    Tracer.logEvent(TracerEventType.MERGE_GRAY_RELEASE,
        String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));

    return mergedResult;
  }

  /**
   * 计算分支变更配置项
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @param operator      操作者
   * @return 配置项变更列表
   */
  private ItemChangeSets calculateBranchChangeSet(String appId, Env env, String clusterName,
      String namespaceName, String branchName, String operator) {
    NamespaceBO parentNamespace = namespaceService
        .loadNamespaceBO(appId, env, clusterName, namespaceName);

    if (parentNamespace == null) {
      throw new BadRequestException("base namespace not existed");
    }

    if (parentNamespace.getItemModifiedCnt() > 0) {
      throw new BadRequestException("Merge operation failed. Because master has modified items");
    }

    // 主节点配置项列表
    List<ItemDTO> masterItems = itemService.findItems(appId, env, clusterName, namespaceName);
    // 分支节点配置项列表
    List<ItemDTO> branchItems = itemService.findItems(appId, env, branchName, namespaceName);

    // 比较
    ItemChangeSets changeSets = itemsComparator
        .compareIgnoreBlankAndCommentItem(parentNamespace.getBaseInfo().getId(),
            masterItems, branchItems);
    changeSets.setDeleteItems(Collections.emptyList());
    changeSets.setDataChangeLastModifiedBy(operator);
    return changeSets;
  }

  /**
   * 查询名称空间分支信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 名称空间信息
   */
  public NamespaceDTO findBranchBaseInfo(String appId, Env env, String clusterName,
      String namespaceName) {
    return namespaceBranchAPI.findBranch(appId, env, clusterName, namespaceName);
  }

  /**
   * 查询关联的名称空间中的公有名称空间
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 公有的名称空间信息
   */
  public NamespaceBO findBranch(String appId, Env env, String clusterName, String namespaceName) {
    NamespaceDTO namespaceDTO = findBranchBaseInfo(appId, env, clusterName, namespaceName);
    if (namespaceDTO == null) {
      return null;
    }
    return namespaceService
        .loadNamespaceBO(appId, env, namespaceDTO.getClusterName(), namespaceName);
  }

}
