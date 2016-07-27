package com.ctrip.framework.apollo.portal.entity.vo;
import com.ctrip.framework.apollo.core.dto.ItemDTO;
import com.ctrip.framework.apollo.core.dto.NamespaceDTO;

import java.util.List;

public class NamespaceVO {
  private NamespaceDTO baseInfo;
  private int itemModifiedCnt;
  private List<ItemVO> items;
  private String format;
  private boolean isPublic;
  private String parentAppId;
  private String comment;


  public NamespaceDTO getBaseInfo() {
    return baseInfo;
  }

  public void setBaseInfo(NamespaceDTO baseInfo) {
    this.baseInfo = baseInfo;
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

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(boolean aPublic) {
    isPublic = aPublic;
  }

  public String getParentAppId() {
    return parentAppId;
  }

  public void setParentAppId(String parentAppId) {
    this.parentAppId = parentAppId;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public static class ItemVO{
    private ItemDTO item;
    private boolean isModified;
    private boolean isDeleted;
    private String oldValue;
    private String newValue;

    public ItemDTO getItem() {
      return item;
    }

    public void setItem(ItemDTO item) {
      this.item = item;
    }

    public boolean isDeleted() {
      return isDeleted;
    }

    public void setDeleted(boolean deleted) {
      isDeleted = deleted;
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
