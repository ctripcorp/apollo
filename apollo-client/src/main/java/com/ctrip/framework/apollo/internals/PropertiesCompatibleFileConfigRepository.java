package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.PropertiesCompatibleConfigFile;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.google.common.base.Preconditions;
import java.util.Properties;

/**
 * Properties兼容文件配置 Repository
 */
public class PropertiesCompatibleFileConfigRepository extends AbstractConfigRepository implements
    ConfigFileChangeListener {

  /**
   * Properties兼容的配置文件
   */
  private final PropertiesCompatibleConfigFile configFile;
  /**
   * 缓存Properties
   */
  private volatile Properties cachedProperties;

  public PropertiesCompatibleFileConfigRepository(PropertiesCompatibleConfigFile configFile) {
    this.configFile = configFile;
    // 添加配置变更监听器
    this.configFile.addChangeListener(this);
    // 尝试同步配置
    this.trySync();
  }

  @Override
  protected synchronized void sync() {
    // 当前配置文件的Properties对象信息
    Properties current = configFile.asProperties();

    Preconditions.checkState(current != null,
        "PropertiesCompatibleConfigFile.asProperties should never return null");

    // 若不相等，说明更新了，设置到缓存中
    if (cachedProperties != current) {
      cachedProperties = current;
      this.fireRepositoryChange(configFile.getNamespace(), cachedProperties);
    }
  }

  @Override
  public Properties getConfig() {
    // 存在就返回，不存在同步
    if (cachedProperties == null) {
      sync();
    }
    return cachedProperties;
  }

  @Override
  public void setUpstreamRepository(ConfigRepository upstreamConfigRepository) {
    // 配置文件是上游，所以不需要设置额外的上游
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
