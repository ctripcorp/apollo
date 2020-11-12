package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.entity.bo.ItemBO;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 名称空间 Service层
 */
@Slf4j
@Service
public class NamespaceService {

  private static final Gson GSON = new Gson();

  private final PortalConfig portalConfig;
  private final PortalSettings portalSettings;
  private final UserInfoHolder userInfoHolder;
  private final AdminServiceAPI.NamespaceAPI namespaceAPI;
  private final ItemService itemService;
  private final ReleaseService releaseService;
  private final AppNamespaceService appNamespaceService;
  private final InstanceService instanceService;
  private final NamespaceBranchService branchService;
  private final RolePermissionService rolePermissionService;

  public NamespaceService(
      final PortalConfig portalConfig,
      final PortalSettings portalSettings,
      final UserInfoHolder userInfoHolder,
      final AdminServiceAPI.NamespaceAPI namespaceAPI,
      final ItemService itemService,
      final ReleaseService releaseService,
      final AppNamespaceService appNamespaceService,
      final InstanceService instanceService,
      final @Lazy NamespaceBranchService branchService,
      final RolePermissionService rolePermissionService) {
    this.portalConfig = portalConfig;
    this.portalSettings = portalSettings;
    this.userInfoHolder = userInfoHolder;
    this.namespaceAPI = namespaceAPI;
    this.itemService = itemService;
    this.releaseService = releaseService;
    this.appNamespaceService = appNamespaceService;
    this.instanceService = instanceService;
    this.branchService = branchService;
    this.rolePermissionService = rolePermissionService;
  }

  /**
   * 保存名称空间
   *
   * @param env       环境
   * @param namespace 名称空间信息
   * @return 保存后的名称空间信息
   */
  public NamespaceDTO createNamespace(Env env, NamespaceDTO namespace) {
    if (StringUtils.isBlank(namespace.getDataChangeCreatedBy())) {
      namespace.setDataChangeCreatedBy(userInfoHolder.getUser().getUserId());
    }
    namespace.setDataChangeLastModifiedBy(userInfoHolder.getUser().getUserId());
    NamespaceDTO createdNamespace = namespaceAPI.createNamespace(env, namespace);

    Tracer.logEvent(TracerEventType.CREATE_NAMESPACE,
        String.format("%s+%s+%s+%s", namespace.getAppId(), env, namespace.getClusterName(),
            namespace.getNamespaceName()));
    return createdNamespace;
  }

  /**
   * 删除名称空间
   *
   * @param env           环境
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   */
  @Transactional(rollbackFor = Exception.class)
  public void deleteNamespace(String appId, Env env, String clusterName, String namespaceName) {

    AppNamespace appNamespace = appNamespaceService.findByAppIdAndName(appId, namespaceName);

    //1. 检查父命名空间是否存在实例
    if (namespaceHasInstances(appId, env, clusterName, namespaceName)) {
      throw new BadRequestException(
          "Can not delete namespace because namespace has active instances");
    }

    //2. 检查子命名空间是否存在实例
    NamespaceDTO childNamespace = branchService
        .findBranchBaseInfo(appId, env, clusterName, namespaceName);
    if (childNamespace != null &&
        namespaceHasInstances(appId, env, childNamespace.getClusterName(), namespaceName)) {
      throw new BadRequestException(
          "Can not delete namespace because namespace's branch has active instances");
    }

    //3. 检查公共命名空间是否存在没有关联命名空间
    if (appNamespace != null && appNamespace.isPublic() && publicAppNamespaceHasAssociatedNamespace(
        namespaceName, env)) {
      throw new BadRequestException(
          "Can not delete public namespace which has associated namespaces");
    }

    String operator = userInfoHolder.getUser().getUserId();
    namespaceAPI.deleteNamespace(env, appId, clusterName, namespaceName, operator);
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
  public NamespaceDTO loadNamespaceBaseInfo(String appId, Env env, String clusterName,
      String namespaceName) {
    NamespaceDTO namespace = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    if (namespace == null) {
      throw new BadRequestException(String.format("Namespace: %s not exist.", namespaceName));
    }
    return namespace;
  }

  /**
   * 查询指定应用id指定集群名称的名称空间列表信息
   *
   * @param appId       应用id
   * @param env         环境
   * @param clusterName 集群名称空间
   * @return 指定应用id指定集群名称的名称空间列表信息
   */
  public List<NamespaceBO> findNamespaceBOs(String appId, Env env, String clusterName) {

    //指定应用id指定集群名称的名称空间列表信息
    List<NamespaceDTO> namespaces = namespaceAPI.findNamespaceByCluster(appId, env, clusterName);
    if (CollectionUtils.isEmpty(namespaces)) {
      throw new BadRequestException("namespaces not exist");
    }

    // NamespaceDTO转NamespaceBO
    List<NamespaceBO> namespaceBOs = new LinkedList<>();
    for (NamespaceDTO namespace : namespaces) {
      NamespaceBO namespaceBO;
      try {
        namespaceBO = transformNamespace2BO(env, namespace);
        namespaceBOs.add(namespaceBO);
      } catch (Exception e) {
        log.error("parse namespace error. app id:{}, env:{}, clusterName:{}, namespace:{}",
            appId, env, clusterName, namespace.getNamespaceName(), e);
        throw e;
      }
    }

    return namespaceBOs;
  }

  /**
   * 通过应用id和集群名称查询名称份之间
   *
   * @param appId       应用id
   * @param env         环境
   * @param clusterName 集群名称空间
   * @return 名称空间列表
   */
  public List<NamespaceDTO> findNamespaces(String appId, Env env, String clusterName) {
    return namespaceAPI.findNamespaceByCluster(appId, env, clusterName);
  }

  /**
   * 查询公有应用名称空间的所有名称空间
   *
   * @param env                 环境
   * @param publicNamespaceName 公有应用名称空间名称
   * @param page                页码
   * @param size                页面大小
   * @return 名称空间列表
   */
  public List<NamespaceDTO> getPublicAppNamespaceAllNamespaces(Env env, String publicNamespaceName,
      int page, int size) {
    return namespaceAPI.getPublicAppNamespaceAllNamespaces(env, publicNamespaceName, page, size);
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
  public NamespaceBO loadNamespaceBO(String appId, Env env, String clusterName,
      String namespaceName) {
    NamespaceDTO namespace = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    if (namespace == null) {
      throw new BadRequestException("namespaces not exist");
    }
    return transformNamespace2BO(env, namespace);
  }

  /**
   * 通过名称空间获取实例数量
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 指定名称空间的实例数量
   */
  public boolean namespaceHasInstances(String appId, Env env, String clusterName,
      String namespaceName) {
    return instanceService.getInstanceCountByNamepsace(appId, env, clusterName, namespaceName) > 0;
  }

  /**
   * 统计指定的公有应用名称空间关联的名称空间数量
   *
   * @param publicNamespaceName 公有应用名称空间名称
   * @param env                 环境
   * @return 指定的公有应用名称空间关联的名称空间数量
   */
  public boolean publicAppNamespaceHasAssociatedNamespace(String publicNamespaceName, Env env) {
    return namespaceAPI.countPublicAppNamespaceAssociatedNamespaces(env, publicNamespaceName) > 0;
  }

  /**
   * 查询关联的名称空间中的公有名称空间
   *
   * @param env           环境
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 公有的名称空间信息
   */
  public NamespaceBO findPublicNamespaceForAssociatedNamespace(Env env, String appId,
      String clusterName, String namespaceName) {
    NamespaceDTO namespace = namespaceAPI.findPublicNamespaceForAssociatedNamespace(env, appId,
        clusterName, namespaceName);

    return transformNamespace2BO(env, namespace);
  }

  /**
   * 获取所有名称空间发布信息
   *
   * @param appId 应用id
   * @return 所有名称空间发布信息
   */
  public Map<String, Map<String, Boolean>> getNamespacesPublishInfo(String appId) {
    Map<String, Map<String, Boolean>> result = Maps.newHashMap();

    Set<Env> envs = portalConfig.publishTipsSupportedEnvs();
    for (Env env : envs) {
      if (portalSettings.isEnvActive(env)) {
        result.put(env.toString(), namespaceAPI.getNamespacePublishInfo(env, appId));
      }
    }

    return result;
  }

  /**
   * NamespaceDTO转名称空间 NamespaceBO
   *
   * @param env       环境
   * @param namespace 名称空间Dto
   * @return 转换后的NamespaceBO
   */
  private NamespaceBO transformNamespace2BO(Env env, NamespaceDTO namespace) {
    NamespaceBO namespaceBO = new NamespaceBO();
    namespaceBO.setBaseInfo(namespace);

    String appId = namespace.getAppId();
    String clusterName = namespace.getClusterName();
    String namespaceName = namespace.getNamespaceName();

    fillAppNamespaceProperties(namespaceBO);

    List<ItemBO> itemBOs = new LinkedList<>();
    namespaceBO.setItems(itemBOs);

    // 最新的发布信息
    ReleaseDTO latestRelease;
    Map<String, String> releaseItems = new HashMap<>();
    Map<String, ItemDTO> deletedItemDTOs = new HashMap<>();
    // 名称空间最新的发布信息
    latestRelease = releaseService.loadLatestRelease(appId, env, clusterName, namespaceName);
    if (latestRelease != null) {
      releaseItems = GSON.fromJson(latestRelease.getConfigurations(), GsonType.CONFIG);
    }

    // 没有发布的配置项
    List<ItemDTO> items = itemService.findItems(appId, env, clusterName, namespaceName);
    int modifiedItemCnt = 0;
    for (ItemDTO itemDTO : items) {
      ItemBO itemBO = transformItem2BO(itemDTO, releaseItems);

      if (itemBO.isModified()) {
        modifiedItemCnt++;
      }
      itemBOs.add(itemBO);
    }

    // 删除配置项
    itemService.findDeletedItems(appId, env, clusterName, namespaceName).forEach(item -> {
      deletedItemDTOs.put(item.getKey(), item);
    });

    List<ItemBO> deletedItems = parseDeletedItems(items, releaseItems, deletedItemDTOs);
    itemBOs.addAll(deletedItems);
    modifiedItemCnt += deletedItems.size();

    namespaceBO.setItemModifiedCnt(modifiedItemCnt);

    return namespaceBO;
  }

  /**
   * 填充应用名称空间属性
   *
   * @param namespace 名称空间
   */
  private void fillAppNamespaceProperties(NamespaceBO namespace) {

    final NamespaceDTO namespaceDTO = namespace.getBaseInfo();
    final String appId = namespaceDTO.getAppId();
    final String clusterName = namespaceDTO.getClusterName();
    final String namespaceName = namespaceDTO.getNamespaceName();
    //先从当前appId下面找,包含私有的和公共的
    AppNamespace appNamespace =
        appNamespaceService
            .findByAppIdAndName(appId, namespaceName);
    //再从公共的app namespace里面找
    if (appNamespace == null) {
      appNamespace = appNamespaceService.findPublicAppNamespace(namespaceName);
    }

    final String format;
    final boolean isPublic;
    if (appNamespace == null) {
      // 脏数据
      log.warn(
          "Dirty data, cannot find appNamespace by namespaceName [{}], appId = {}, cluster = {}, set it format to {}, make public",
          namespaceName, appId, clusterName, ConfigFileFormat.Properties.getValue());
      format = ConfigFileFormat.Properties.getValue();
      isPublic = true; // set to true, because public namespace allowed to delete by user
    } else {
      format = appNamespace.getFormat();
      isPublic = appNamespace.isPublic();
      namespace.setParentAppId(appNamespace.getAppId());
      namespace.setComment(appNamespace.getComment());
    }
    namespace.setFormat(format);
    namespace.setPublic(isPublic);
  }

  /**
   * 解析删除的配置项
   *
   * @param newItems        新的配置项
   * @param releaseItems    发布的配置英
   * @param deletedItemDTOs 删除的配置项
   * @return 配置项列表
   */
  private List<ItemBO> parseDeletedItems(List<ItemDTO> newItems, Map<String, String> releaseItems,
      Map<String, ItemDTO> deletedItemDTOs) {
    Map<String, ItemDTO> newItemMap = BeanUtils.mapByKey("key", newItems);

    List<ItemBO> deletedItems = new LinkedList<>();
    for (Map.Entry<String, String> entry : releaseItems.entrySet()) {
      String key = entry.getKey();
      if (newItemMap.get(key) == null) {
        ItemBO deletedItem = new ItemBO();

        deletedItem.setDeleted(true);
        ItemDTO deletedItemDto = deletedItemDTOs.computeIfAbsent(key, k -> new ItemDTO());
        deletedItemDto.setKey(key);
        String oldValue = entry.getValue();
        deletedItem.setItem(deletedItemDto);

        deletedItemDto.setValue(oldValue);
        deletedItem.setModified(true);
        deletedItem.setOldValue(oldValue);
        deletedItem.setNewValue("");
        deletedItems.add(deletedItem);
      }
    }
    return deletedItems;
  }

  /**
   * itemDTO转ItemBO
   *
   * @param itemDTO      配置项信息
   * @param releaseItems 发布配置项Map
   * @return 配置项业务对象
   */
  private ItemBO transformItem2BO(ItemDTO itemDTO, Map<String, String> releaseItems) {
    String key = itemDTO.getKey();
    ItemBO itemBO = new ItemBO();
    itemBO.setItem(itemDTO);
    String newValue = itemDTO.getValue();
    String oldValue = releaseItems.get(key);
    // 新配置项或已修改
    if (StringUtils.isNotBlank(key) && (!newValue.equals(oldValue))) {
      itemBO.setModified(true);
      itemBO.setOldValue(oldValue == null ? "" : oldValue);
      itemBO.setNewValue(newValue);
    }
    return itemBO;
  }

  /**
   * 将名称空间角色分配给操作者（ 默认将修改、发布名称空间角色分配给名称空间创建者）
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param operator      操作人
   */
  public void assignNamespaceRoleToOperator(String appId, String namespaceName, String operator) {
    // 分配用户给名称空间修改的角色
    rolePermissionService.assignRoleToUsers(
        RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.MODIFY_NAMESPACE),
        Sets.newHashSet(operator), operator);
    // 分配用户给名称空间发布的角色
    rolePermissionService.assignRoleToUsers(
        RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.RELEASE_NAMESPACE),
        Sets.newHashSet(operator), operator);
  }
}
