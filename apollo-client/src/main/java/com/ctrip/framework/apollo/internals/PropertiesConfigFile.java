package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.utils.PropertiesUtil;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * .properties 的 ConfigFile 实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class PropertiesConfigFile extends AbstractConfigFile {

  /**
   * 配置字符串缓存
   * <p>因为 Properties 是 KV 数据结构，需要将多条 KV 拼接成一个字符串，进行缓存到 m_contentCache 中</p>
   */
  protected AtomicReference<String> m_contentCache;

  public PropertiesConfigFile(String namespace,
      ConfigRepository configRepository) {
    super(namespace, configRepository);
    m_contentCache = new AtomicReference<>();
  }

  @Override
  protected void update(Properties newProperties) {
    // 设置【新】Properties
    m_configProperties.set(newProperties);
    // 清空缓存
    m_contentCache.set(null);
  }

  @Override
  public String getContent() {
    // 更新到缓存
    if (m_contentCache.get() == null) {
      m_contentCache.set(doGetContent());
    }
    // 从缓存中，获得配置字符串
    return m_contentCache.get();
  }

  String doGetContent() {
    if (!this.hasContent()) {
      return null;
    }

    try {
      // 拼接 KV 属性，成字符串
      return PropertiesUtil.toString(m_configProperties.get());
    } catch (Throwable ex) {
      ApolloConfigException exception = new ApolloConfigException(
          String.format("Parse properties file content failed for namespace: %s, cause: %s",
              namespace, ExceptionUtil.getDetailMessage(ex)));
      Tracer.logError(exception);
      throw exception;
    }
  }

  @Override
  public boolean hasContent() {
    // 判断是否有内容
    return m_configProperties.get() != null && !m_configProperties.get().isEmpty();
  }

  @Override
  public ConfigFileFormat getConfigFileFormat() {
    // 配置文件格式枚举
    return ConfigFileFormat.Properties;
  }
}
