package com.ctrip.framework.foundation.internals;

import com.ctrip.framework.foundation.internals.provider.DefaultApplicationProvider;
import com.ctrip.framework.foundation.internals.provider.DefaultNetworkProvider;
import com.ctrip.framework.foundation.internals.provider.DefaultServerProvider;
import com.ctrip.framework.foundation.spi.ProviderManager;
import com.ctrip.framework.foundation.spi.provider.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认的供应管理器
 */
@Slf4j
public class DefaultProviderManager implements ProviderManager {

  /**
   * 供应器列表<指定的供应器实现类Class,供应器实现类>
   */
  private Map<Class<? extends Provider>, Provider> providers = new LinkedHashMap<>();

  /**
   * 构造默认供应管理器
   */
  public DefaultProviderManager() {
    // 从/META-INF/app.properties 加载每个应用配置，如Appid，
    Provider applicationProvider = new DefaultApplicationProvider();
    applicationProvider.initialize();
    register(applicationProvider);

    // 加载网络参数
    Provider networkProvider = new DefaultNetworkProvider();
    networkProvider.initialize();
    register(networkProvider);

    // 从 /opt/settings/server.properties、JVM属性或操作系统环境变量加载Env（fat、fws、uat、prod…）和数据中心(dc)，
    Provider serverProvider = new DefaultServerProvider();
    serverProvider.initialize();
    register(serverProvider);
  }

  /**
   * 注册供应器
   *
   * @param provider 指定的供应器实现类
   */
  public synchronized void register(Provider provider) {
    providers.put(provider.getType(), provider);
  }

  @Override
  public <T extends Provider> T provider(Class<T> clazz) {
    Provider provider = providers.get(clazz);

    if (provider != null) {
      return (T) provider;
    }
    log.error(
        "No provider [{}] found in DefaultProviderManager, please make sure it is registered in DefaultProviderManager ",
        clazz.getName());
    return (T) NullProviderManager.provider;
  }

  @Override
  public String getProperty(String name, String defaultValue) {
    for (Provider provider : providers.values()) {
      String value = provider.getProperty(name, null);

      if (value != null) {
        return value;
      }
    }
    return defaultValue;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(512);
    if (null != providers) {
      for (Map.Entry<Class<? extends Provider>, Provider> entry : providers.entrySet()) {
        sb.append(entry.getValue()).append("\n");
      }
    }
    sb.append("(DefaultProviderManager)").append("\n");
    return sb.toString();
  }
}
