package com.ctrip.framework.apollo.openapi.dto;

public class OpenNamespaceLockDTO {

  private String namespaceName;
  private boolean isLocked;
  private String lockedBy;

  public String getNamespaceName() {
    return namespaceName;
  }

  public void setNamespaceName(String namespaceName) {
    this.namespaceName = namespaceName;
  }

  public boolean isLocked() {
    return isLocked;
  }

  public void setLocked(boolean locked) {
    isLocked = locked;
  }

  public String getLockedBy() {
    return lockedBy;
  }

  public void setLockedBy(String lockedBy) {
    this.lockedBy = lockedBy;
  }

  @Override
  public String toString() {
    return "OpenNamespaceLockDTO{" +
        "namespaceName='" + namespaceName + '\'' +
        ", isLocked=" + isLocked +
        ", lockedBy='" + lockedBy + '\'' +
        '}';
  }
}
