package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.google.common.base.Preconditions;
import org.springframework.context.ApplicationEvent;

/**
 * 应用名称空间 - 删除事件
 */
public class AppNamespaceDeletionEvent extends ApplicationEvent {

  /**
   * 构建 AppNamespaceDeletionEvent
   *
   * @param source 源对象
   */
  public AppNamespaceDeletionEvent(Object source) {
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
