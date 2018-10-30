package com.ctrip.framework.apollo.openapi.dto;

import java.util.List;

public class OpenNamespaceDTO extends BaseDTO {

  private String appId;

  private String clusterName;

  private String namespaceName;

  private String comment;

  private String format;

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
}
