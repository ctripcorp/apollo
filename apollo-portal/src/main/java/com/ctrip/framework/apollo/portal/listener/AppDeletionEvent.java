package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.entity.App;
import com.google.common.base.Preconditions;
import org.springframework.context.ApplicationEvent;

/**
 * 应用删除事件。
 */
public class AppDeletionEvent extends ApplicationEvent {

  /**
   * 构建应用删除事件
   *
   * @param source 事件源对象
   */
  public AppDeletionEvent(Object source) {
    super(source);
  }

  /**
   * 获取应用信息
   *
   * @return 应用信息
   */
  public App getApp() {
    Preconditions.checkState(source != null);
    return (App) this.source;
  }
}
