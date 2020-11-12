package com.ctrip.framework.apollo.common.dto;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置项创建、更新、删除的列表数据
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ItemChangeSets extends BaseDTO {

  /**
   * 创建的配置项列表记录
   */
  private List<ItemDTO> createItems = new LinkedList<>();
  /**
   * 更新的配置项列表记录
   */
  private List<ItemDTO> updateItems = new LinkedList<>();
  /**
   * 删除的配置项列表记录
   */
  private List<ItemDTO> deleteItems = new LinkedList<>();

  /**
   * 添加创建的记录
   *
   * @param item 创建的配置项
   */
  public void addCreateItem(ItemDTO item) {
    createItems.add(item);
  }

  /**
   * 添加更新的记录
   *
   * @param item 创建的配置项
   */
  public void addUpdateItem(ItemDTO item) {
    updateItems.add(item);
  }

  /**
   * 添加删除的记录
   *
   * @param item 创建的配置项
   */
  public void addDeleteItem(ItemDTO item) {
    deleteItems.add(item);
  }

  /**
   * 配置项创建、更新、删除的列表数据是否为空
   *
   * @return true, 表示数据为空，否则 ，false
   */
  public boolean isEmpty() {
    return createItems.isEmpty() && updateItems.isEmpty() && deleteItems.isEmpty();
  }
}
