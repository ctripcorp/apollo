package com.ctrip.framework.apollo.common.constants;

/**
 * 名称空间分支状态
 */
public interface NamespaceBranchStatus {

  /**
   * 删除
   */
  int DELETED = 0;
  /**
   * 活跃
   */
  int ACTIVE = 1;
  /**
   * 合并
   */
  int MERGED = 2;

}
