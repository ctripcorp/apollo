package com.ctrip.framework.apollo.internals;

import java.util.Properties;

import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.PropertiesCompatibleConfigFile;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.google.common.base.Preconditions;

public class PropertiesCompatibleFileConfigRepository extends AbstractConfigRepository implements
    ConfigFileChangeListener {
  private final PropertiesCompatibleConfigFile configFile;
  private volatile Properties cachedProperties;

  public PropertiesCompatibleFileConfigRepository(PropertiesCompatibleConfigFile configFile) {
    this.configFile = configFile;
    this.configFile.addChangeListener(this);
    this.trySync();
  }

  @Override
  protected synchronized void sync() {
    Properties current = configFile.asProperties();

    Preconditions.checkState(current != null, "PropertiesCompatibleConfigFile.asProperties should never return null");

    if (cachedProperties != current) {
      cachedProperties = current;
      this.fireRepositoryChange(configFile.getNamespace(), cachedProperties);
    }
  }

  @Override
  public Properties getConfig() {
    if (cachedProperties == null) {
      sync();
    }
    return cachedProperties;
  }

  @Override
  public void setUpstreamRepository(ConfigRepository upstreamConfigRepository) {
    //config file is the upstream, so no need to set up extra upstream
  }

  @Override
  public ConfigSourceType getSourceType() {
    return configFile.getSourceType();
  }

  @Override
  public void onChange(ConfigFileChangeEvent changeEvent) {
    this.trySync();
  }
}
