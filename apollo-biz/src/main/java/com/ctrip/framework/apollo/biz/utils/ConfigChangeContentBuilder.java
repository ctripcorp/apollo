package com.ctrip.framework.apollo.biz.utils;

import com.ctrip.framework.apollo.biz.entity.Item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

/**
 * 配置项改变内容构建器
 */
public class ConfigChangeContentBuilder {

  /**
   * GSON对象
   */
  private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
  /**
   * 创建的配置项
   */
  @Getter
  private List<Item> createItems = new LinkedList<>();
  /**
   * 更新的配置项
   */
  @Getter
  private List<ItemPair> updateItems = new LinkedList<>();
  /**
   * 删除的配置项
   */
  @Getter
  private List<Item> deleteItems = new LinkedList<>();

  /**
   * 添加配置项
   *
   * @param item 配置项
   * @return 配置项改变内容构建器
   */
  public ConfigChangeContentBuilder createItem(Item item) {
    if (!StringUtils.isBlank(item.getKey())) {
      createItems.add(cloneItem(item));
    }
    return this;
  }

  /**
   * 更新配置项
   *
   * @param oldItem 旧的配置项
   * @param newItem 新的配置项
   * @return 配置项改变内容构建器
   */
  public ConfigChangeContentBuilder updateItem(Item oldItem, Item newItem) {
    if (!oldItem.getValue().equals(newItem.getValue())) {
      ItemPair itemPair = new ItemPair(cloneItem(oldItem), cloneItem(newItem));
      updateItems.add(itemPair);
    }
    return this;
  }

  /**
   * 删除配置项
   *
   * @param item 配置项
   * @return 配置项改变内容构建器
   */
  public ConfigChangeContentBuilder deleteItem(Item item) {
    if (!StringUtils.isBlank(item.getKey())) {
      deleteItems.add(cloneItem(item));
    }
    return this;
  }

  /**
   * 是否存在内容
   *
   * @return true，存在内容，否则，false
   */
  public boolean hasContent() {
    return !createItems.isEmpty() || !updateItems.isEmpty() || !deleteItems.isEmpty();
  }

  /**
   * 构建
   *
   * @return 构建转换好的JSON字符串
   */
  public String build() {
    //因为事务第一段提交并没有更新时间,所以build时统一更新
    Date now = new Date();

    for (Item item : createItems) {
      item.setDataChangeLastModifiedTime(now);
    }

    for (ItemPair item : updateItems) {
      item.newItem.setDataChangeLastModifiedTime(now);
    }

    for (Item item : deleteItems) {
      item.setDataChangeLastModifiedTime(now);
    }
    return GSON.toJson(this);
  }

  /**
   * 配置项对
   */
  @AllArgsConstructor
  static class ItemPair {

    /**
     * 旧配置项
     */
    Item oldItem;
    /**
     * 新配置项
     */
    Item newItem;
  }

  /**
   * 拷贝配置项
   *
   * @param source 配置项
   * @return 配置项
   */
  Item cloneItem(Item source) {
    Item target = new Item();
    BeanUtils.copyProperties(source, target);
    return target;
  }

  /**
   * 转换为配置项改变内容构建器
   *
   * @param content 配置项内容
   * @return 配置项改变内容构建器
   */
  public static ConfigChangeContentBuilder convertJsonString(String content) {
    return GSON.fromJson(content, ConfigChangeContentBuilder.class);
  }
}
