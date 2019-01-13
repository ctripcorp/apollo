package com.ctrip.framework.apollo.portal.service;


import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.txtresolver.ConfigTextResolver;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.entity.vo.ItemDiffs;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceIdentifier;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.tracer.Tracer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class ItemService {

  private final UserInfoHolder userInfoHolder;
  private final AdminServiceAPI.NamespaceAPI namespaceAPI;
  private final AdminServiceAPI.ItemAPI itemAPI;
  private final ConfigTextResolver fileTextResolver;
  private final ConfigTextResolver propertyResolver;

  public ItemService(
      final UserInfoHolder userInfoHolder,
      final AdminServiceAPI.NamespaceAPI namespaceAPI,
      final AdminServiceAPI.ItemAPI itemAPI,
      final @Qualifier("fileTextResolver") ConfigTextResolver fileTextResolver,
      final @Qualifier("propertyResolver") ConfigTextResolver propertyResolver) {
    this.userInfoHolder = userInfoHolder;
    this.namespaceAPI = namespaceAPI;
    this.itemAPI = itemAPI;
    this.fileTextResolver = fileTextResolver;
    this.propertyResolver = propertyResolver;
  }


  /**
   * parse config text and update config items
   *
   * @return parse result
   */
  public void updateConfigItemByText(NamespaceTextModel model) {
    String appId = model.getAppId();
    Env env = model.getEnv();
    String clusterName = model.getClusterName();
    String namespaceName = model.getNamespaceName();
    long namespaceId = model.getNamespaceId();
    String configText = model.getConfigText();

    ConfigTextResolver resolver =
        model.getFormat() == ConfigFileFormat.Properties ? propertyResolver : fileTextResolver;

    ItemChangeSets changeSets = resolver.resolve(namespaceId, configText,
        itemAPI.findItems(appId, env, clusterName, namespaceName));
    if (changeSets.isEmpty()) {
      return;
    }

    changeSets.setDataChangeLastModifiedBy(userInfoHolder.getUser().getUserId());
    updateItems(appId, env, clusterName, namespaceName, changeSets);

    Tracer.logEvent(TracerEventType.MODIFY_NAMESPACE_BY_TEXT,
        String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
    Tracer.logEvent(TracerEventType.MODIFY_NAMESPACE, String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
  }

  public void updateItems(String appId, Env env, String clusterName, String namespaceName, ItemChangeSets changeSets){
    itemAPI.updateItemsByChangeSet(appId, env, clusterName, namespaceName, changeSets);
  }


  public ItemDTO createItem(String appId, Env env, String clusterName, String namespaceName, ItemDTO item) {
    NamespaceDTO namespace = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    if (namespace == null) {
      throw new BadRequestException(
          "namespace:" + namespaceName + " not exist in env:" + env + ", cluster:" + clusterName);
    }
    item.setNamespaceId(namespace.getId());

    ItemDTO itemDTO = itemAPI.createItem(appId, env, clusterName, namespaceName, item);
    Tracer.logEvent(TracerEventType.MODIFY_NAMESPACE, String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
    return itemDTO;
  }

  public void updateItem(String appId, Env env, String clusterName, String namespaceName, ItemDTO item) {
    itemAPI.updateItem(appId, env, clusterName, namespaceName, item.getId(), item);
  }

  public void deleteItem(Env env, long itemId, String userId) {
    itemAPI.deleteItem(env, itemId, userId);
  }

  public List<ItemDTO> findItems(String appId, Env env, String clusterName, String namespaceName) {
    return itemAPI.findItems(appId, env, clusterName, namespaceName);
  }

  public ItemDTO loadItem(Env env, String appId, String clusterName, String namespaceName, String key) {
    return itemAPI.loadItem(env, appId, clusterName, namespaceName, key);
  }

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

      itemAPI.updateItemsByChangeSet(appId, env, clusterName, namespaceName, changeSets);

      Tracer.logEvent(TracerEventType.SYNC_NAMESPACE, String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
    }
  }

  public List<ItemDiffs> compare(List<NamespaceIdentifier> comparedNamespaces, List<ItemDTO> sourceItems) {

    List<ItemDiffs> result = new LinkedList<>();

    for (NamespaceIdentifier namespace : comparedNamespaces) {

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

  private long getNamespaceId(NamespaceIdentifier namespaceIdentifier) {
    String appId = namespaceIdentifier.getAppId();
    String clusterName = namespaceIdentifier.getClusterName();
    String namespaceName = namespaceIdentifier.getNamespaceName();
    Env env = namespaceIdentifier.getEnv();
    NamespaceDTO namespaceDTO = null;
    try {
      namespaceDTO = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new BadRequestException(String.format(
            "namespace not exist. appId:%s, env:%s, clusterName:%s, namespaceName:%s", appId, env, clusterName,
            namespaceName));
      }
    }
    return namespaceDTO.getId();
  }

  private ItemChangeSets parseChangeSets(NamespaceIdentifier namespace, List<ItemDTO> sourceItems) {
    ItemChangeSets changeSets = new ItemChangeSets();
    List<ItemDTO>
        targetItems =
        itemAPI.findItems(namespace.getAppId(), namespace.getEnv(),
            namespace.getClusterName(), namespace.getNamespaceName());

    long namespaceId = getNamespaceId(namespace);

    if (CollectionUtils.isEmpty(targetItems)) {//all source items is added
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

        if (targetItem == null) {//added items

          changeSets.addCreateItem(buildItem(namespaceId, ++maxLineNum, sourceItem));

        } else if (isModified(sourceValue, targetItem.getValue(), sourceComment,
            targetItem.getComment())) {//modified items
          targetItem.setValue(sourceValue);
          targetItem.setComment(sourceComment);
          changeSets.addUpdateItem(targetItem);
        }
      }
    }

    return changeSets;
  }

  private ItemDTO buildItem(long namespaceId, int lineNum, ItemDTO sourceItem) {
    ItemDTO createdItem = new ItemDTO();
    BeanUtils.copyEntityProperties(sourceItem, createdItem);
    createdItem.setLineNum(lineNum);
    createdItem.setNamespaceId(namespaceId);
    return createdItem;
  }

  private boolean isModified(String sourceValue, String targetValue, String sourceComment, String targetComment) {

    if (!sourceValue.equals(targetValue)) {
      return true;
    }

    if (sourceComment == null) {
      return !StringUtils.isEmpty(targetComment);
    } else if (targetComment != null) {
      return !sourceComment.equals(targetComment);
    } else {
      return false;
    }
  }
}
