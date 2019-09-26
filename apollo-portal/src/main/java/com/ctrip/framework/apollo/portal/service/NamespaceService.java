package com.ctrip.framework.apollo.portal.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.entity.bo.ItemBO;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

@Service
public class NamespaceService {

  private Logger logger = LoggerFactory.getLogger(NamespaceService.class);
  private Gson gson = new Gson();

  private final PortalConfig portalConfig;
  private final PortalSettings portalSettings;
  private final UserInfoHolder userInfoHolder;
  private final AdminServiceAPI.NamespaceAPI namespaceAPI;
  private final ClusterService clusterService;
  private final ItemService itemService;
  private final ReleaseService releaseService;
  private final AppNamespaceService appNamespaceService;
  private final InstanceService instanceService;
  private final NamespaceBranchService branchService;
  private final RolePermissionService rolePermissionService;

  public NamespaceService(final PortalConfig portalConfig, final PortalSettings portalSettings,
      final UserInfoHolder userInfoHolder, final AdminServiceAPI.NamespaceAPI namespaceAPI,
      final ClusterService clusterService, final ItemService itemService,
      final ReleaseService releaseService, final AppNamespaceService appNamespaceService,
      final InstanceService instanceService, final @Lazy NamespaceBranchService branchService,
      final RolePermissionService rolePermissionService) {
    this.portalConfig = portalConfig;
    this.portalSettings = portalSettings;
    this.userInfoHolder = userInfoHolder;
    this.namespaceAPI = namespaceAPI;
    this.clusterService = clusterService;
    this.itemService = itemService;
    this.releaseService = releaseService;
    this.appNamespaceService = appNamespaceService;
    this.instanceService = instanceService;
    this.branchService = branchService;
    this.rolePermissionService = rolePermissionService;
  }


  public NamespaceDTO createNamespace(Env env, NamespaceDTO namespace) {
    if (StringUtils.isEmpty(namespace.getDataChangeCreatedBy())) {
      namespace.setDataChangeCreatedBy(userInfoHolder.getUser().getUserId());
    }
    namespace.setDataChangeLastModifiedBy(userInfoHolder.getUser().getUserId());
    NamespaceDTO createdNamespace = namespaceAPI.createNamespace(env, namespace);

    Tracer.logEvent(TracerEventType.CREATE_NAMESPACE, String.format("%s+%s+%s+%s",
        namespace.getAppId(), env, namespace.getClusterName(), namespace.getNamespaceName()));
    return createdNamespace;
  }


  @Transactional
  public void deleteNamespace(String appId, Env env, String clusterName, String namespaceName) {

    AppNamespace appNamespace = appNamespaceService.findByAppIdAndName(appId, namespaceName);

    // 1. check parent namespace has not instances
    if (namespaceHasInstances(appId, env, clusterName, namespaceName)) {
      throw new BadRequestException(
          "Can not delete namespace because namespace has active instances");
    }

    // 2. check child namespace has not instances
    NamespaceDTO childNamespace =
        branchService.findBranchBaseInfo(appId, env, clusterName, namespaceName);
    if (childNamespace != null
        && namespaceHasInstances(appId, env, childNamespace.getClusterName(), namespaceName)) {
      throw new BadRequestException(
          "Can not delete namespace because namespace's branch has active instances");
    }

    // 3. check public namespace has not associated namespace
    if (appNamespace != null && appNamespace.isPublic()
        && publicAppNamespaceHasAssociatedNamespace(namespaceName, env)) {
      throw new BadRequestException(
          "Can not delete public namespace which has associated namespaces");
    }

    String operator = userInfoHolder.getUser().getUserId();

    namespaceAPI.deleteNamespace(env, appId, clusterName, namespaceName, operator);
  }

  public NamespaceDTO loadNamespaceBaseInfo(String appId, Env env, String clusterName,
      String namespaceName) {
    NamespaceDTO namespace = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    if (namespace == null) {
      throw new BadRequestException("namespaces not exist");
    }
    return namespace;
  }

  /**
   * load cluster all namespace info with items
   */
  public List<NamespaceBO> findNamespaceBOs(String appId, Env env, String clusterName) {
    return findNamespaceBOs(appId, env, clusterName, false);
  }

  public List<NamespaceBO> findNamespaceBOs(String appId, Env env, String clusterName,
      boolean withFullItems) {

    List<NamespaceDTO> namespaces = namespaceAPI.findNamespaceByCluster(appId, env, clusterName);
    if (namespaces == null || namespaces.size() == 0) {
      throw new BadRequestException("namespaces not exist");
    }

    List<NamespaceBO> namespaceBOs = new LinkedList<>();
    for (NamespaceDTO namespace : namespaces) {

      NamespaceBO namespaceBO;
      try {
        if (withFullItems) {
          namespaceBO = transformNamespace2BOwithFullItems(env, namespace);
        } else {
          namespaceBO = transformNamespace2BO(env, namespace);
        }
        namespaceBOs.add(namespaceBO);
      } catch (Exception e) {
        logger.error("parse namespace error. app id:{}, env:{}, clusterName:{}, namespace:{}",
            appId, env, clusterName, namespace.getNamespaceName(), e);
        throw e;
      }
    }

    return namespaceBOs;
  }

  public void createMissingNamespaces(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName) {
    createMissingNamespaces(appId, env, clusterName, false);
  }

  public void onlyCreateMissingAppNamespace(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName) {
    createMissingNamespaces(appId, env, clusterName, true);
  }

  private void createMissingNamespaces(String appId, String env, String clusterName,
      boolean onlyCreateAppNamespace) {
    Set<String> missingNamespaces = findMissingNamespaceNames(appId, env, clusterName);

    for (String missingNamespace : missingNamespaces) {
      AppNamespace appNamespace = appNamespaceService.findByAppIdAndName(appId, missingNamespace);
      if (appNamespace == null) {
        throw new BadRequestException(String.format(
            "AppNamespace not exists. AppId = %s, NamespaceName = %s", appId, missingNamespace));
      }

      AppNamespaceDTO appNamespaceDTO = BeanUtils.transform(AppNamespaceDTO.class, appNamespace);
      if (onlyCreateAppNamespace) {
        namespaceAPI.onlyCreateMissingAppNamespace(Env.fromString(env), appNamespaceDTO);
      } else {
        namespaceAPI.createMissingAppNamespace(Env.fromString(env), appNamespaceDTO);
      }
    }
  }

  public Set<String> findMissingNamespaceNames(String appId, String env, String clusterName) {
    List<AppNamespaceDTO> configDbAppNamespaces =
        namespaceAPI.getAppNamespaces(appId, Env.fromString(env));
    List<NamespaceDTO> configDbNamespaces = findNamespaces(appId, Env.fromString(env), clusterName);
    List<AppNamespace> portalDbAppNamespaces = appNamespaceService.findByAppId(appId);

    Set<String> configDbAppNamespaceNames =
        configDbAppNamespaces.stream().map(AppNamespaceDTO::getName).collect(Collectors.toSet());
    Set<String> configDbNamespaceNames =
        configDbNamespaces.stream().map(NamespaceDTO::getNamespaceName).collect(Collectors.toSet());

    Set<String> portalDbAllAppNamespaceNames = Sets.newHashSet();
    Set<String> portalDbPrivateAppNamespaceNames = Sets.newHashSet();

    for (AppNamespace appNamespace : portalDbAppNamespaces) {
      portalDbAllAppNamespaceNames.add(appNamespace.getName());
      if (!appNamespace.isPublic()) {
        portalDbPrivateAppNamespaceNames.add(appNamespace.getName());
      }
    }

    // AppNamespaces should be the same
    Set<String> missingAppNamespaceNames =
        Sets.difference(portalDbAllAppNamespaceNames, configDbAppNamespaceNames);
    // Private namespaces should all exist
    Set<String> missingNamespaceNames =
        Sets.difference(portalDbPrivateAppNamespaceNames, configDbNamespaceNames);

    return Sets.union(missingAppNamespaceNames, missingNamespaceNames);
  }

  public List<NamespaceDTO> findNamespaces(String appId, Env env, String clusterName) {
    return namespaceAPI.findNamespaceByCluster(appId, env, clusterName);
  }

  public List<NamespaceDTO> getPublicAppNamespaceAllNamespaces(Env env, String publicNamespaceName,
      int page, int size) {
    return namespaceAPI.getPublicAppNamespaceAllNamespaces(env, publicNamespaceName, page, size);
  }

  public NamespaceBO loadNamespaceBO(String appId, Env env, String clusterName,
      String namespaceName) {
    return loadNamespaceBO(appId, env, clusterName, namespaceName, false);
  }

  public NamespaceBO loadNamespaceBO(String appId, Env env, String clusterName,
      String namespaceName, boolean withFullItems) {
    NamespaceDTO namespace = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    if (namespace == null) {
      throw new BadRequestException("namespaces not exist");
    }

    if (withFullItems) {
      return transformNamespace2BOwithFullItems(env, namespace);
    } else {
      return transformNamespace2BO(env, namespace);
    }
  }

  public boolean namespaceHasInstances(String appId, Env env, String clusterName,
      String namespaceName) {
    return instanceService.getInstanceCountByNamepsace(appId, env, clusterName, namespaceName) > 0;
  }

  public boolean publicAppNamespaceHasAssociatedNamespace(String publicNamespaceName, Env env) {
    return namespaceAPI.countPublicAppNamespaceAssociatedNamespaces(env, publicNamespaceName) > 0;
  }

  public NamespaceBO findPublicNamespaceForAssociatedNamespace(Env env, String appId,
      String clusterName, String namespaceName) {
    NamespaceDTO namespace = namespaceAPI.findPublicNamespaceForAssociatedNamespace(env, appId,
        clusterName, namespaceName);

    return transformNamespace2BO(env, namespace);
  }

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

  public NamespaceBO transformNamespace2BO(Env env, NamespaceDTO namespace) {
    NamespaceBO namespaceBO = new NamespaceBO();
    namespaceBO.setBaseInfo(namespace);

    String appId = namespace.getAppId();
    String clusterName = namespace.getClusterName();
    String namespaceName = namespace.getNamespaceName();

    fillAppNamespaceProperties(namespaceBO);

    List<ItemBO> itemBOs = new LinkedList<>();
    namespaceBO.setItems(itemBOs);

    // latest Release
    ReleaseDTO latestRelease;
    Map<String, String> releaseItems = new HashMap<>();
    latestRelease = releaseService.loadLatestRelease(appId, env, clusterName, namespaceName);
    if (latestRelease != null) {
      releaseItems = gson.fromJson(latestRelease.getConfigurations(), GsonType.CONFIG);
    }

    // not Release config items
    List<ItemDTO> items = itemService.findItems(appId, env, clusterName, namespaceName);
    int modifiedItemCnt = 0;
    for (ItemDTO itemDTO : items) {

      ItemBO itemBO = transformItem2BO(itemDTO, releaseItems);

      if (itemBO.isModified()) {
        modifiedItemCnt++;
      }

      itemBOs.add(itemBO);
    }

    // deleted items
    List<ItemBO> deletedItems = parseDeletedItems(items, releaseItems);
    itemBOs.addAll(deletedItems);
    modifiedItemCnt += deletedItems.size();

    namespaceBO.setItemModifiedCnt(modifiedItemCnt);

    return namespaceBO;
  }

  private NamespaceBO transformNamespace2BOwithFullItems(Env env, NamespaceDTO namespace) {
    NamespaceBO namespaceBO = new NamespaceBO();
    namespaceBO.setBaseInfo(namespace);

    String appId = namespace.getAppId();
    String clusterName = namespace.getClusterName();
    String namespaceName = namespace.getNamespaceName();

    fillAppNamespaceProperties(namespaceBO);

    long parentClusterId;
    String parentClusterName = null;
    ClusterDTO curCluster = clusterService.loadCluster(appId, env, clusterName);
    if (curCluster.getParentClusterId() != 0) {
      parentClusterId = curCluster.getParentClusterId();
      ClusterDTO parentCluster = clusterService.loadCluster(env, parentClusterId);
      if (parentCluster != null)
        parentClusterName = parentCluster.getName();
    }

    List<ItemBO> itemBOs = new LinkedList<>();
    namespaceBO.setItems(itemBOs);

    // latest Release
    ReleaseDTO latestRelease;
    Map<String, String> releaseItems = new HashMap<>();
    if (parentClusterName != null) {
      // load latest release items in parent cluster namespace
      latestRelease = releaseService.loadLatestRelease(namespaceBO.getParentAppId(), env,
          parentClusterName, namespaceName);
      if (latestRelease != null) {
        releaseItems.putAll(gson.fromJson(latestRelease.getConfigurations(), GsonType.CONFIG));
      }
    }
    // load latest release items in custom cluster namespace
    latestRelease = releaseService.loadLatestRelease(appId, env, clusterName, namespaceName);
    if (latestRelease != null) {
      releaseItems.putAll(gson.fromJson(latestRelease.getConfigurations(), GsonType.CONFIG));
    }

    // not Release config items
    List<ItemDTO> items = Arrays.asList();
    if (parentClusterName != null) {
      // not Release config items in parent cluster namespace
      items = itemService.findItems(namespaceBO.getParentAppId(), env, parentClusterName,
          namespaceName);
    }
    if (CollectionUtils.isEmpty(items)) {
      // not Release config items in custom cluster namespace
      items = itemService.findItems(appId, env, clusterName, namespaceName);
    } else {
      // not Release config items in custom cluster namespace
      List<ItemDTO> customItems = itemService.findItems(appId, env, clusterName, namespaceName);
      for (int i = 0; i < customItems.size(); i++) {
        for (ItemDTO parentItemDTO : items) {
          // parent cluster namespacenot not Release config items
          // replace with custom cluster namespace not Release config items
          if (customItems.get(i).getKey().equals(parentItemDTO.getKey())) {
            items.set(i, customItems.get(i));
          }
          break;
        }
      }
    }

    int modifiedItemCnt = 0;
    for (ItemDTO itemDTO : items) {

      ItemBO itemBO = transformItem2BO(itemDTO, releaseItems);

      if (itemBO.isModified()) {
        modifiedItemCnt++;
      }

      itemBOs.add(itemBO);
    }

    // deleted items
    List<ItemBO> deletedItems = parseDeletedItems(items, releaseItems);
    itemBOs.addAll(deletedItems);
    modifiedItemCnt += deletedItems.size();

    namespaceBO.setItemModifiedCnt(modifiedItemCnt);

    return namespaceBO;
  }

  private void fillAppNamespaceProperties(NamespaceBO namespace) {

    NamespaceDTO namespaceDTO = namespace.getBaseInfo();
    // 先从当前appId下面找,包含私有的和公共的
    AppNamespace appNamespace = appNamespaceService.findByAppIdAndName(namespaceDTO.getAppId(),
        namespaceDTO.getNamespaceName());
    // 再从公共的app namespace里面找
    if (appNamespace == null) {
      appNamespace = appNamespaceService.findPublicAppNamespace(namespaceDTO.getNamespaceName());
    }

    String format;
    boolean isPublic;
    if (appNamespace == null) {
      // dirty data
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

  private List<ItemBO> parseDeletedItems(List<ItemDTO> newItems, Map<String, String> releaseItems) {
    Map<String, ItemDTO> newItemMap = BeanUtils.mapByKey("key", newItems);

    List<ItemBO> deletedItems = new LinkedList<>();
    for (Map.Entry<String, String> entry : releaseItems.entrySet()) {
      String key = entry.getKey();
      if (newItemMap.get(key) == null) {
        ItemBO deletedItem = new ItemBO();

        deletedItem.setDeleted(true);
        ItemDTO deletedItemDto = new ItemDTO();
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

  private ItemBO transformItem2BO(ItemDTO itemDTO, Map<String, String> releaseItems) {
    String key = itemDTO.getKey();
    ItemBO itemBO = new ItemBO();
    itemBO.setItem(itemDTO);
    String newValue = itemDTO.getValue();
    String oldValue = releaseItems.get(key);
    // new item or modified
    if (!StringUtils.isEmpty(key) && (oldValue == null || !newValue.equals(oldValue))) {
      itemBO.setModified(true);
      itemBO.setOldValue(oldValue == null ? "" : oldValue);
      itemBO.setNewValue(newValue);
    }
    return itemBO;
  }

  public void assignNamespaceRoleToOperator(String appId, String namespaceName, String operator) {
    // default assign modify、release namespace role to namespace creator

    rolePermissionService.assignRoleToUsers(
        RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.MODIFY_NAMESPACE),
        Sets.newHashSet(operator), operator);
    rolePermissionService.assignRoleToUsers(
        RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.RELEASE_NAMESPACE),
        Sets.newHashSet(operator), operator);
  }
}
