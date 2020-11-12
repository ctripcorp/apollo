package com.ctrip.framework.apollo.core.schedule;

/**
 * 定时策略接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface SchedulePolicy {

  /**
   * 执行失败
   *
   * @return 下次执行延迟
   */
  long fail();

  /**
   * 执行成功
   */
  void success();
}
