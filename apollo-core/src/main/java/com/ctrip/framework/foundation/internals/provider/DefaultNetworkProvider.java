package com.ctrip.framework.foundation.internals.provider;

import com.ctrip.framework.foundation.internals.NetworkInterfaceManager;
import com.ctrip.framework.foundation.spi.provider.NetworkProvider;
import com.ctrip.framework.foundation.spi.provider.Provider;
import com.google.common.collect.Maps;

import java.util.Map;

public class DefaultNetworkProvider implements NetworkProvider {
  
  private Map<String,String> m_netWorkProperties = Maps.newHashMap();
  
  @Override
  public String getProperty(String name, String defaultValue) {
    String val = m_netWorkProperties.get(name);
    return val == null ? defaultValue : val;
  }

  @Override
  public void initialize() {
     m_netWorkProperties.put("host.address", getHostAddress());
     m_netWorkProperties.put("host.name", getHostName());
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
    return "hostName [" + getHostName() + "] hostIP [" + getHostAddress() + "] (DefaultNetworkProvider)";
  }
}
