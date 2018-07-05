package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.entity.App;
import com.google.common.base.Preconditions;
import org.springframework.context.ApplicationEvent;

public class AppDeletionEvent extends ApplicationEvent {

  private String newAppId;

  public AppDeletionEvent(Object source, String newAppId) {
    super(source);
    this.newAppId = newAppId;
  }

  public String getNewAppId() {
    return newAppId;
  }

  public App getApp() {
    Preconditions.checkState(source != null);
    return (App) this.source;
  }
}
