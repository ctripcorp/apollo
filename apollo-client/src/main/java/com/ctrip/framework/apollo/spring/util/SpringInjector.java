package com.ctrip.framework.apollo.spring.util;

import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.property.PlaceholderHelper;
import com.ctrip.framework.apollo.spring.property.SpringValueRegistry;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * Spring 注入器，实现依赖注入( DI，全称“Dependency Injection” ) 。
 * <p>
 * 类似 DefaultInjector 。但是不要被类名“欺骗”啦，只是注入集成 Spring 需要做的 DI ，例如 SpringValueRegistry 的单例
 */
public class SpringInjector {

  /**
   * 注入器
   */
  private static volatile Injector s_injector;
  /**
   * 锁
   */
  private static final Object lock = new Object();

  /**
   * 构造方法，获取注入器的实例
   *
   * @return 注入器的实例
   */
  private static Injector getInjector() {
    // 若 Injector 不存在，则进行获得
    if (s_injector == null) {
      synchronized (lock) {
        // 若 Injector 不存在，则进行获得
        if (s_injector == null) {
          try {
            s_injector = Guice.createInjector(new SpringModule());
          } catch (Throwable ex) {
            ApolloConfigException exception = new ApolloConfigException(
                "Unable to initialize Apollo Spring Injector!", ex);
            Tracer.logError(exception);
            throw exception;
          }
        }
      }
    }

    return s_injector;
  }

  /**
   * 获取指定类型的实例
   *
   * @param clazz Class对象
   * @param <T>   泛型
   * @return 实例对象
   */
  public static <T> T getInstance(Class<T> clazz) {
    try {
      return getInjector().getInstance(clazz);
    } catch (Throwable ex) {
      Tracer.logError(ex);
      throw new ApolloConfigException(
          String.format("Unable to load instance for %s!", clazz.getName()), ex);
    }
  }

  /**
   * SpringModule 类，告诉 Spring Guice哪些实例需要DI的配置
   */
  private static class SpringModule extends AbstractModule {

    @Override
    protected void configure() {
      // 配置类的scope类型
      bind(PlaceholderHelper.class).in(Singleton.class);
      bind(ConfigPropertySourceFactory.class).in(Singleton.class);
      bind(SpringValueRegistry.class).in(Singleton.class);
    }
  }
}
