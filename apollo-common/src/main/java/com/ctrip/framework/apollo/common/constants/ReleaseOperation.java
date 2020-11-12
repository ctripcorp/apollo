package com.ctrip.framework.apollo.common.constants;

/**
 * 发布操作.
 *
 * @author Jason Song(song_s@ctrip.com)
 */

public interface ReleaseOperation {

  /**
   * 正常发布
   */
  int NORMAL_RELEASE = 0;
  /**
   * 回滚
   */
  int ROLLBACK = 1;
  /**
   * 灰度发布
   */
  int GRAY_RELEASE = 2;
  /**
   * 应用灰度规则
   */
  int APPLY_GRAY_RULES = 3;
  /**
   * 灰度发布合并主节点
   */
  int GRAY_RELEASE_MERGE_TO_MASTER = 4;
  /**
   * 主节点正常发布合并灰度发布
   */
  int MASTER_NORMAL_RELEASE_MERGE_TO_GRAY = 5;
  /**
   * 主节点回滚合并灰度发布
   */
  int MATER_ROLLBACK_MERGE_TO_GRAY = 6;
  /**
   * 丢弃灰度发布
   */
  int ABANDON_GRAY_RELEASE = 7;
  /**
   * 灰度发布删除修改合并
   */
  int GRAY_RELEASE_DELETED_AFTER_MERGE = 8;
}
