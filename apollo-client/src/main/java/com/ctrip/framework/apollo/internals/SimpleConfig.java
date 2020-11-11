package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * 常用的配置实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public class SimpleConfig extends AbstractConfig implements RepositoryChangeListener {

  /**
   * 名称空间名称
   */
  private final String m_namespace;
  /**
   * 配置 Repository
   */
  private final ConfigRepository m_configRepository;
  /**
   * 配置 Properties 的缓存引用
   */
  private volatile Properties m_configProperties;
  /**
   * 配置源的类型
   */
  private volatile ConfigSourceType m_sourceType = ConfigSourceType.NONE;

  /**
   * 构建SimpleConfig.
   *
   * @param namespace        此配置实例的名称空间
   * @param configRepository 此配置实例的配置存储库
   */
  public SimpleConfig(String namespace, ConfigRepository configRepository) {
    m_namespace = namespace;
    m_configRepository = configRepository;
    this.initialize();
  }

  /**
   * 初始化
   */
  private void initialize() {
    try {
      // 更新配置
      updateConfig(m_configRepository.getConfig(), m_configRepository.getSourceType());
    } catch (Throwable ex) {
      Tracer.logError(ex);
      log.warn("Init Apollo Simple Config failed - namespace: {}, reason: {}", m_namespace,
          ExceptionUtil.getDetailMessage(ex));
    } finally {
      //register the change listener no matter config repository is working or not
      //so that whenever config repository is recovered, config could get changed
      // 注册到 ConfigRepository 中，从而实现每次配置发生变更时，更新配置缓存 `m_configProperties`
      m_configRepository.addChangeListener(this);
    }
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    // 查询指定key的属性，不存在为默认值
    if (m_configProperties == null) {
      log.warn("Could not load config from Apollo, always return default value!");
      return defaultValue;
    }
    return this.m_configProperties.getProperty(key, defaultValue);
  }

  @Override
  public Set<String> getPropertyNames() {
    // 查询配置Properties的属性key
    if (m_configProperties == null) {
      return Collections.emptySet();
    }

    return m_configProperties.stringPropertyNames();
  }

  @Override
  public ConfigSourceType getSourceType() {
    return m_sourceType;
  }

  @Override
  public synchronized void onRepositoryChange(String namespace, Properties newProperties) {
    // 忽略，若未变更
    if (newProperties.equals(m_configProperties)) {
      return;
    }
    // 读取新的 Properties 对象
    Properties newConfigProperties = propertiesFactory.getPropertiesInstance();
    newConfigProperties.putAll(newProperties);

    // 计算配置变更集合
    List<ConfigChange> changes = calcPropertyChanges(namespace, m_configProperties,
        newConfigProperties);
    Map<String, ConfigChange> changeMap = Maps.uniqueIndex(changes,
        new Function<ConfigChange, String>() {
          @Override
          public String apply(ConfigChange input) {
            return input.getPropertyName();
          }
        });

    // 更新配置
    updateConfig(newConfigProperties, m_configRepository.getSourceType());
    // 清理配置缓存
    clearConfigCache();

    // 通知监听器
    this.fireConfigChange(new ConfigChangeEvent(m_namespace, changeMap));
    Tracer.logEvent("Apollo.Client.ConfigChanges", m_namespace);
  }

  private void updateConfig(Properties newConfigProperties, ConfigSourceType sourceType) {
    m_configProperties = newConfigProperties;
    m_sourceType = sourceType;
  }
}
