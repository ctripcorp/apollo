package com.ctrip.framework.apollo.core.schedule;

/**
 * 基于指数级计算的定时策略实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ExponentialSchedulePolicy implements SchedulePolicy {

  /**
   * 延迟时间下限
   */
  private final long delayTimeLowerBound;
  /**
   * 延迟时间上限
   */
  private final long delayTimeUpperBound;
  /**
   * 最后延迟执行时间
   */
  private long lastDelayTime;

  /**
   * 构建ExponentialSchedulePolicy
   *
   * @param delayTimeLowerBound 延迟时间下限
   * @param delayTimeUpperBound 延迟时间上限
   */
  public ExponentialSchedulePolicy(long delayTimeLowerBound, long delayTimeUpperBound) {
    this.delayTimeLowerBound = delayTimeLowerBound;
    this.delayTimeUpperBound = delayTimeUpperBound;
  }

  @Override
  public long fail() {

    long delayTime = lastDelayTime;
    // 设置初始时间
    if (delayTime == 0) {
      delayTime = delayTimeLowerBound;
    } else {
      // 指数级计算，直到上限
      delayTime = Math.min(lastDelayTime << 1, delayTimeUpperBound);
    }
    // 最后延迟执行时间
    lastDelayTime = delayTime;

    return delayTime;
  }

  @Override
  public void success() {
    lastDelayTime = 0;
  }
}
