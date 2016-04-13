package com.ctrip.apollo.portal.service.txtresolver;

import com.ctrip.apollo.core.dto.ItemChangeSets;

public class TextResolverResult {

  private boolean isResolveSuccess;
  /**
   * error msg
   */
  private String msg = "";
  private ItemChangeSets changeSets;

  public boolean isResolveSuccess() {
    return isResolveSuccess;
  }

  public void setResolveSuccess(boolean resolveSuccess) {
    isResolveSuccess = resolveSuccess;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public ItemChangeSets getChangeSets() {
    return changeSets;
  }

  public void setChangeSets(ItemChangeSets changeSets) {
    this.changeSets = changeSets;
  }

}
