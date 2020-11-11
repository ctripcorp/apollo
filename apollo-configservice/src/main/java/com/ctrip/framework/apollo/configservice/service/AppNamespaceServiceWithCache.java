package com.ctrip.framework.apollo.configservice.service;

import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.repository.AppNamespaceRepository;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.configservice.wrapper.CaseInsensitiveMapWrapper;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 应用名称空间服务缓存，缓存 AppNamespace 的 Service 实现类。通过将 AppNamespace 缓存在内存中，提高查询性能
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
@Service
public class AppNamespaceServiceWithCache implements InitializingBean {

  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
      .skipNulls();
  private final AppNamespaceRepository appNamespaceRepository;
  private final BizConfig bizConfig;

  /**
   * 扫描周期
   */
  private int scanInterval;
  /**
   * 扫描周期时间单位
   */
  private TimeUnit scanIntervalTimeUnit;
  /**
   * 重建周期
   */
  private int rebuildInterval;
  /**
   * 重建周期单位
   */
  private TimeUnit rebuildIntervalTimeUnit;
  /**
   * 定时任务 ExecutorService
   */
  private ScheduledExecutorService scheduledExecutorService;
  /**
   * 最后扫描到的 AppNamespace 的编号
   */
  private long maxIdScanned;

  /**
   * 公用类型的 AppNamespace 的缓存(store namespaceName -> AppNamespace)
   */
  private CaseInsensitiveMapWrapper<AppNamespace> publicAppNamespaceCache;

  /**
   * App 下的 AppNamespace 的缓存（store appId+namespaceName -> AppNamespace）
   */
  private CaseInsensitiveMapWrapper<AppNamespace> appNamespaceCache;

  /**
   * AppNamespace 的缓存(store id -> AppNamespace)
   */
  private Map<Long, AppNamespace> appNamespaceIdCache;

  public AppNamespaceServiceWithCache(
      final AppNamespaceRepository appNamespaceRepository,
      final BizConfig bizConfig) {
    this.appNamespaceRepository = appNamespaceRepository;
    this.bizConfig = bizConfig;
    initialize();
  }

  private void initialize() {
    maxIdScanned = 0;
    // 创建缓存对象
    publicAppNamespaceCache = new CaseInsensitiveMapWrapper<>(Maps.newConcurrentMap());
    appNamespaceCache = new CaseInsensitiveMapWrapper<>(Maps.newConcurrentMap());
    appNamespaceIdCache = Maps.newConcurrentMap();
    // 创建 ScheduledExecutorService 对象，大小为 1 。
    scheduledExecutorService = Executors.newScheduledThreadPool(1, ApolloThreadFactory
        .create("AppNamespaceServiceWithCache", true));
  }

  /**
   * 获得 AppNamespace 对象
   *
   * @param appId         应用id
   * @param namespaceName 应用名称空间名称
   * @return 应用名称空间
   */
  public AppNamespace findByAppIdAndNamespace(String appId, String namespaceName) {
    Preconditions.checkArgument(!StringUtils.isContainEmpty(appId, namespaceName),
        "appId and namespaceName must not be empty");
    return appNamespaceCache.get(STRING_JOINER.join(appId, namespaceName));
  }

  /**
   * 获得 AppNamespace 对象
   *
   * @param appId          应用id
   * @param namespaceNames 应用名称空间名称列表
   * @return 应用名称空间列表
   */
  public List<AppNamespace> findByAppIdAndNamespaces(String appId, Set<String> namespaceNames) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(appId), "appId must not be null");
    if (CollectionUtils.isEmpty(namespaceNames)) {
      return Collections.emptyList();
    }
    List<AppNamespace> result = Lists.newArrayList();
    // 循环获取
    for (String namespaceName : namespaceNames) {
      AppNamespace appNamespace = appNamespaceCache.get(STRING_JOINER.join(appId, namespaceName));
      if (appNamespace != null) {
        result.add(appNamespace);
      }
    }
    return result;
  }

  /**
   * 获得公用类型的 AppNamespace 对象
   *
   * @param namespaceName 名称空间名称
   * @return 应用名称空间
   */
  public AppNamespace findPublicNamespaceByName(String namespaceName) {
    Preconditions
        .checkArgument(!Strings.isNullOrEmpty(namespaceName), "namespaceName must not be empty");
    return publicAppNamespaceCache.get(namespaceName);
  }

  /**
   * 获得公用类型的 AppNamespace 对象
   *
   * @param namespaceNames 名称空间名称列表
   * @return 应用名称空间
   */
  public List<AppNamespace> findPublicNamespacesByNames(Set<String> namespaceNames) {
    if (CollectionUtils.isEmpty(namespaceNames)) {
      return Collections.emptyList();
    }
    // 循环获取
    List<AppNamespace> result = Lists.newArrayList();
    for (String namespaceName : namespaceNames) {
      AppNamespace appNamespace = publicAppNamespaceCache.get(namespaceName);
      if (appNamespace != null) {
        result.add(appNamespace);
      }
    }
    return result;
  }

  @Override
  public void afterPropertiesSet() {
    // 从 ServerConfig 中，读取定时任务的周期配置
    populateDataBaseInterval();
    // 全量初始化 AppNamespace 缓存，阻止启动过程，直到加载完成
    scanNewAppNamespaces();
    scheduledExecutorService.scheduleAtFixedRate(() -> {
      Transaction transaction = Tracer.newTransaction("Apollo.AppNamespaceServiceWithCache",
          "rebuildCache");
      try {
        this.updateAndDeleteCache();
        transaction.setStatus(Transaction.SUCCESS);
      } catch (Throwable ex) {
        transaction.setStatus(ex);
        log.error("Rebuild cache failed", ex);
      } finally {
        transaction.complete();
      }
    }, rebuildInterval, rebuildInterval, rebuildIntervalTimeUnit);
    scheduledExecutorService.scheduleWithFixedDelay(this::scanNewAppNamespaces, scanInterval,
        scanInterval, scanIntervalTimeUnit);
  }

  /**
   * 全量初始化 AppNamespace 缓存
   */
  private void scanNewAppNamespaces() {
    Transaction transaction = Tracer.newTransaction("Apollo.AppNamespaceServiceWithCache",
        "scanNewAppNamespaces");
    try {
      this.loadNewAppNamespaces();
      transaction.setStatus(Transaction.SUCCESS);
    } catch (Throwable ex) {
      transaction.setStatus(ex);
      log.error("Load new app namespaces failed", ex);
    } finally {
      transaction.complete();
    }
  }

  //for those new app namespaces

  /**
   * 加载新的 AppNamespace
   */
  private void loadNewAppNamespaces() {
    boolean hasMore = true;
    // 循环，直到无新的 AppNamespace
    while (hasMore && !Thread.currentThread().isInterrupted()) {
      //current batch is 500
      // 获得大于 maxIdScanned 的 500 条 AppNamespace 记录，按照 id 升序
      List<AppNamespace> appNamespaces = appNamespaceRepository
          .findFirst500ByIdGreaterThanOrderByIdAsc(maxIdScanned);
      if (CollectionUtils.isEmpty(appNamespaces)) {
        break;
      }
      // 合并到 AppNamespace 缓存中
      mergeAppNamespaces(appNamespaces);
      // 获得新的 maxIdScanned ，取最后一条记录
      int scanned = appNamespaces.size();
      maxIdScanned = appNamespaces.get(scanned - 1).getId();
      // 若拉取不足 500 条，说明无新消息了
      hasMore = scanned == 500;
      log.info("Loaded {} new app namespaces with startId {}", scanned, maxIdScanned);
    }
  }

  /**
   * 合并到 AppNamespace 缓存中
   *
   * @param appNamespaces 应用名称空间列表
   */
  private void mergeAppNamespaces(List<AppNamespace> appNamespaces) {
    for (AppNamespace appNamespace : appNamespaces) {
      // 添加到 `appNamespaceCache` 中updateAndDeleteCache, appNamespace);
      // 添加到 `appNamespaceIdCache`
      appNamespaceIdCache.put(appNamespace.getId(), appNamespace);
      // 若是公用类型，则添加到 `publicAppNamespaceCache` 中
      if (appNamespace.isPublic()) {
        publicAppNamespaceCache.put(appNamespace.getName(), appNamespace);
      }
    }
  }

  /**
   * 更新或删除的应用名称空间
   */
  private void updateAndDeleteCache() {
    // 从缓存中，获得所有的 AppNamespace 编号集合
    List<Long> ids = Lists.newArrayList(appNamespaceIdCache.keySet());
    if (CollectionUtils.isEmpty(ids)) {
      return;
    }

    // 每 500 一批，从数据库中查询最新的 AppNamespace 信息
    List<List<Long>> partitionIds = Lists.partition(ids, 500);
    for (List<Long> toRebuild : partitionIds) {
      Iterable<AppNamespace> appNamespaces = appNamespaceRepository.findAllById(toRebuild);

      if (appNamespaces == null) {
        continue;
      }

      // 处理更新的情况
      Set<Long> foundIds = handleUpdatedAppNamespaces(appNamespaces);

      // 处理删除的情况
      handleDeletedAppNamespaces(Sets.difference(Sets.newHashSet(toRebuild), foundIds));
    }
  }

  /**
   * 更新应用名称空间处理
   *
   * @param appNamespaces 应用名称空间
   * @return 更新过的名称空间id列表
   */
  private Set<Long> handleUpdatedAppNamespaces(Iterable<AppNamespace> appNamespaces) {
    Set<Long> foundIds = Sets.newHashSet();
    for (AppNamespace appNamespace : appNamespaces) {
      foundIds.add(appNamespace.getId());
      // 获得缓存中的 AppNamespace 对象
      AppNamespace thatInCache = appNamespaceIdCache.get(appNamespace.getId());
      // 从 DB 中查询到的 AppNamespace 的更新时间更大，才认为是更新
      if (thatInCache != null && appNamespace.getDataChangeLastModifiedTime().after(thatInCache
          .getDataChangeLastModifiedTime())) {
        // 添加到 appNamespaceIdCache 中
        appNamespaceIdCache.put(appNamespace.getId(), appNamespace);
        // 添加到 appNamespaceCache 中
        String oldKey = assembleAppNamespaceKey(thatInCache);
        String newKey = assembleAppNamespaceKey(appNamespace);
        appNamespaceCache.put(newKey, appNamespace);

        //in case appId or namespaceName changes
        // 当appId或namespaceName 发生改变的情况，将老的移除出 appNamespaceCache
        if (!newKey.equals(oldKey)) {
          appNamespaceCache.remove(oldKey);
        }

        // 添加到 publicAppNamespaceCache 中，新的是公用类型
        if (appNamespace.isPublic()) {
          // 添加到 publicAppNamespaceCache 中
          publicAppNamespaceCache.put(appNamespace.getName(), appNamespace);

          // 当 namespaceName 发生改变的情况，将老的移除出 publicAppNamespaceCache
          //in case namespaceName changes
          if (!appNamespace.getName().equals(thatInCache.getName()) && thatInCache.isPublic()) {
            publicAppNamespaceCache.remove(thatInCache.getName());
          }
        } else if (thatInCache.isPublic()) {
          // 新的不是公用类型，需要移除
          //just in case isPublic changes
          publicAppNamespaceCache.remove(thatInCache.getName());
        }
        log.info("Found AppNamespace changes, old: {}, new: {}", thatInCache, appNamespace);
      }
    }
    return foundIds;
  }

  /**
   * 删除名称空间处理
   *
   * @param deletedIds 待删除的id
   */
  private void handleDeletedAppNamespaces(Set<Long> deletedIds) {
    if (CollectionUtils.isEmpty(deletedIds)) {
      return;
    }
    for (Long deletedId : deletedIds) {
      // 从 appNamespaceIdCache 中移除
      AppNamespace deleted = appNamespaceIdCache.remove(deletedId);
      if (deleted == null) {
        continue;
      }
      // 从 appNamespaceCache 中移除
      appNamespaceCache.remove(assembleAppNamespaceKey(deleted));
      // 从 publicAppNamespaceCache 移除
      if (deleted.isPublic()) {
        AppNamespace publicAppNamespace = publicAppNamespaceCache.get(deleted.getName());
        // in case there is some dirty data, e.g. public namespace deleted in some app and now created in another app
        if (publicAppNamespace == deleted) {
          publicAppNamespaceCache.remove(deleted.getName());
        }
      }
      log.info("Found AppNamespace deleted, {}", deleted);
    }
  }

  /**
   * 拼接应用名称空间KEy
   *
   * @param appNamespace 应用名称空间
   * @return 拼接后的名称空间KEy
   */
  private String assembleAppNamespaceKey(AppNamespace appNamespace) {
    return STRING_JOINER.join(appNamespace.getAppId(), appNamespace.getName());
  }

  /**
   * 填充基本的周期数据
   */
  private void populateDataBaseInterval() {
    // 扫描周期
    scanInterval = bizConfig.appNamespaceCacheScanInterval();
    scanIntervalTimeUnit = bizConfig.appNamespaceCacheScanIntervalTimeUnit();
    // 重建周期
    rebuildInterval = bizConfig.appNamespaceCacheRebuildInterval();
    rebuildIntervalTimeUnit = bizConfig.appNamespaceCacheRebuildIntervalTimeUnit();
  }

  /**
   * 重置
   */
  private void reset() {
    // 停止执行任务
    scheduledExecutorService.shutdownNow();
    // 初始化
    initialize();
    afterPropertiesSet();
  }
}
