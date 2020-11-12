package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.google.common.base.Preconditions;
import org.springframework.context.ApplicationEvent;

/**
 * 应用名称空间 - 创建事件
 */
public class AppNamespaceCreationEvent extends ApplicationEvent {

  /**
   * 构建 AppNamespaceCreationEvent
   *
   * @param source 源对象
   */
  public AppNamespaceCreationEvent(Object source) {
    super(source);
  }

  /**
   * 获取应用名称空间信息
   *
   * @return 应用名称空间信息
   */
  public AppNamespace getAppNamespace() {
    Preconditions.checkState(source != null);
    return (AppNamespace) this.source;
  }
}
