package com.ctrip.framework.apollo.build;

import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.internals.Injector;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;

/**
 * Apollo 注入器，实现依赖注入
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloInjector {

  /**
   * 注入器
   */
  private static volatile Injector s_injector;
  /**
   * 锁
   */
  private static final Object lock = new Object();

  /**
   * 获取注入器
   *
   * @return 注入器
   */
  private static Injector getInjector() {
    // 若 Injector 不存在，则进行获得
    if (s_injector == null) {
      // 若 Injector 不存在，则进行获得
      synchronized (lock) {
        if (s_injector == null) {
          try {
            // 基于 JDK SPI 加载对应的 Injector 实现对象
            s_injector = ServiceBootstrap.loadFirst(Injector.class);
          } catch (Throwable ex) {
            ApolloConfigException exception = new ApolloConfigException(
                "Unable to initialize Apollo Injector!", ex);
            Tracer.logError(exception);
            throw exception;
          }
        }
      }
    }

    return s_injector;
  }

  /**
   * 根据Class获取实例
   *
   * @param clazz Class对象
   * @param <T>   实例泛型
   * @return 实例类型
   */
  public static <T> T getInstance(Class<T> clazz) {
    try {
      return getInjector().getInstance(clazz);
    } catch (Throwable ex) {
      Tracer.logError(ex);
      throw new ApolloConfigException(
          String.format("Unable to load instance for type %s!", clazz.getName()), ex);
    }
  }

  /**
   * 根据Class和实例名称获取实例
   *
   * @param clazz Class对象
   * @param <T>   实例泛型
   * @return 实例类型
   */
  public static <T> T getInstance(Class<T> clazz, String name) {
    try {
      return getInjector().getInstance(clazz, name);
    } catch (Throwable ex) {
      Tracer.logError(ex);
      throw new ApolloConfigException(
          String.format("Unable to load instance for type %s and name %s !", clazz.getName(), name),
          ex);
    }
  }
}
