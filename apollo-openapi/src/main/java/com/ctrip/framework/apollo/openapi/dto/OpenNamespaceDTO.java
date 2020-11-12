package com.ctrip.framework.apollo.openapi.dto;

import java.util.List;

/**
 * 开放Api的名称空间
 */
public class OpenNamespaceDTO extends BaseDTO {

  /**
   * 应用Id
   */
  private String appId;
  /**
   * 集群名称
   */
  private String clusterName;
  /**
   * 名称空间名称
   */
  private String namespaceName;
  /**
   * 备注
   */
  private String comment;
  /**
   * 名称空间的格式（后缀）类型
   */
  private String format;
  /**
   * 名称空间是否为公共
   */
  private boolean isPublic;

  private List<OpenItemDTO> items;

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getNamespaceName() {
    return namespaceName;
  }

  public void setNamespaceName(String namespaceName) {
    this.namespaceName = namespaceName;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
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

  public List<OpenItemDTO> getItems() {
    return items;
  }

  public void setItems(List<OpenItemDTO> items) {
    this.items = items;
  }

  @Override
  public String toString() {
    return "OpenNamespaceDTO{" +
        "appId='" + appId + '\'' +
        ", clusterName='" + clusterName + '\'' +
        ", namespaceName='" + namespaceName + '\'' +
        ", comment='" + comment + '\'' +
        ", format='" + format + '\'' +
        ", isPublic=" + isPublic +
        ", items=" + items +
        ", dataChangeCreatedBy='" + dataChangeCreatedBy + '\'' +
        ", dataChangeLastModifiedBy='" + dataChangeLastModifiedBy + '\'' +
        ", dataChangeCreatedTime=" + dataChangeCreatedTime +
        ", dataChangeLastModifiedTime=" + dataChangeLastModifiedTime +
        '}';
  }
}
