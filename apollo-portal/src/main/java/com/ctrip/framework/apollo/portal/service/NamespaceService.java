package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.utils.StringUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
public class NamespaceService {

  private Logger logger = LoggerFactory.getLogger(NamespaceService.class);
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
    @Autowired
    private NamespaceBoAsyncService namespaceBoAsyncService;

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


  public NamespaceDTO createNamespace(Env env, NamespaceDTO namespace) {
    if (StringUtils.isEmpty(namespace.getDataChangeCreatedBy())) {
      namespace.setDataChangeCreatedBy(userInfoHolder.getUser().getUserId());
    }
    namespace.setDataChangeLastModifiedBy(userInfoHolder.getUser().getUserId());
    NamespaceDTO createdNamespace = namespaceAPI.createNamespace(env, namespace);

    Tracer.logEvent(TracerEventType.CREATE_NAMESPACE,
        String.format("%s+%s+%s+%s", namespace.getAppId(), env, namespace.getClusterName(),
            namespace.getNamespaceName()));
    return createdNamespace;
  }


  @Transactional
  public void deleteNamespace(String appId, Env env, String clusterName, String namespaceName) {

    AppNamespace appNamespace = appNamespaceService.findByAppIdAndName(appId, namespaceName);

    //1. check parent namespace has not instances
    if (namespaceHasInstances(appId, env, clusterName, namespaceName)) {
      throw new BadRequestException(
          "Can not delete namespace because namespace has active instances");
    }

    //2. check child namespace has not instances
    NamespaceDTO childNamespace = branchService
        .findBranchBaseInfo(appId, env, clusterName, namespaceName);
    if (childNamespace != null &&
        namespaceHasInstances(appId, env, childNamespace.getClusterName(), namespaceName)) {
      throw new BadRequestException(
          "Can not delete namespace because namespace's branch has active instances");
    }

    //3. check public namespace has not associated namespace
    if (appNamespace != null && appNamespace.isPublic() && publicAppNamespaceHasAssociatedNamespace(
        namespaceName, env)) {
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
      throw new BadRequestException(String.format("Namespace: %s not exist.", namespaceName));
    }
    return namespace;
  }

    /**
     * load cluster all namespace info with items
     */
    public List<NamespaceBO> findNamespaceBOs(String appId, Env env, String clusterName) {
        List<NamespaceDTO> namespaces = namespaceAPI.findNamespaceByCluster(appId, env, clusterName);
        if (CollectionUtils.isEmpty(namespaces)) {
            throw new BadRequestException("namespaces not exist");
        }
        //In order to solve the problem of slow processing of multiple namespace
       // with a large number of items
        return namespaces.parallelStream()
                .map(
                        namespace ->
                        {
                            try {
                                return transformNamespace2BO(env, namespace);
                            } catch (Exception e) {
                                logger.error("parse namespace error. app id:{}, env:{}, clusterName:{}, namespace:{}",
                                        appId, env, clusterName, namespace.getNamespaceName(), e);
                                throw e;
                            }
                        })
                .collect(Collectors.toList());
    }

  public List<NamespaceDTO> findNamespaces(String appId, Env env, String clusterName) {
    return namespaceAPI.findNamespaceByCluster(appId, env, clusterName);
  }

  public List<NamespaceDTO> getPublicAppNamespaceAllNamespaces(Env env, String publicNamespaceName,
      int page,
      int size) {
    return namespaceAPI.getPublicAppNamespaceAllNamespaces(env, publicNamespaceName, page, size);
  }

  public NamespaceBO loadNamespaceBO(String appId, Env env, String clusterName,
      String namespaceName) {
    NamespaceDTO namespace = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    if (namespace == null) {
      throw new BadRequestException("namespaces not exist");
    }
    return transformNamespace2BO(env, namespace);
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
    NamespaceDTO namespace =
        namespaceAPI
            .findPublicNamespaceForAssociatedNamespace(env, appId, clusterName, namespaceName);

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

    private NamespaceBO transformNamespace2BO(Env env, NamespaceDTO namespace) {
        NamespaceBO namespaceBO = new NamespaceBO();
        try {
            namespaceBO.setBaseInfo(namespace);
            String appId = namespace.getAppId();
            String clusterName = namespace.getClusterName();
            String namespaceName = namespace.getNamespaceName();
            fillAppNamespaceProperties(namespaceBO);
            Map<String, String> releaseItems = new HashMap<>();
            List<ItemDTO> items = new ArrayList<>();
            //Get the latest release asynchronously
            Future<Map<String, String>> latestReleaseAsync = namespaceBoAsyncService
                    .getLatestReleaseAsync(appId, env, clusterName, namespaceName);
            //Get the item list asynchronously
            Future<List<ItemDTO>> itemsAsync = namespaceBoAsyncService.getItemsAsync(appId, env, clusterName, namespaceName);
            //This method takes the most time, so make it in the main thread
            List<ItemDTO> deletedItems = itemService.findDeletedItems(appId, env, clusterName, namespaceName);
            Map<String, ItemDTO> deletedItemsMap = new HashMap<>();
            //Convert list to map
            if (!CollectionUtils.isEmpty(deletedItems)) {
                Map<String, ItemDTO> collect = deletedItems
                        .parallelStream()
                        .collect(toMap(ItemDTO::getKey, itemDTO -> itemDTO));
                deletedItemsMap.putAll(collect);
            }
            List<ItemDTO> list = itemsAsync.get();
            if (!CollectionUtils.isEmpty(list)) {
                items.addAll(list);
            }
            Map<String, String> releaseAsync = latestReleaseAsync.get();
            if (Objects.nonNull(releaseAsync)) {
                releaseItems.putAll(releaseAsync);
            }
            // Convert itemDTO' s list to ItemBO 's list
            List<ItemBO> itemBOs = items
                    .parallelStream()
                    .map(itemDTO -> transformItem2BO(itemDTO, releaseItems))
                    .collect(Collectors.toList());
            // Count the number of modified items
            long modifiedItemCnt = itemBOs
                    .parallelStream()
                    .filter(ItemBO::isModified)
                    .count();
            // Parse deleted items
            List<ItemBO> deletedItemBos = parseDeletedItems(items, releaseItems, deletedItemsMap);
            itemBOs.addAll(deletedItemBos);
            namespaceBO.setItems(itemBOs);
            // The number of items to be modified
            namespaceBO.setItemModifiedCnt((int) modifiedItemCnt + deletedItemBos.size());
        } catch (InterruptedException | ExecutionException e) {
            logger.error("transformNamespace2BO error namespace:{}, env:{},err:{}",
                    namespace, env, e);
        }
        return namespaceBO;
    }

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
      //dirty data
      logger.warn("Dirty data, cannot find appNamespace by namespaceName [{}], appId = {}, cluster = {}, set it format to {}, make public", namespaceName, appId, clusterName, ConfigFileFormat.Properties.getValue());
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

  private List<ItemBO> parseDeletedItems(List<ItemDTO> newItems, Map<String, String> releaseItems, Map<String, ItemDTO> deletedItemDTOs) {
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

  private ItemBO transformItem2BO(ItemDTO itemDTO, Map<String, String> releaseItems) {
    String key = itemDTO.getKey();
    ItemBO itemBO = new ItemBO();
    itemBO.setItem(itemDTO);
    String newValue = itemDTO.getValue();
    String oldValue = releaseItems.get(key);
    //new item or modified
    if (!StringUtils.isEmpty(key) && (!newValue.equals(oldValue))) {
      itemBO.setModified(true);
      itemBO.setOldValue(oldValue == null ? "" : oldValue);
      itemBO.setNewValue(newValue);
    }
    return itemBO;
  }

  public void assignNamespaceRoleToOperator(String appId, String namespaceName, String operator) {
    //default assign modify、release namespace role to namespace creator

    rolePermissionService
        .assignRoleToUsers(
            RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.MODIFY_NAMESPACE),
            Sets.newHashSet(operator), operator);
    rolePermissionService
        .assignRoleToUsers(
            RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.RELEASE_NAMESPACE),
            Sets.newHashSet(operator), operator);
  }
}
