package com.ctrip.apollo.core.dto;

import java.util.LinkedList;
import java.util.List;

/**
 * storage cud result
 */
public class ItemChangeSets {

  private String modifyBy;
  private List<ItemDTO> createItems = new LinkedList<>();
  private List<ItemDTO> updateItems = new LinkedList<>();
  private List<ItemDTO> deletedItems = new LinkedList<>();

  public void addCreatedItem(ItemDTO item) {
    createItems.add(item);
  }

  public void addUpdateItem(ItemDTO item) {
    updateItems.add(item);
  }

  public void addDeletedItem(ItemDTO item) {
    deletedItems.add(item);
  }


  public List<ItemDTO> getCreateItems() {
    return createItems;
  }

  public List<ItemDTO> getUpdateItems() {
    return updateItems;
  }

  public List<ItemDTO> getDeletedItems() {
    return deletedItems;
  }

  public void setCreateItems(List<ItemDTO> createItems) {
    this.createItems = createItems;
  }

  public void setUpdateItems(List<ItemDTO> updateItems) {
    this.updateItems = updateItems;
  }

  public void setDeletedItems(List<ItemDTO> deletedItems) {
    this.deletedItems = deletedItems;
  }

  public String getModifyBy() {
    return modifyBy;
  }

  public void setModifyBy(String modifyBy) {
    this.modifyBy = modifyBy;
  }

}
