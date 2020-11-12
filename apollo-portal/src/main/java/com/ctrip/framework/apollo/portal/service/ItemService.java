package com.ctrip.framework.apollo.portal.service;


import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI.ItemAPI;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI.NamespaceAPI;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI.ReleaseAPI;
import com.ctrip.framework.apollo.portal.component.txtresolver.ConfigTextResolver;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.entity.vo.ItemDiffs;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceIdentifier;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

/**
 * 配置项 Service层
 */
@Service
public class ItemService {

  private static final Gson GSON = new Gson();

  private final UserInfoHolder userInfoHolder;
  private final AdminServiceAPI.NamespaceAPI namespaceAPI;
  private final AdminServiceAPI.ItemAPI itemAPI;
  private final AdminServiceAPI.ReleaseAPI releaseAPI;
  private final ConfigTextResolver fileTextResolver;
  private final ConfigTextResolver propertyResolver;

  public ItemService(
      final UserInfoHolder userInfoHolder,
      final NamespaceAPI namespaceAPI,
      final ItemAPI itemAPI,
      final ReleaseAPI releaseAPI,
      final @Qualifier("fileTextResolver") ConfigTextResolver fileTextResolver,
      final @Qualifier("propertyResolver") ConfigTextResolver propertyResolver) {
    this.userInfoHolder = userInfoHolder;
    this.namespaceAPI = namespaceAPI;
    this.itemAPI = itemAPI;
    this.releaseAPI = releaseAPI;
    this.fileTextResolver = fileTextResolver;
    this.propertyResolver = propertyResolver;
  }


  /**
   * 解析配置文本，并批量更新名称空间的配置项集
   *
   * @param model 名称空间文本 Model
   */
  public void updateConfigItemByText(NamespaceTextModel model) {
    String appId = model.getAppId();
    Env env = model.getEnv();
    String clusterName = model.getClusterName();
    String namespaceName = model.getNamespaceName();

    NamespaceDTO namespace = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    if (namespace == null) {
      throw new BadRequestException(
          "namespace:" + namespaceName + " not exist in env:" + env + ", cluster:" + clusterName);
    }
    long namespaceId = namespace.getId();

    String configText = model.getConfigText();
    // 获得对应格式的 ConfigTextResolver 对象
    ConfigTextResolver resolver = model.getFormat() == ConfigFileFormat.Properties ?
        propertyResolver : fileTextResolver;
    // 解析成 ItemChangeSets
    ItemChangeSets changeSets = resolver.resolve(namespaceId, configText,
        itemAPI.findItems(appId, env, clusterName, namespaceName));
    if (changeSets.isEmpty()) {
      return;
    }

    // 设置修改人为当前管理员
    changeSets.setDataChangeLastModifiedBy(userInfoHolder.getUser().getUserId());
    // 调用 Admin Service API ，批量更新配置项
    updateItems(appId, env, clusterName, namespaceName, changeSets);

    Tracer.logEvent(TracerEventType.MODIFY_NAMESPACE_BY_TEXT,
        String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
    Tracer.logEvent(TracerEventType.MODIFY_NAMESPACE,
        String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
  }

  /**
   * 更新配置项
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param changeSets    配置项变更列表
   */
  public void updateItems(String appId, Env env, String clusterName, String namespaceName,
      ItemChangeSets changeSets) {
    itemAPI.updateItemsByChangeSet(appId, env, clusterName, namespaceName, changeSets);
  }

  /**
   * 添加 Item
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名字
   * @param namespaceName 名称空间名称
   * @param item          配置项对象
   * @return 保存成功的配置项对象
   */
  public ItemDTO createItem(String appId, Env env, String clusterName, String namespaceName,
      ItemDTO item) {
    // 校验 NamespaceDTO 是否存在。若不存在，抛出 BadRequestException 异常
    NamespaceDTO namespace = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    if (namespace == null) {
      throw new BadRequestException(
          "namespace:" + namespaceName + " not exist in env:" + env + ", cluster:" + clusterName);
    }
    // 设置 ItemDTO 的 `namespaceId`
    item.setNamespaceId(namespace.getId());
    // 保存 Item 到 Admin Service
    ItemDTO itemDTO = itemAPI.createItem(appId, env, clusterName, namespaceName, item);
    Tracer.logEvent(TracerEventType.MODIFY_NAMESPACE,
        String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
    return itemDTO;
  }

  /**
   * 更新配置项
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称空间
   * @param namespaceName 名称空间名称
   * @param item          配置项信息
   */
  public void updateItem(String appId, Env env, String clusterName, String namespaceName,
      ItemDTO item) {
    itemAPI.updateItem(appId, env, clusterName, namespaceName, item.getId(), item);
  }

  /**
   * 删除配置项
   *
   * @param env    环境
   * @param itemId 配置项id
   * @param userId 用户id
   */
  public void deleteItem(Env env, long itemId, String userId) {
    itemAPI.deleteItem(env, itemId, userId);
  }

  /**
   * 查询指定名称空间的属性的配置项列表信息（以行号升序）
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 指定名称空间的属性的配置项列表信息（以行号升序）
   */
  public List<ItemDTO> findItems(String appId, Env env, String clusterName, String namespaceName) {
    return itemAPI.findItems(appId, env, clusterName, namespaceName);
  }

  /**
   * 查询已经被删除的配置项信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 已经被删除的配置项信息
   */
  public List<ItemDTO> findDeletedItems(String appId, Env env, String clusterName,
      String namespaceName) {
    return itemAPI.findDeletedItems(appId, env, clusterName, namespaceName);
  }

  /**
   * 找到定Key的配置项信息
   *
   * @param env           环境
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param key           配置项Key
   * @return 指定Key的配置项信息
   */
  public ItemDTO loadItem(Env env, String appId, String clusterName, String namespaceName,
      String key) {
    return itemAPI.loadItem(env, appId, clusterName, namespaceName, key);
  }

  /**
   * 通过配置项id找到配置项信息
   *
   * @param env    环境
   * @param itemId 配置项id
   * @return 指定配置项id的配置项信息
   */
  public ItemDTO loadItemById(Env env, long itemId) {
    ItemDTO item = itemAPI.loadItemById(env, itemId);
    if (item == null) {
      throw new BadRequestException("item not found for itemId " + itemId);
    }
    return item;
  }

  /**
   * 同步配置项
   *
   * @param comparedNamespaces 比较的名称空间信息列表
   * @param sourceItems        源配置项
   */
  public void syncItems(List<NamespaceIdentifier> comparedNamespaces, List<ItemDTO> sourceItems) {
    List<ItemDiffs> itemDiffs = compare(comparedNamespaces, sourceItems);
    for (ItemDiffs itemDiff : itemDiffs) {
      NamespaceIdentifier namespaceIdentifier = itemDiff.getNamespace();
      ItemChangeSets changeSets = itemDiff.getDiffs();
      changeSets.setDataChangeLastModifiedBy(userInfoHolder.getUser().getUserId());

      String appId = namespaceIdentifier.getAppId();
      Env env = namespaceIdentifier.getEnv();
      String clusterName = namespaceIdentifier.getClusterName();
      String namespaceName = namespaceIdentifier.getNamespaceName();

      //  更新配置项
      itemAPI.updateItemsByChangeSet(appId, env, clusterName, namespaceName, changeSets);

      Tracer.logEvent(TracerEventType.SYNC_NAMESPACE,
          String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
    }
  }

  /**
   * 撤消配置项
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   */
  public void revokeItem(String appId, Env env, String clusterName, String namespaceName) {

    // 关联的名称空间中的公有名称空间
    NamespaceDTO namespace = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    if (namespace == null) {
      throw new BadRequestException(String.format("namespace:%s not exist in env:%s, cluster:%s",
          namespaceName, env, clusterName));
    }
    long namespaceId = namespace.getId();

    Map<String, String> releaseItemDTOs = new HashMap<>();
    // 名称空间最新的发布信息
    ReleaseDTO latestRelease = releaseAPI.loadLatestRelease(appId, env, clusterName, namespaceName);
    if (latestRelease != null) {
      releaseItemDTOs = GSON.fromJson(latestRelease.getConfigurations(), GsonType.CONFIG);
    }
    // 指定名称空间的属性的配置项列表信息
    List<ItemDTO> baseItems = itemAPI.findItems(appId, env, clusterName, namespaceName);
    Map<String, ItemDTO> oldKeyMapItem = BeanUtils.mapByKey("key", baseItems);
    Map<String, ItemDTO> deletedItemDTOs = new HashMap<>();

    // 已删除备注项
    findDeletedItems(appId, env, clusterName, namespaceName).forEach(item -> {
      deletedItemDTOs.put(item.getKey(), item);
    });

    ItemChangeSets changeSets = new ItemChangeSets();
    AtomicInteger lineNum = new AtomicInteger(1);
    releaseItemDTOs.forEach((key, value) -> {
      ItemDTO oldItem = oldKeyMapItem.get(key);
      if (oldItem == null) {
        ItemDTO deletedItemDto = deletedItemDTOs.computeIfAbsent(key, k -> new ItemDTO());
        // 添加创建的配置项
        changeSets.addCreateItem(buildNormalItem(0L, namespaceId, key, value,
            deletedItemDto.getComment(), lineNum.get()));
      } else if (!oldItem.getValue().equals(value) || lineNum.get() != oldItem.getLineNum()) {
        // 添加更新的配置项
        changeSets.addUpdateItem(buildNormalItem(oldItem.getId(), namespaceId, key,
            value, oldItem.getComment(), lineNum.get()));
      }
      oldKeyMapItem.remove(key);
      lineNum.set(lineNum.get() + 1);
    });
    // 添加删除的配置项
    oldKeyMapItem.forEach((key, value) -> changeSets.addDeleteItem(oldKeyMapItem.get(key)));
    changeSets.setDataChangeLastModifiedBy(userInfoHolder.getUser().getUserId());

    // 更新配置项
    updateItems(appId, env, clusterName, namespaceName, changeSets);

    Tracer.logEvent(TracerEventType.MODIFY_NAMESPACE_BY_TEXT,
        String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
    Tracer.logEvent(TracerEventType.MODIFY_NAMESPACE,
        String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
  }

  /**
   * 比较
   *
   * @param comparedNamespaces 待比较的名称空间列表
   * @param sourceItems        源配置项列表
   * @return 比较后配置项差异列表
   */
  public List<ItemDiffs> compare(List<NamespaceIdentifier> comparedNamespaces,
      List<ItemDTO> sourceItems) {

    List<ItemDiffs> result = new LinkedList<>();
    // 遍历
    for (NamespaceIdentifier namespace : comparedNamespaces) {

      // 配置项差异
      ItemDiffs itemDiffs = new ItemDiffs(namespace);
      try {
        itemDiffs.setDiffs(parseChangeSets(namespace, sourceItems));
      } catch (BadRequestException e) {
        itemDiffs.setDiffs(new ItemChangeSets());
        itemDiffs.setExtInfo("该集群下没有名为 " + namespace.getNamespaceName() + " 的namespace");
      }
      result.add(itemDiffs);
    }

    return result;
  }

  /**
   * 获取名称空间id
   *
   * @param namespaceIdentifier 名称空间标识
   * @return 名称空间id
   */
  private long getNamespaceId(NamespaceIdentifier namespaceIdentifier) {
    String appId = namespaceIdentifier.getAppId();
    String clusterName = namespaceIdentifier.getClusterName();
    String namespaceName = namespaceIdentifier.getNamespaceName();
    Env env = namespaceIdentifier.getEnv();
    NamespaceDTO namespaceDTO = null;
    try {
      // 关联的名称空间中公有名称空间
      namespaceDTO = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new BadRequestException(String.format(
            "namespace not exist. appId:%s, env:%s, clusterName:%s, namespaceName:%s", appId, env,
            clusterName, namespaceName));
      }
      throw e;
    }
    return namespaceDTO.getId();
  }

  /**
   * 解析变更配置项列表
   *
   * @param namespace   名称空间
   * @param sourceItems 源配置项
   * @return 配置项变更列表
   */
  private ItemChangeSets parseChangeSets(NamespaceIdentifier namespace, List<ItemDTO> sourceItems) {
    ItemChangeSets changeSets = new ItemChangeSets();
    // 指定名称空间的属性的配置项列表信息
    List<ItemDTO> targetItems = itemAPI.findItems(namespace.getAppId(), namespace.getEnv(),
        namespace.getClusterName(), namespace.getNamespaceName());

    long namespaceId = getNamespaceId(namespace);

    if (CollectionUtils.isEmpty(targetItems)) {
      // 添加所有源配置项，新增的配置项
      int lineNum = 1;
      for (ItemDTO sourceItem : sourceItems) {
        changeSets.addCreateItem(buildItem(namespaceId, lineNum++, sourceItem));
      }
    } else {

      Map<String, ItemDTO> targetItemMap = BeanUtils.mapByKey("key", targetItems);
      String key, sourceValue, sourceComment;
      ItemDTO targetItem = null;
      int maxLineNum = targetItems.size();//append to last
      for (ItemDTO sourceItem : sourceItems) {
        key = sourceItem.getKey();
        sourceValue = sourceItem.getValue();
        sourceComment = sourceItem.getComment();
        targetItem = targetItemMap.get(key);

        // 新增的配置项
        if (targetItem == null) {//added items

          changeSets.addCreateItem(buildItem(namespaceId, ++maxLineNum, sourceItem));

        } else if (isModified(sourceValue, targetItem.getValue(), sourceComment,
            targetItem.getComment())) {
          // 变更的配置项
          targetItem.setValue(sourceValue);
          targetItem.setComment(sourceComment);
          changeSets.addUpdateItem(targetItem);
        }
      }
    }

    return changeSets;
  }

  /**
   * 构建配置项
   *
   * @param namespaceId 名称空间id
   * @param lineNum     行号
   * @param sourceItem  源配置项
   * @return 构建的配置项信息
   */
  private ItemDTO buildItem(long namespaceId, int lineNum, ItemDTO sourceItem) {
    ItemDTO createdItem = new ItemDTO();
    BeanUtils.copyEntityProperties(sourceItem, createdItem);
    createdItem.setLineNum(lineNum);
    createdItem.setNamespaceId(namespaceId);
    return createdItem;
  }

  /**
   * 构建通常的配置项
   *
   * @param id          配置项id
   * @param namespaceId 名称空间id
   * @param key         配置项key
   * @param value       配置项value
   * @param comment     备注
   * @param lineNum     行号
   * @return 构建的配置项信息
   */
  private ItemDTO buildNormalItem(Long id, Long namespaceId, String key, String value,
      String comment, int lineNum) {
    ItemDTO item = new ItemDTO(key, value, comment, lineNum);
    item.setId(id);
    item.setNamespaceId(namespaceId);
    return item;
  }

  /**
   * 是否被修改
   *
   * @param sourceValue   原值
   * @param targetValue   目标值
   * @param sourceComment 源备注
   * @param targetComment 目标备注
   * @return true, 被修改，否则，false
   */
  private boolean isModified(String sourceValue, String targetValue, String sourceComment,
      String targetComment) {

    if (!sourceValue.equals(targetValue)) {
      return true;
    }

    if (sourceComment == null) {
      return StringUtils.isNotBlank(targetComment);
    }
    if (targetComment != null) {
      return !sourceComment.equals(targetComment);
    }
    return false;
  }
}
