package com.ctrip.framework.foundation.internals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.foundation.spi.ProviderManager;
import com.ctrip.framework.foundation.spi.provider.ApplicationProvider;
import com.ctrip.framework.foundation.spi.provider.NetworkProvider;
import com.ctrip.framework.foundation.spi.provider.Provider;
import com.ctrip.framework.foundation.spi.provider.ServerProvider;

public class DefaultProviderManager implements ProviderManager {
  private static final Logger logger = LoggerFactory.getLogger(DefaultProviderManager.class);
  private Map<Class<? extends Provider>, Provider> m_providers = new LinkedHashMap<>();

  public DefaultProviderManager() {
    // Load per-application configuration, like app id, from classpath://META-INF/app.properties
    //Provider applicationProvider = new DefaultApplicationProvider();
    
    ApplicationProvider  applicationProvider = ServiceBootstrap.loadFirst(ApplicationProvider.class, ServiceBootstrap.ASC);
    applicationProvider.initialize();
    register(applicationProvider);

    // Load network parameters
    //Provider networkProvider = new DefaultNetworkProvider();
    NetworkProvider networkProvider = ServiceBootstrap.loadFirst(NetworkProvider.class, ServiceBootstrap.ASC);
    networkProvider.initialize();
    register(networkProvider);

    // Load environment (fat, fws, uat, prod ...) and dc, from /opt/settings/server.properties, JVM property and/or OS
    // environment variables.
    //Provider serverProvider = new DefaultServerProvider();
    ServerProvider serverProvider = ServiceBootstrap.loadFirst(ServerProvider.class, ServiceBootstrap.ASC);
    serverProvider.initialize();
    register(serverProvider);
  }

  public synchronized void register(Provider provider) {
    m_providers.put(provider.getType(), provider);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Provider> T provider(Class<T> clazz) {
    Provider provider = m_providers.get(clazz);

    if (provider != null) {
      return (T) provider;
    } else {
      logger.error("No provider [{}] found in DefaultProviderManager, please make sure it is registered in DefaultProviderManager ",
          clazz.getName());
      return (T) NullProviderManager.provider;
    }
  }

  @Override
  public String getProperty(String name, String defaultValue) {
    for (Provider provider : m_providers.values()) {
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
    if (null != m_providers) {
      for (Map.Entry<Class<? extends Provider>, Provider> entry : m_providers.entrySet()) {
        sb.append(entry.getValue()).append("\n");
      }
    }
    sb.append("(DefaultProviderManager)").append("\n");
    return sb.toString();
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
