package com.ctrip.apollo.portal.entity;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.dto.NamespaceDTO;

import java.util.List;

public class NamespaceVO {
  private NamespaceDTO namespace;
  private int itemModifiedCnt;
  private List<ItemVO> items;


  public NamespaceDTO getNamespace() {
    return namespace;
  }

  public void setNamespace(NamespaceDTO namespace) {
    this.namespace = namespace;
  }

  public int getItemModifiedCnt() {
    return itemModifiedCnt;
  }

  public void setItemModifiedCnt(int itemModifiedCnt) {
    this.itemModifiedCnt = itemModifiedCnt;
  }

  public List<ItemVO> getItems() {
    return items;
  }

  public void setItems(List<ItemVO> items) {
    this.items = items;
  }

  public static class ItemVO{
    private ItemDTO item;
    private boolean isModified;
    private String oldValue;
    private String newValue;

    public ItemDTO getItem() {
      return item;
    }

    public void setItem(ItemDTO item) {
      this.item = item;
    }

    public boolean isModified() {
      return isModified;
    }

    public void setModified(boolean isModified) {
      this.isModified = isModified;
    }

    public String getOldValue() {
      return oldValue;
    }

    public void setOldValue(String oldValue) {
      this.oldValue = oldValue;
    }

    public String getNewValue() {
      return newValue;
    }

    public void setNewValue(String newValue) {
      this.newValue = newValue;
    }

  }

}
