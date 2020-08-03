package com.ctrip.framework.apollo.portal.listener;

import com.google.common.base.Preconditions;

import com.ctrip.framework.apollo.common.entity.AppNamespace;

import org.springframework.context.ApplicationEvent;

public class AppNamespaceCreationEvent extends ApplicationEvent {

  private static final long serialVersionUID = 6590632108409290989L;

  public AppNamespaceCreationEvent(Object source) {
    super(source);
  }

  public AppNamespace getAppNamespace() {
    Preconditions.checkState(source != null);
    return (AppNamespace) this.source;
  }
}
