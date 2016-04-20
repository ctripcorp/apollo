package com.ctrip.apollo.core.dto;

public class ItemDTO extends BaseDTO{

  private long namespaceId;

  private String key;

  private String value;

  private String comment;

  private int lineNum;

  public ItemDTO() {

  }

  public ItemDTO(String key, String value, String comment, int lineNum) {
    this.key = key;
    this.value = value;
    this.comment = comment;
    this.lineNum = lineNum;
  }

  public String getComment() {
    return comment;
  }

  public String getKey() {
    return key;
  }

  public long getNamespaceId() {
    return namespaceId;
  }

  public String getValue() {
    return value;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setNamespaceId(long namespaceId) {
    this.namespaceId = namespaceId;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public int getLineNum() {
    return lineNum;
  }

  public void setLineNum(int lineNum) {
    this.lineNum = lineNum;
  }


  @Override
  public String toString() {
    return "ItemDTO{" +
           "id=" + id +
           ", namespaceId=" + namespaceId +
           ", key='" + key + '\'' +
           ", value='" + value + '\'' +
           ", comment='" + comment + '\'' +
           ", lineNum=" + lineNum +
           ", dataChangeCreatedBy='" + dataChangeCreatedBy + '\'' +
           ", dataChangeCreatedTime=" + dataChangeCreatedTime +
           ", dataChangeLastModifiedBy='" + dataChangeLastModifiedBy + '\'' +
           ", dataChangeLastModifiedTime=" + dataChangeLastModifiedTime +
           '}';
  }
}
