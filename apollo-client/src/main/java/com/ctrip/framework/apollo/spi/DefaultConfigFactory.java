package com.ctrip.framework.apollo.spi;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.PropertiesCompatibleConfigFile;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.internals.ConfigRepository;
import com.ctrip.framework.apollo.internals.DefaultConfig;
import com.ctrip.framework.apollo.internals.JsonConfigFile;
import com.ctrip.framework.apollo.internals.LocalFileConfigRepository;
import com.ctrip.framework.apollo.internals.PropertiesCompatibleFileConfigRepository;
import com.ctrip.framework.apollo.internals.PropertiesConfigFile;
import com.ctrip.framework.apollo.internals.RemoteConfigRepository;
import com.ctrip.framework.apollo.internals.TxtConfigFile;
import com.ctrip.framework.apollo.internals.XmlConfigFile;
import com.ctrip.framework.apollo.internals.YamlConfigFile;
import com.ctrip.framework.apollo.internals.YmlConfigFile;
import com.ctrip.framework.apollo.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认配置工厂实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public class DefaultConfigFactory implements ConfigFactory {

  private ConfigUtil m_configUtil;

  public DefaultConfigFactory() {
    m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
  }

  @Override
  public Config create(String namespace) {
    ConfigFileFormat format = determineFileFormat(namespace);
    // 判断属性文件是否兼容
    if (ConfigFileFormat.isPropertiesCompatible(format)) {
      return new DefaultConfig(namespace,
          createPropertiesCompatibleFileConfigRepository(namespace, format));
    }
    // 创建 ConfigRepository 对象
    // 创建 DefaultConfig 对象
    return new DefaultConfig(namespace, createLocalConfigRepository(namespace));
  }

  @Override
  public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
    // 创建 ConfigRepository 对象
    ConfigRepository configRepository = createLocalConfigRepository(namespace);
    // 创建对应的 ConfigFile 对象
    switch (configFileFormat) {
      case Properties:
        return new PropertiesConfigFile(namespace, configRepository);
      case XML:
        return new XmlConfigFile(namespace, configRepository);
      case JSON:
        return new JsonConfigFile(namespace, configRepository);
      case YAML:
        return new YamlConfigFile(namespace, configRepository);
      case YML:
        return new YmlConfigFile(namespace, configRepository);
      case TXT:
        return new TxtConfigFile(namespace, configRepository);
    }

    return null;
  }

  /**
   * 创建 LocalConfigRepository 对象
   *
   * @param namespace 指定的名称空间
   * @return 创建的LocalConfigRepository 对象
   */
  LocalFileConfigRepository createLocalConfigRepository(String namespace) {
    // 本地模式，使用 LocalFileConfigRepository 对象
    if (m_configUtil.isInLocalMode()) {
      log.warn(
          "==== Apollo is in local mode! Won't pull configs from remote server for namespace {} ! ====",
          namespace);
      return new LocalFileConfigRepository(namespace);
    }
    // 非本地模式，使用 LocalFileConfigRepository + RemoteConfigRepository对象
    return new LocalFileConfigRepository(namespace, createRemoteConfigRepository(namespace));
  }

  /**
   * 创建 RemoteConfigRepository 对象
   *
   * @param namespace 指定的名称空间
   * @return 创建的RemoteConfigRepository 对象
   */
  RemoteConfigRepository createRemoteConfigRepository(String namespace) {
    return new RemoteConfigRepository(namespace);
  }

  /**
   * 创建 PropertiesCompatibleFileConfigRepository对象
   *
   * @param namespace 指定的名称空间
   * @param format    配置文件格式枚举对象
   * @return 创建的PropertiesCompatibleFileConfigRepository对象
   */
  PropertiesCompatibleFileConfigRepository createPropertiesCompatibleFileConfigRepository(
      String namespace, ConfigFileFormat format) {
    // 实际的名称空间名称
    String actualNamespaceName = trimNamespaceFormat(namespace, format);
    // 兼容的配置文件
    PropertiesCompatibleConfigFile configFile = (PropertiesCompatibleConfigFile) ConfigService
        .getConfigFile(actualNamespaceName, format);

    return new PropertiesCompatibleFileConfigRepository(configFile);
  }


  /**
   * 获取名称空间的文件格式类型
   *
   * @param namespaceName 指定的名称空间名称
   * @return 指定名称空间名称的文件类型枚举
   */
  ConfigFileFormat determineFileFormat(String namespaceName) {
    // 对于格式不是properties的名称空间，文件扩展名必须存在，例如,application.yaml
    String lowerCase = namespaceName.toLowerCase();
    // 匹配
    for (ConfigFileFormat format : ConfigFileFormat.values()) {
      if (lowerCase.endsWith("." + format.getValue())) {
        return format;
      }
    }

    return ConfigFileFormat.Properties;
  }

  /**
   * 清空名称空间格式
   *
   * @param namespaceName 名称空间名称
   * @param format        配置文件格式枚举
   * @return 清空名称空间格式的名称空间名称
   */
  String trimNamespaceFormat(String namespaceName, ConfigFileFormat format) {
    // 文件类型
    String extension = "." + format.getValue();
    // 如果不以指定文件类型为后缀，直接返回这个名称空间
    if (!namespaceName.toLowerCase().endsWith(extension)) {
      return namespaceName;
    }

    // 如果存在后缀，去除后缀
    return namespaceName.substring(0, namespaceName.length() - extension.length());
  }

}
