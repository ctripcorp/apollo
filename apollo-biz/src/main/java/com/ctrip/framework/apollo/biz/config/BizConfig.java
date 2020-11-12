package com.ctrip.framework.apollo.biz.config;

import com.ctrip.framework.apollo.biz.service.BizDBPropertySource;
import com.ctrip.framework.apollo.common.config.RefreshableConfig;
import com.ctrip.framework.apollo.common.config.RefreshablePropertySource;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * 业务配置
 */
@Component
public class BizConfig extends RefreshableConfig {

  /**
   * 配置项Key默认的长度为128
   */
  private static final int DEFAULT_ITEM_KEY_LENGTH = 128;
  /**
   * 配置项Value默认限制值为20000
   */
  private static final int DEFAULT_ITEM_VALUE_LENGTH = 20000;
  /**
   * 应用名称空间缓存重建间隔，默认为1分钟
   */
  private static final Long DEFAULT_APPNAMESPACE_CACHE_REBUILD_INTERVAL = TimeUnit.MINUTES
      .toSeconds(1);
  /**
   * 灰度发布规则扫描间隔值，默认为1分钟
   */
  private static final Long DEFAULT_GRAY_RELEASE_RULE_SCAN_INTERVAL = TimeUnit.MINUTES
      .toSeconds(1);
  /**
   * 应用名称空间缓存扫描间隔，默认1秒
   */
  private static final Long DEFAULT_APPNAMESPACE_CACHE_SCAN_INTERVAL = TimeUnit.SECONDS
      .toSeconds(1);
  /**
   * 默认访问密钥缓存扫描间隔，默认1秒
   */
  private static final Long DEFAULT_ACCESSKEY_CACHE_SCAN_INTERVAL = TimeUnit.SECONDS.toSeconds(1);
  /**
   * 默认访问密钥缓存重建间隔，默认1分钟
   */
  private static final Long DEFAULT_ACCESSKEY_CACHE_REBUILD_INTERVAL = TimeUnit.MINUTES
      .toSeconds(1);
  /**
   * 发布消息缓存扫描间隔，默认1秒
   */
  private static final Long DEFAULT_RELEASE_MESSAGE_CACHE_SCAN_INTERVAL = TimeUnit.SECONDS
      .toSeconds(1);
  /**
   * 发布消息扫描间隔毫秒值，默认1秒
   */
  private static final Long DEFAULT_RELEASE_MESSAGE_SCAN_INTERVAL_IN_MS = TimeUnit.SECONDS
      .toMillis(1);
  /**
   * 发布消息通知批处理量，默认100
   */
  private static final int DEFAULT_RELEASE_MESSAGE_NOTIFICATION_BATCH = 100;

  /**
   * 发布消息通知批处理间隔，默认100毫秒
   */
  private static final Long DEFAULT_RELEASE_MESSAGE_NOTIFICATION_BATCH_INTERVAL_IN_MILLI = TimeUnit.MILLISECONDS
      .toMillis(100);
  /**
   * 长轮询超时时间
   */
  private static final Long DEFAULT_LONG_POLLING_TIMEOUT = TimeUnit.MINUTES.toSeconds(1);

  private static final Gson GSON = new Gson();
  /**
   * 名称空间Value长度类型引用<namespaceId, 最大长度限制值>
   */
  private static final Type namespaceValueLengthOverrideTypeReference =
      new TypeToken<Map<Long, Integer>>() {
      }.getType();


  private final BizDBPropertySource propertySource;

  public BizConfig(final BizDBPropertySource propertySource) {
    this.propertySource = propertySource;
  }

  @Override
  protected List<RefreshablePropertySource> getRefreshablePropertySources() {
    return Collections.singletonList(propertySource);
  }

  /**
   * 获取Eureka服务Url
   *
   * @return Eureka服务Url列表(逗号分割)
   */
  public List<String> eurekaServiceUrls() {
    String configuration = getValue("eureka.service.url", "");
    if (Strings.isNullOrEmpty(configuration)) {
      return Collections.emptyList();
    }

    // 逗号分割
    return splitter.splitToList(configuration);
  }

  /**
   * 获取灰度发布规则扫描间隔值
   *
   * @return 灰度发布规则扫描间隔值
   */
  public int grayReleaseRuleScanInterval() {
    int interval = getIntProperty("apollo.gray-release-rule-scan.interval",
        DEFAULT_GRAY_RELEASE_RULE_SCAN_INTERVAL.intValue());
    return checkInt(interval, 1, Integer.MAX_VALUE,
        DEFAULT_GRAY_RELEASE_RULE_SCAN_INTERVAL.intValue());
  }

  /**
   * 获取长轮询超时时间
   *
   * @return 长轮询超时时间
   */
  public long longPollingTimeoutInMilli() {
    int timeout = getIntProperty("long.polling.timeout", DEFAULT_LONG_POLLING_TIMEOUT.intValue());
    // java client's long polling timeout is 90 seconds, so server side long polling timeout must be less than 90
    return 1000 * checkInt(timeout, 1, 90, DEFAULT_LONG_POLLING_TIMEOUT.intValue());
  }

  /**
   * 获取配置项 key 最大长度限制值
   *
   * @return 配置项 key 最大长度限制值
   */
  public int itemKeyLengthLimit() {
    int limit = getIntProperty("item.key.length.limit", DEFAULT_ITEM_KEY_LENGTH);
    return checkInt(limit, 5, Integer.MAX_VALUE, DEFAULT_ITEM_KEY_LENGTH);
  }

  /**
   * 获取配置项 value 最大长度限制值
   *
   * @return 配置项 value 最大长度限制值
   */
  public int itemValueLengthLimit() {
    int limit = getIntProperty("item.value.length.limit", DEFAULT_ITEM_VALUE_LENGTH);
    // 检查范围
    return checkInt(limit, 5, Integer.MAX_VALUE, DEFAULT_ITEM_VALUE_LENGTH);
  }

  /**
   * 获取名称空间的配置项value最大长度限制
   *
   * @return 名称空间的配置项value最大长度限制信息<namespaceId, 最大长度限制值>
   */
  public Map<Long, Integer> namespaceValueLengthLimitOverride() {
    String namespaceValueLengthOverrideString = getValue("namespace.value.length.limit.override");
    Map<Long, Integer> namespaceValueLengthOverride = Maps.newHashMap();
    if (!Strings.isNullOrEmpty(namespaceValueLengthOverrideString)) {
      namespaceValueLengthOverride = GSON.fromJson(namespaceValueLengthOverrideString,
          namespaceValueLengthOverrideTypeReference);
    }
    return namespaceValueLengthOverride;
  }

  /**
   * 是否关闭名称空间锁
   *
   * @return true, 是，否则,false(默认开启名称空间锁)
   */
  public boolean isNamespaceLockSwitchOff() {
    return !getBooleanProperty("namespace.lock.switch", false);
  }

  /**
   * ctrip config
   **/
  public String cloggingUrl() {
    return getValue("clogging.server.url");
  }

  public String cloggingPort() {
    return getValue("clogging.server.port");
  }

  /**
   * 获取应用名称空间缓存扫描间隔
   *
   * @return 应用名称空间缓存扫描间隔
   */
  public int appNamespaceCacheScanInterval() {
    int interval = getIntProperty("apollo.app-namespace-cache-scan.interval",
        DEFAULT_APPNAMESPACE_CACHE_SCAN_INTERVAL.intValue());
    return checkInt(interval, 1, Integer.MAX_VALUE,
        DEFAULT_APPNAMESPACE_CACHE_SCAN_INTERVAL.intValue());
  }

  /**
   * 获取应用名称空间缓存扫描间隔时间单位
   *
   * @return 应用名称空间缓存扫描间隔时间单位（秒）
   */
  public TimeUnit appNamespaceCacheScanIntervalTimeUnit() {
    return TimeUnit.SECONDS;
  }

  /**
   * 获取应用名称空间缓存重建间隔
   *
   * @return 应用名称空间缓存重建间隔
   */
  public int appNamespaceCacheRebuildInterval() {
    int interval = getIntProperty("apollo.app-namespace-cache-rebuild.interval",
        DEFAULT_APPNAMESPACE_CACHE_REBUILD_INTERVAL.intValue());
    return checkInt(interval, 1, Integer.MAX_VALUE,
        DEFAULT_APPNAMESPACE_CACHE_REBUILD_INTERVAL.intValue());
  }

  /**
   * 获取应用名称空间缓存重建间隔时间单位
   *
   * @return 密钥缓存重建间隔时间单位（秒）
   */
  public TimeUnit appNamespaceCacheRebuildIntervalTimeUnit() {
    return TimeUnit.SECONDS;
  }

  /**
   * 获取访问密钥缓存扫描间隔
   *
   * @return 访问密钥缓存扫描间隔
   */
  public int accessKeyCacheScanInterval() {
    int interval = getIntProperty("apollo.access-key-cache-scan.interval",
        DEFAULT_ACCESSKEY_CACHE_SCAN_INTERVAL.intValue());
    return checkInt(interval, 1, Integer.MAX_VALUE,
        DEFAULT_ACCESSKEY_CACHE_SCAN_INTERVAL.intValue());
  }

  /**
   * 获取获取访问密钥缓存扫描间隔时间单位
   *
   * @return 密钥缓存重建间隔时间单位（秒）
   */
  public TimeUnit accessKeyCacheScanIntervalTimeUnit() {
    return TimeUnit.SECONDS;
  }

  /**
   * 获取访问密钥缓存重建间隔
   *
   * @return 访问密钥缓存重建间隔
   */
  public int accessKeyCacheRebuildInterval() {
    int interval = getIntProperty("apollo.access-key-cache-rebuild.interval",
        DEFAULT_ACCESSKEY_CACHE_REBUILD_INTERVAL.intValue());
    return checkInt(interval, 1, Integer.MAX_VALUE,
        DEFAULT_ACCESSKEY_CACHE_REBUILD_INTERVAL.intValue());
  }

  /**
   * 获取访问密钥缓存重建间隔时间单位
   *
   * @return 密钥缓存重建间隔时间单位（秒）
   */
  public TimeUnit accessKeyCacheRebuildIntervalTimeUnit() {
    return TimeUnit.SECONDS;
  }

  /**
   * 获取发布消息缓存扫描间隔
   *
   * @return 发布消息缓存扫描间隔
   */
  public int releaseMessageCacheScanInterval() {
    int interval = getIntProperty("apollo.release-message-cache-scan.interval",
        DEFAULT_RELEASE_MESSAGE_CACHE_SCAN_INTERVAL.intValue());
    return checkInt(interval, 1, Integer.MAX_VALUE,
        DEFAULT_RELEASE_MESSAGE_CACHE_SCAN_INTERVAL.intValue());
  }

  public TimeUnit releaseMessageCacheScanIntervalTimeUnit() {
    return TimeUnit.SECONDS;
  }

  /**
   * 获取发布消息扫描间隔毫秒值
   *
   * @return 发布消息扫描间隔毫秒值
   */
  public int releaseMessageScanIntervalInMilli() {
    int interval = getIntProperty("apollo.message-scan.interval",
        DEFAULT_RELEASE_MESSAGE_SCAN_INTERVAL_IN_MS.intValue());
    return checkInt(interval, 100, Integer.MAX_VALUE,
        DEFAULT_RELEASE_MESSAGE_SCAN_INTERVAL_IN_MS.intValue());
  }

  /**
   * 获取发布消息通知批处理量
   *
   * @return 发布消息通知批处理量
   */
  public int releaseMessageNotificationBatch() {
    int batch = getIntProperty("apollo.release-message.notification.batch",
        DEFAULT_RELEASE_MESSAGE_NOTIFICATION_BATCH);
    return checkInt(batch, 1, Integer.MAX_VALUE, DEFAULT_RELEASE_MESSAGE_NOTIFICATION_BATCH);
  }

  /**
   * 发布消息通知批处理间隔
   *
   * @return 发布消息通知批处理间隔
   */
  public int releaseMessageNotificationBatchIntervalInMilli() {
    int interval = getIntProperty("apollo.release-message.notification.batch.interval",
        DEFAULT_RELEASE_MESSAGE_NOTIFICATION_BATCH_INTERVAL_IN_MILLI.intValue());
    return checkInt(interval, 10, Integer.MAX_VALUE,
        DEFAULT_RELEASE_MESSAGE_NOTIFICATION_BATCH_INTERVAL_IN_MILLI.intValue());
  }

  /**
   * 是否开启配置缓存
   *
   * @return true, 开启，否则，false
   */
  public boolean isConfigServiceCacheEnabled() {
    return getBooleanProperty("config-service.cache.enabled", false);
  }

  /**
   * 检查值是否在指定范围内
   *
   * @param value        值
   * @param min          最小值
   * @param max          最大值
   * @param defaultValue 默认值
   * @return true, 返回这个值，false,返回默认值
   */
  int checkInt(int value, int min, int max, int defaultValue) {
    if (value >= min && value <= max) {
      return value;
    }
    return defaultValue;
  }

  /**
   * 是否开启系统服务访问权限控制
   *
   * @return true, 开启，否则，false
   */
  public boolean isAdminServiceAccessControlEnabled() {
    return getBooleanProperty("admin-service.access.control.enabled", false);
  }

  /**
   * 允许访问apollo-admin服务的访问token列表（逗号分割）
   *
   * @return 访问token列表
   */
  public String getAdminServiceAccessTokens() {
    return getValue("admin-service.access.tokens");
  }
}
