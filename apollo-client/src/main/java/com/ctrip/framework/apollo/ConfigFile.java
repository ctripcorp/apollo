package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.enums.ConfigSourceType;

/**
 * 配置文件接口.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigFile {

  /**
   * 获取名称空间的文件内容
   *
   * @return 文件内容，如果没有内容，返回{@code null}
   */
  String getContent();

  /**
   * 配置文件是否有任何内容
   *
   * @return 如果有内容，true,否则，false.
   */
  boolean hasContent();

  /**
   * 获取此配置文件实例的名称空间
   *
   * @return 名称空间
   */
  String getNamespace();

  /**
   * 获取此配置文件实例的文件格式
   *
   * @return 当前配置文件的格式枚举
   */
  ConfigFileFormat getConfigFileFormat();

  /**
   * 添加配置文件变更监听器.
   *
   * @param listener 配置文件变更监听器
   */
  void addChangeListener(ConfigFileChangeListener listener);

  /**
   * 移除配置文件变更监听器
   *
   * @param listener 要删除的特定配置文件变更监听器
   * @return 如果找到并删除了特定的配置更改侦听器，则为true
   */
  boolean removeChangeListener(ConfigFileChangeListener listener);

  /**
   * 返回配置源的类型
   *
   * @return 配置源的类型
   */
  ConfigSourceType getSourceType();
}
