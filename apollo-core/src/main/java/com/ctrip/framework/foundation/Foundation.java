package com.ctrip.framework.foundation;

import com.ctrip.framework.foundation.internals.NullProviderManager;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import com.ctrip.framework.foundation.spi.ProviderManager;
import com.ctrip.framework.foundation.spi.provider.ApplicationProvider;
import com.ctrip.framework.foundation.spi.provider.NetworkProvider;
import com.ctrip.framework.foundation.spi.provider.ServerProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * 供应器基类
 */
@Slf4j
public abstract class Foundation {

  /**
   * 对象锁
   */
  private static Object lock = new Object();
  /**
   * 供应器管理器
   */
  private static volatile ProviderManager manager;

  /**
   * 初始化.
   */
  static {
    getManager();
  }

  /**
   * 获取并初始化供应器管理器
   *
   * @return
   */
  private static ProviderManager getManager() {
    try {
      if (manager == null) {
        // 双重锁定以确保只有一个线程初始化ProviderManager
        synchronized (lock) {
          if (manager == null) {
            // 加载ProviderManager接口的全部实现类中的第一个实现类
            manager = ServiceBootstrap.loadFirst(ProviderManager.class);
          }
        }
      }
      return manager;
    } catch (Throwable ex) {
      //否则为Null提供者管理器
      manager = new NullProviderManager();
      log.error("Initialize ProviderManager failed.", ex);
      return manager;
    }
  }

  /**
   * 返回具有给定名称的属性值,如果不存在返回 {@code defaultValue}
   *
   * @param name         给定的名称
   * @param defaultValue 未找到名称或发生任何错误时的默认值
   * @return 具有给定名称的属性值
   */
  public static String getProperty(String name, String defaultValue) {
    try {
      return getManager().getProperty(name, defaultValue);
    } catch (Throwable ex) {
      log.error("getProperty for {} failed.", name, ex);
      return defaultValue;
    }
  }

  /**
   * 获取网络供应器，如果不存在返回 {@code NullProviderManager#provider}
   *
   * @return 网络供应器
   */
  public static NetworkProvider net() {
    try {
      return getManager().provider(NetworkProvider.class);
    } catch (Exception ex) {
      log.error("Initialize NetworkProvider failed.", ex);
      return NullProviderManager.provider;
    }
  }

  /**
   * 获取服务器供应器，如果不存在返回 {@code NullProviderManager#provider}
   *
   * @return 服务器供应器，如果不存在返回 {@code NullProviderManager#provider}
   */
  public static ServerProvider server() {
    try {
      return getManager().provider(ServerProvider.class);
    } catch (Exception ex) {
      log.error("Initialize ServerProvider failed.", ex);
      return NullProviderManager.provider;
    }
  }

  /**
   * 获取应用供应器，如果不存在返回 {@code NullProviderManager#provider}
   *
   * @return 应用供应器，如果不存在返回 {@code NullProviderManager#provider}
   */
  public static ApplicationProvider app() {
    try {
      return getManager().provider(ApplicationProvider.class);
    } catch (Exception ex) {
      log.error("Initialize ApplicationProvider failed.", ex);
      return NullProviderManager.provider;
    }
  }
}
