package com.ctrip.framework.foundation.internals.provider;

import com.ctrip.framework.foundation.internals.NetworkInterfaceManager;
import com.ctrip.framework.foundation.spi.provider.NetworkProvider;
import com.ctrip.framework.foundation.spi.provider.Provider;

/**
 * 默认的网络供应器.
 */
public class DefaultNetworkProvider implements NetworkProvider {

  @Override
  public String getProperty(String name, String defaultValue) {
    // 获取主机地址，即ip.
    if ("host.address".equalsIgnoreCase(name)) {
      String val = getHostAddress();
      return val == null ? defaultValue : val;
    }
    // 获取主机名
    if ("host.name".equalsIgnoreCase(name)) {
      String val = getHostName();
      return val == null ? defaultValue : val;
    }
    return defaultValue;
  }

  @Override
  public void initialize() {

  }

  @Override
  public String getHostAddress() {
    return NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
  }

  @Override
  public String getHostName() {
    return NetworkInterfaceManager.INSTANCE.getLocalHostName();
  }

  @Override
  public Class<? extends Provider> getType() {
    return NetworkProvider.class;
  }

  @Override
  public String toString() {
    return "hostName [" + getHostName() + "] hostIP [" + getHostAddress()
        + "] (DefaultNetworkProvider)";
  }
}
