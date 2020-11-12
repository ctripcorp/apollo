package com.ctrip.framework.apollo.portal.component;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Item 数组比较器，用于比较两个 Item 数组的差异，并返回差异的变更结果集
 */
@Component
public class ItemsComparator {

  /**
   * 配置项比较
   *
   * @param baseNamespaceId 基名称空间id
   * @param baseItems       基配置项列表
   * @param targetItems     目标配置项列表
   * @return 配置项变更列表
   */
  public ItemChangeSets compareIgnoreBlankAndCommentItem(long baseNamespaceId,
      List<ItemDTO> baseItems, List<ItemDTO> targetItems) {
    // 过滤空行和注释的配置项
    List<ItemDTO> filteredSourceItems = filterBlankAndCommentItem(baseItems);
    List<ItemDTO> filteredTargetItems = filterBlankAndCommentItem(targetItems);

    // 创建 ItemDTO Map
    Map<String, ItemDTO> sourceItemMap = BeanUtils.mapByKey("key", filteredSourceItems);
    Map<String, ItemDTO> targetItemMap = BeanUtils.mapByKey("key", filteredTargetItems);

    // 创建 ItemChangeSets 对象
    ItemChangeSets changeSets = new ItemChangeSets();

    // 处理新增或修改的情况
    for (ItemDTO item : targetItems) {
      String key = item.getKey();

      ItemDTO sourceItem = sourceItemMap.get(key);
      // 新增的情况
      if (sourceItem == null) {
        ItemDTO copiedItem = copyItem(item);
        copiedItem.setNamespaceId(baseNamespaceId);
        changeSets.addCreateItem(copiedItem);
      } else if (!Objects.equals(sourceItem.getValue(), item.getValue())) {
        // 修改的情况
        //only value & comment can be update
        sourceItem.setValue(item.getValue());
        sourceItem.setComment(item.getComment());
        changeSets.addUpdateItem(sourceItem);
      }
    }

    // 处理删除的情况
    for (ItemDTO item : baseItems) {
      String key = item.getKey();

      ItemDTO targetItem = targetItemMap.get(key);
      //delete
      if (targetItem == null) {
        changeSets.addDeleteItem(item);
      }
    }

    return changeSets;
  }

  /**
   * 过滤空行和注释的配置项
   *
   * @param items 配置项列表
   * @return 过滤后的配置项列表
   */
  private List<ItemDTO> filterBlankAndCommentItem(List<ItemDTO> items) {

    List<ItemDTO> result = new LinkedList<>();
    if (CollectionUtils.isEmpty(items)) {
      return result;
    }

    for (ItemDTO item : items) {
      if (!StringUtils.isBlank(item.getKey())) {
        result.add(item);
      }
    }
    return result;
  }

  /**
   * 拷贝配置项
   *
   * @param sourceItem 源配置项
   * @return 配置项信息
   */
  private ItemDTO copyItem(ItemDTO sourceItem) {
    ItemDTO copiedItem = new ItemDTO();
    copiedItem.setKey(sourceItem.getKey());
    copiedItem.setValue(sourceItem.getValue());
    copiedItem.setComment(sourceItem.getComment());
    return copiedItem;

  }

}
