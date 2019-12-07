package com.ctrip.framework.foundation.internals.provider;

import com.ctrip.framework.foundation.spi.provider.ApplicationProvider;
import com.ctrip.framework.foundation.spi.provider.NetworkProvider;
import com.ctrip.framework.foundation.spi.provider.Provider;
import com.ctrip.framework.foundation.spi.provider.ServerProvider;

import java.io.InputStream;

public class NullProvider implements ApplicationProvider, NetworkProvider, ServerProvider {
  @Override
  public Class<? extends Provider> getType() {
    return null;
  }

  @Override
  public String getProperty(String name, String defaultValue) {
    return defaultValue;
  }

  @Override
  public void initialize() {

  }

  @Override
  public String getAppId() {
    return null;
  }

  @Override
  public boolean isAppIdSet() {
    return false;
  }

  @Override
  public String getEnvType() {
    return null;
  }

  @Override
  public boolean isEnvTypeSet() {
    return false;
  }

  @Override
  public String getDataCenter() {
    return null;
  }

  @Override
  public boolean isDataCenterSet() {
    return false;
  }

  @Override
  public void initialize(InputStream in) {

  }

  @Override
  public String getHostAddress() {
    return null;
  }

  @Override
  public String getHostName() {
    return null;
  }

  @Override
  public String toString() {
    return "(NullProvider)";
  }
}
