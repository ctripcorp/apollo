package com.ctrip.framework.apollo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配置的源类型，表示从哪里加载配置
 *
 * @since 1.1.0
 */
@AllArgsConstructor
@Getter
public enum ConfigSourceType {
  /**
   * 远程
   */
  REMOTE("Loaded from remote config service"),
  /**
   * 本地
   */
  LOCAL("Loaded from local cache"),
  /**
   * 无
   */
  NONE("Load failed");
  /**
   * 描述
   */
  private final String description;
}
