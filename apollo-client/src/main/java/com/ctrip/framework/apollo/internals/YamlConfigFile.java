package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.PropertiesCompatibleConfigFile;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.ctrip.framework.apollo.util.yaml.YamlParser;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * 类型为 .yaml 的 ConfigFile 实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public class YamlConfigFile extends PlainTextConfigFile implements PropertiesCompatibleConfigFile {

  /**
   * 缓存的Properties
   */
  private volatile Properties cachedProperties;

  /**
   * 构建Yaml配置文件对象
   *
   * @param namespace        名称空间
   * @param configRepository 配置存储库
   */
  public YamlConfigFile(String namespace, ConfigRepository configRepository) {
    super(namespace, configRepository);
    tryTransformToProperties();
  }

  @Override
  public ConfigFileFormat getConfigFileFormat() {
    return ConfigFileFormat.YAML;
  }

  @Override
  protected void update(Properties newProperties) {
    // 更新属性
    super.update(newProperties);
    tryTransformToProperties();
  }

  @Override
  public Properties asProperties() {
    // 为空就试图转换为Properties
    if (cachedProperties == null) {
      transformToProperties();
    }
    // 返回转换后的Propertes对象
    return cachedProperties;
  }

  /**
   * 试图转换为Properties
   *
   * @return 转换成功，返回true,否则，false
   */
  private boolean tryTransformToProperties() {
    try {
      transformToProperties();
      return true;
    } catch (Throwable ex) {
      Tracer.logEvent("ApolloConfigException", ExceptionUtil.getDetailMessage(ex));
      log.warn("yaml to properties failed, reason: {}", ExceptionUtil.getDetailMessage(ex));
    }
    return false;
  }

  /**
   * 转换为Properties
   */
  private synchronized void transformToProperties() {
    cachedProperties = toProperties();
  }

  /**
   * 转换为Properties
   *
   * @return 返回的Properties对象
   */
  private Properties toProperties() {
    // 没有内容直接返回属性实例
    if (!this.hasContent()) {
      return propertiesFactory.getPropertiesInstance();
    }

    try {
      // 获取属性实例，并添加内容
      return ApolloInjector.getInstance(YamlParser.class).yamlToProperties(getContent());
    } catch (Throwable ex) {
      ApolloConfigException exception = new ApolloConfigException(
          "Parse yaml file content failed for namespace: " + namespace, ex);
      Tracer.logError(exception);
      throw exception;
    }
  }
}
