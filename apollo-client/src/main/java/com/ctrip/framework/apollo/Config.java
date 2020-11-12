package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.google.common.base.Function;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * 配置接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface Config {

  /**
   * 使用给定的键返回属性值，如果键不存在，则返回{@code defaultValue}。
   *
   * @param key          属性名称
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @return 属性值
   */
  String getProperty(String key, String defaultValue);

  /**
   * 使用给定的键返回整型属性值，如果键不存在，则返回{@code defaultValue}。
   *
   * @param key          属性名称
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @return integer属性值
   */
  Integer getIntProperty(String key, Integer defaultValue);

  /**
   * 使用给定的键返回长整型属性值，如果键不存在，则返回{@code defaultValue}。
   *
   * @param key          属性名称
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @return long属性值
   */
  Long getLongProperty(String key, Long defaultValue);

  /**
   * 使用给定的键返回短整型属性值，如果键不存在，则返回{@code defaultValue}。
   *
   * @param key          属性名称
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @return short属性值
   */
  Short getShortProperty(String key, Short defaultValue);

  /**
   * 使用给定的键返回浮点型属性值，如果键不存在，则返回{@code defaultValue}。
   *
   * @param key          属性名称
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @return float属性值
   */
  Float getFloatProperty(String key, Float defaultValue);

  /**
   * 使用给定的键返回双精度浮点型属性值，如果键不存在，则返回{@code defaultValue}。
   *
   * @param key          属性名称
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @return double属性值
   */
  Double getDoubleProperty(String key, Double defaultValue);

  /**
   * 使用给定的键返回字节型属性值，如果键不存在，则返回{@code defaultValue}。
   *
   * @param key          属性名称
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @return byte属性值
   */
  Byte getByteProperty(String key, Byte defaultValue);

  /**
   * 使用给定的键返回布尔型属性值，如果键不存在，则返回{@code defaultValue}。
   *
   * @param key          属性名称
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @return boolean属性值
   */
  Boolean getBooleanProperty(String key, Boolean defaultValue);

  /**
   * 使用给定的键返回数组类型属性值，如果键不存在，则返回{@code defaultValue}。.
   *
   * @param key          属性名称
   * @param delimiter    the delimiter regex
   * @param defaultValue 找不到键或发生任何错误时的默认值
   */
  String[] getArrayProperty(String key, String delimiter, String[] defaultValue);

  /**
   * 返回具有给定名称的日期属性值，如果名称不存在，则返回{@code defaultValue}。将尝试用语言环境.US格式如下： yyyy-MM-dd HH:MM:ss
   * SSS，年-月-日HH:MM:ssyyyy-MM-dd
   *
   * @param key          属性名称
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @return date属性值
   */
  Date getDateProperty(String key, Date defaultValue);

  /**
   * 返回具有给定名称的日期属性值，如果名称不存在，则返回{@code defaultValue}。将使用指定的格式分析日期语言环境.US
   *
   * @param key          属性名称
   * @param format       日期格式，更多信息请参见{@linkjava.text.SimpleDateFormat}
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @return date属性值
   */
  Date getDateProperty(String key, String format, Date defaultValue);

  /**
   * 返回具有给定名称的日期属性值，如果名称不返回，则返回{@code defaultValue}存在。
   *
   * @param key          属性名称
   * @param format       日期格式，更多信息请参见{@linkjava.text.SimpleDateFormat}
   * @param locale       要使用的区域设置
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @return date属性值
   */
  Date getDateProperty(String key, String format, Locale locale, Date defaultValue);

  /**
   * 返回具有给定键的枚举属性值，如果该键不存在，则返回{@code defaultValue}。
   *
   * @param key          属性名称
   * @param enumType     the enum class
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @param <T>          枚举泛型
   * @return 枚举属性值
   */
  <T extends Enum<T>> T getEnumProperty(String key, Class<T> enumType, T defaultValue);

  /**
   * 返回具有给定名称的duration属性值（以毫秒为单位），如果名称不存在，则返回{@code defaultValue}。 请注意，格式应符合以下示例（不区分大小写）。示例：
   * <pre>
   *   “123MS”--解析为“123毫秒”
   *   “20S”--解析为“20秒”
   *   “15M”--解析为“15分钟”（其中一分钟为60秒）
   *   “10H”--解析为“10小时”（其中一小时为3600秒）
   *   “2D”--解析为“2天”（其中一天24小时或86400秒）
   *   “2D3H4M5S123MS”--解析为“2天、3小时、4分钟、5秒和123毫秒”
   *  </pre>
   *
   * @param key          属性名称
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @return the parsed property value(in milliseconds)
   */
  long getDurationProperty(String key, long defaultValue);

  /**
   * 将配置变更的监听器添加到此配置实例，当此名称空间中的任何键发生更改时，将收到通知。
   *
   * @param listener 配置变更的监听器
   */
  void addChangeListener(ConfigChangeListener listener);

  /**
   * 将配置变更的监听器添加到此配置实例中，仅当此名称空间中的任何感兴趣的键发生更改时才会收到通知。
   *
   * @param listener       配置变更的监听器
   * @param interestedKeys the keys interested by the listener
   * @since 1.0.0
   */
  void addChangeListener(ConfigChangeListener listener, Set<String> interestedKeys);

  /**
   * 将配置变更的监听器添加到此配置实例中，仅当此名称空间中的任何感兴趣的键发生更改时才会收到通知
   *
   * @param listener              配置变更的监听器
   * @param interestedKeys        配置变更的监听器感兴趣的键
   * @param interestedKeyPrefixes 配置变更的监听器感兴趣的键前缀，例如“spring.”表示{@code listener}对以“spring”开头的键感兴趣，例如"spring.banner",
   *                              "spring.jpa",“application”表示{@code listener}对以“application”开头的键感兴趣，例如“applicationName”application.port“，等等。
   *                              更多细节， see {@link com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener#interestedKeyPrefixes()}
   *                              and {@link java.lang.String#startsWith(String)}
   * @since 1.3.0
   */
  void addChangeListener(ConfigChangeListener listener, Set<String> interestedKeys,
      Set<String> interestedKeyPrefixes);

  /**
   * 移除配置变更的监听器
   *
   * @param listener 待移除的指定配置变更的监听器
   * @return 如果找到并删除了特定的配置更改侦听器，则为true
   * @since 1.1.0
   */
  boolean removeChangeListener(ConfigChangeListener listener);

  /**
   * 获取属性名称列表
   *
   * @return 属性名称列表
   */
  Set<String> getPropertyNames();

  /**
   * 使用给定的键返回用户定义的属性值，如果键不存在，则返回defaultValue
   *
   * @param key          属性名称
   * @param function     转换{@link Function}。从字符串到用户定义类型 the transform {@link Function}.,w,
   * @param defaultValue 找不到键或发生任何错误时的默认值
   * @param <T>          user-defined type
   * @return 属性值
   * @since 1.1.0
   */
  <T> T getProperty(String key, Function<String, T> function, T defaultValue);

  /**
   * 返回配置的源类型，即从哪里加载配置
   *
   * @return 配置源的类型
   * @since 1.1.0
   */
  ConfigSourceType getSourceType();
}
