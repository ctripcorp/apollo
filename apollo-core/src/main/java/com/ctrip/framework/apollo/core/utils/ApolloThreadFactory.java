package com.ctrip.framework.apollo.core.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Apollo线程工厂
 */
@Slf4j
public class ApolloThreadFactory implements ThreadFactory {

  /**
   * 线程数量
   */
  private final AtomicLong threadNumber = new AtomicLong(1);
  /**
   * 名称前缀
   */
  private final String namePrefix;
  /**
   * 是否为守护进程
   */
  private final boolean daemon;
  /**
   * 线程组
   */
  @Getter
  private static final ThreadGroup threadGroup = new ThreadGroup("Apollo");

  /**
   * 创建线程工厂
   *
   * @param namePrefix 名称前缀
   * @param daemon     是否为守护进程
   * @return 创建的线程工厂
   */
  public static ThreadFactory create(String namePrefix, boolean daemon) {
    return new ApolloThreadFactory(namePrefix, daemon);
  }

  /**
   * 等待所有停止
   *
   * @param timeoutInMillis 超时时间
   * @return true, 所有线程都已经停止，否则 ，false
   */
  public static boolean waitAllShutdown(int timeoutInMillis) {
    ThreadGroup group = getThreadGroup();
    // 活跃的线程
    Thread[] activeThreads = new Thread[group.activeCount()];
    group.enumerate(activeThreads);
    Set<Thread> alives = new HashSet<>(Arrays.asList(activeThreads));
    Set<Thread> dies = new HashSet<>();
    log.info("Current ACTIVE thread count is: {}", alives.size());
    // 过期时间戳
    long expire = System.currentTimeMillis() + timeoutInMillis;
    // 如果没超过过期时间
    while (System.currentTimeMillis() < expire) {
      // 剔除非活跃、中断、是守护进程的线程
      classify(alives, dies,
          thread -> !thread.isAlive() || thread.isInterrupted() || thread.isDaemon());
      if (alives.size() > 0) {
        log.info("Alive apollo threads: {}", alives);
        try {
          TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ex) {
          // ignore
        }
      } else {
        log.info("All apollo threads are shutdown.");
        return true;
      }
    }
    log.warn("Some apollo threads are still alive but expire time has reached, alive threads: {}",
        alives);
    return false;
  }

  /**
   * 分类标准接口
   *
   * @param <T>
   */
  private interface ClassifyStandard<T> {

    /**
     * 分类应满足的条件方法
     *
     * @param thread 线程
     * @return true, 满足，否则，false
     */
    boolean satisfy(T thread);
  }

  /**
   * 分类
   *
   * @param src      源
   * @param des      目标
   * @param standard 分类标准接口
   * @param <T>      泛型
   */
  private static <T> void classify(Set<T> src, Set<T> des, ClassifyStandard<T> standard) {
    Set<T> set = new HashSet<>();
    // 分类
    for (T t : src) {
      if (standard.satisfy(t)) {
        set.add(t);
      }
    }
    // 排除满足条件的对象
    src.removeAll(set);
    // 向目标中添加满足条件的对象
    des.addAll(set);
  }

  /**
   * 构建ApolloThreadFactory
   *
   * @param namePrefix 名称前缀
   * @param daemon     是否为守护进程
   */
  private ApolloThreadFactory(String namePrefix, boolean daemon) {
    this.namePrefix = namePrefix;
    this.daemon = daemon;
  }

  /**
   * 创建一个新线程
   *
   * @param runnable
   * @return
   */
  @Override
  public Thread newThread(Runnable runnable) {
    // 创建线程
    Thread thread = new Thread(threadGroup, runnable,
        threadGroup.getName() + "-" + namePrefix + "-" + threadNumber.getAndIncrement());
    // 设置属性
    thread.setDaemon(daemon);
    if (thread.getPriority() != Thread.NORM_PRIORITY) {
      thread.setPriority(Thread.NORM_PRIORITY);
    }
    return thread;
  }
}
