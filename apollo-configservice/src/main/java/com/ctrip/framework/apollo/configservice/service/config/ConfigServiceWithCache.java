package com.ctrip.framework.apollo.configservice.service.config;

import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.message.Topics;
import com.ctrip.framework.apollo.biz.service.ReleaseMessageService;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.biz.utils.ReleaseMessageKeyGenerator;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.dto.ApolloNotificationMessages;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 配置查询服务，使用guava做本地缓存，带有本地缓存功能的实现
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public class ConfigServiceWithCache extends AbstractConfigService {

  /**
   * 默认的缓存失效时长 1h
   */
  private static final long DEFAULT_EXPIRED_AFTER_ACCESS_IN_MINUTES = TimeUnit.HOURS
      .toMinutes(1);
  // 日志内存的枚举
  private static final String TRACER_EVENT_CACHE_INVALIDATE = "ConfigCache.Invalidate";
  private static final String TRACER_EVENT_CACHE_LOAD = "ConfigCache.LoadFromDB";
  private static final String TRACER_EVENT_CACHE_LOAD_ID = "ConfigCache.LoadFromDBById";
  private static final String TRACER_EVENT_CACHE_GET = "ConfigCache.Get";
  private static final String TRACER_EVENT_CACHE_GET_ID = "ConfigCache.GetById";
  private static final Splitter STRING_SPLITTER =
      Splitter.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR).omitEmptyStrings();

  @Autowired
  private ReleaseService releaseService;

  @Autowired
  private ReleaseMessageService releaseMessageService;
  /**
   * 构建一个发布消息ID与配置发布记录对应的关系缓存
   */
  private LoadingCache<String, ConfigCacheEntry> configCache;

  private LoadingCache<Long, Optional<Release>> configIdCache;
  /**
   * 空的配置发布实体
   */
  private ConfigCacheEntry nullConfigCacheEntry;

  public ConfigServiceWithCache() {
    nullConfigCacheEntry = new ConfigCacheEntry(ConfigConsts.NOTIFICATION_ID_PLACEHOLDER, null);
  }

  /**
   * 初始化方法，在实例创建后调用
   */
  @PostConstruct
  void initialize() {
    // 初始化本地缓存
    configCache = CacheBuilder.newBuilder()
        .expireAfterAccess(DEFAULT_EXPIRED_AFTER_ACCESS_IN_MINUTES, TimeUnit.MINUTES)
        .build(new CacheLoader<String, ConfigCacheEntry>() {
          @Override
          public ConfigCacheEntry load(String key) {
            // appId + clusterName + namespaceName
            // 根据KEY切分命名空间信息集合
            List<String> namespaceInfo = STRING_SPLITTER.splitToList(key);
            if (namespaceInfo.size() != 3) {
              Tracer.logError(
                  new IllegalArgumentException(String.format("Invalid cache load key %s", key)));
              return nullConfigCacheEntry;
            }

            Transaction transaction = Tracer.newTransaction(TRACER_EVENT_CACHE_LOAD, key);
            try {
              // 最后配置发布消息信息
              ReleaseMessage latestReleaseMessage = releaseMessageService
                  .findLatestReleaseMessageForMessages(Lists.newArrayList(key));
              // 最后配置发布信息
              Release latestRelease = releaseService.findLatestActiveRelease(namespaceInfo.get(0),
                  namespaceInfo.get(1), namespaceInfo.get(2));

              transaction.setStatus(Transaction.SUCCESS);

              // 构建通知ID，当最后配置发布消息为null 通知ID=-1，标识无通知信息
              long notificationId = latestReleaseMessage == null ?
                  ConfigConsts.NOTIFICATION_ID_PLACEHOLDER : latestReleaseMessage.getId();

              if (notificationId == ConfigConsts.NOTIFICATION_ID_PLACEHOLDER
                  && latestRelease == null) {
                return nullConfigCacheEntry;
              }
              // 构建缓存实例， 通知ID-最后的配置发布记录
              return new ConfigCacheEntry(notificationId, latestRelease);
            } catch (Throwable ex) {
              transaction.setStatus(ex);
              throw ex;
            } finally {
              transaction.complete();
            }
          }
        });
    configIdCache = CacheBuilder.newBuilder()
        .expireAfterAccess(DEFAULT_EXPIRED_AFTER_ACCESS_IN_MINUTES, TimeUnit.MINUTES)
        .build(new CacheLoader<Long, Optional<Release>>() {
          @Override
          public Optional<Release> load(Long key) throws Exception {
            Transaction transaction = Tracer
                .newTransaction(TRACER_EVENT_CACHE_LOAD_ID, String.valueOf(key));
            try {
              // 配置发布消息
              Release release = releaseService.findActiveOne(key);

              transaction.setStatus(Transaction.SUCCESS);
              return Optional.ofNullable(release);
            } catch (Throwable ex) {
              transaction.setStatus(ex);
              throw ex;
            } finally {
              transaction.complete();
            }
          }
        });
  }

  @Override
  protected Release findActiveOne(long id, ApolloNotificationMessages clientMessages) {
    Tracer.logEvent(TRACER_EVENT_CACHE_GET_ID, String.valueOf(id));
    return configIdCache.getUnchecked(id).orElse(null);
  }

  @Override
  protected Release findLatestActiveRelease(String appId, String clusterName, String namespaceName,
      ApolloNotificationMessages clientMessages) {
    // 构建缓存KEY
    String key = ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName);

    Tracer.logEvent(TRACER_EVENT_CACHE_GET, key);
    // 缓存获取
    ConfigCacheEntry cacheEntry = configCache.getUnchecked(key);

    // 校验缓存是否已经失效，失效更新缓存
    if (clientMessages != null && clientMessages.has(key) &&
        clientMessages.get(key) > cacheEntry.getNotificationId()) {
      //invalidate the cache and try to load from db again
      invalidate(key);
      cacheEntry = configCache.getUnchecked(key);
    }

    return cacheEntry.getRelease();
  }

  /**
   * 校验缓存中的KEY
   *
   * @param key 发布消息IDcc
   */
  private void invalidate(String key) {
    configCache.invalidate(key);
    Tracer.logEvent(TRACER_EVENT_CACHE_INVALIDATE, key);
  }

  @Override
  public void handleMessage(ReleaseMessage message, String channel) {
    log.info("message received - channel: {}, message: {}", channel, message);
    if (!Topics.APOLLO_RELEASE_TOPIC.equals(channel) || Strings
        .isNullOrEmpty(message.getMessage())) {
      return;
    }

    try {
      // 校验缓存
      invalidate(message.getMessage());

      // 在缓存中获取当前KEY的值，用于更新缓存信息
      configCache.getUnchecked(message.getMessage());
    } catch (Throwable ex) {
      //ignore
    }
  }

  /**
   * 发布消息ID与发布记录对应关系实体
   */
  @AllArgsConstructor
  @Getter
  private static class ConfigCacheEntry {

    /**
     * 通知id
     */
    private final long notificationId;
    /**
     * 发布信息
     */
    private final Release release;
  }
}
