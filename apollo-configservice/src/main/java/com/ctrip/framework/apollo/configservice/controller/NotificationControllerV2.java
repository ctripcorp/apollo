package com.ctrip.framework.apollo.configservice.controller;

import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.message.ReleaseMessageListener;
import com.ctrip.framework.apollo.biz.message.Topics;
import com.ctrip.framework.apollo.biz.utils.EntityManagerUtil;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.configservice.service.ReleaseMessageServiceWithCache;
import com.ctrip.framework.apollo.configservice.util.NamespaceUtil;
import com.ctrip.framework.apollo.configservice.util.WatchKeysUtil;
import com.ctrip.framework.apollo.configservice.wrapper.DeferredResultWrapper;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 实现 ReleaseMessageListener 接口，通知 Controller ，仅提供 notifications/v2 接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
@RestController
@RequestMapping("/notifications/v2")
public class NotificationControllerV2 implements ReleaseMessageListener {

  /**
   * 监听的key 与 DeferredResultWrapper 的 Multimap
   * <p>
   * Key：监听的key, Value：DeferredResultWrapper 数组
   */
  private final Multimap<String, DeferredResultWrapper> deferredResults =
      Multimaps.synchronizedSetMultimap(TreeMultimap.create(String.CASE_INSENSITIVE_ORDER,
          Ordering.natural()));

  private static final Splitter STRING_SPLITTER = Splitter
      .on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR).omitEmptyStrings();
  private static final Type notificationsTypeReference = new TypeToken<List<ApolloConfigNotification>>() {
  }.getType();

  /**
   * 大量通知分批执行 ExecutorService
   */
  private final ExecutorService largeNotificationBatchExecutorService;
  /**
   * 监听Key的工具类
   */
  private final WatchKeysUtil watchKeysUtil;
  private final ReleaseMessageServiceWithCache releaseMessageService;
  private final EntityManagerUtil entityManagerUtil;
  private final NamespaceUtil namespaceUtil;
  private final Gson gson;
  private final BizConfig bizConfig;

  @Autowired
  public NotificationControllerV2(
      final WatchKeysUtil watchKeysUtil,
      final ReleaseMessageServiceWithCache releaseMessageService,
      final EntityManagerUtil entityManagerUtil,
      final NamespaceUtil namespaceUtil,
      final Gson gson,
      final BizConfig bizConfig) {
    largeNotificationBatchExecutorService = Executors
        .newSingleThreadExecutor(ApolloThreadFactory.create
            ("NotificationControllerV2", true));
    this.watchKeysUtil = watchKeysUtil;
    this.releaseMessageService = releaseMessageService;
    this.entityManagerUtil = entityManagerUtil;
    this.namespaceUtil = namespaceUtil;
    this.gson = gson;
    this.bizConfig = bizConfig;
  }

  /**
   * 轮询通知
   *
   * @param appId                 应用id
   * @param cluster               集群名称
   * @param notificationsAsString 通知字符串
   * @param dataCenter            数据中心
   * @param clientIp              客户端ip
   * @return 仅返回配置发生变化的 Namespace 对应的Apollo配置通知
   */
  @GetMapping
  public DeferredResult<ResponseEntity<List<ApolloConfigNotification>>> pollNotification(
      @RequestParam(value = "appId") String appId, @RequestParam(value = "cluster") String cluster,
      @RequestParam(value = "notifications") String notificationsAsString,
      @RequestParam(value = "dataCenter", required = false) String dataCenter,
      @RequestParam(value = "ip", required = false) String clientIp) {
    // 解析 notificationsAsString 参数，创建 ApolloConfigNotification 数组。
    List<ApolloConfigNotification> notifications = null;

    try {
      notifications =
          gson.fromJson(notificationsAsString, notificationsTypeReference);
    } catch (Throwable ex) {
      Tracer.logError(ex);
    }

    if (CollectionUtils.isEmpty(notifications)) {
      throw new BadRequestException("Invalid format of notifications: " + notificationsAsString);
    }

    // 过滤并创建Apollo配置通知Map
    Map<String, ApolloConfigNotification> filteredNotifications = filterNotifications(appId,
        notifications);

    if (CollectionUtils.isEmpty(filteredNotifications)) {
      throw new BadRequestException("Invalid format of notifications: " + notificationsAsString);
    }
    // 创建 DeferredResultWrapper 对象
    DeferredResultWrapper deferredResultWrapper = new DeferredResultWrapper(
        bizConfig.longPollingTimeoutInMilli());
    // 名称空间集合
    Set<String> namespaces = Sets.newHashSetWithExpectedSize(filteredNotifications.size());
    // 客户端的通知 Map,key 为 名称空间名称，value 为通知id。
    Map<String, Long> clientSideNotifications = Maps.newHashMapWithExpectedSize(
        filteredNotifications.size());
    // 循环 ApolloConfigNotification Map ，初始化上述变量
    for (Map.Entry<String, ApolloConfigNotification> notificationEntry : filteredNotifications
        .entrySet()) {
      String normalizedNamespace = notificationEntry.getKey();
      ApolloConfigNotification notification = notificationEntry.getValue();
      // 添加到 `namespaces` 中。
      namespaces.add(normalizedNamespace);
      // 添加到 `clientSideNotifications` 中。
      clientSideNotifications.put(normalizedNamespace, notification.getNotificationId());
      // 记录名字被标准化的名称空间，因为，最终返回给客户端，使用原始的 Namespace 名字，否则客户端无法识别。
      if (!Objects.equals(notification.getNamespaceName(), normalizedNamespace)) {
        deferredResultWrapper.recordNamespaceNameNormalizedResult(notification.getNamespaceName(),
            normalizedNamespace);
      }
    }

    // 组装监听Key的Multimap
    Multimap<String, String> watchedKeysMap =
        watchKeysUtil.assembleAllWatchKeys(appId, cluster, namespaces, dataCenter);

    // 生成监听Key集合
    Set<String> watchedKeys = Sets.newHashSet(watchedKeysMap.values());

    /**
     * 1、在检查之前设置deferredResult，为了避免在设置deferredResult之前进行检查，
     * 下次在检查和设置deferredResult之间执行方法handleMessage时可能会收到通知
     */
    // 注册超时事件
    deferredResultWrapper
        .onTimeout(() -> logWatchedKeys(watchedKeys, "Apollo.LongPoll.TimeOutKeys"));
    // 注册结束事件
    deferredResultWrapper.onCompletion(() -> {
      //unregister all keys
      // 移除 Watch Key + DeferredResultWrapper 出 `deferredResults`
      for (String key : watchedKeys) {
        deferredResults.remove(key, deferredResultWrapper);
      }
      logWatchedKeys(watchedKeys, "Apollo.LongPoll.CompletedKeys");
    });

    //register all keys
    // 注册 Watch Key + DeferredResultWrapper 到 deferredResults中，等待配置发生变化后通知。详见handleMessage方法。
    for (String key : watchedKeys) {
      this.deferredResults.put(key, deferredResultWrapper);
    }

    logWatchedKeys(watchedKeys, "Apollo.LongPoll.RegisteredKeys");
    log.debug("Listening {} from appId: {}, cluster: {}, namespace: {}, datacenter: {}",
        watchedKeys, appId, cluster, namespaces, dataCenter);

    /**
     * 2、检查新的发布
     */
    // 获得监听Key集合中，每个监听Key对应的 ReleaseMessage 记录。
    List<ReleaseMessage> latestReleaseMessages =
        releaseMessageService.findLatestReleaseMessagesGroupByMessages(watchedKeys);

    /**
     * Manually close the entity manager.
     * Since for async request, Spring won't do so until the request is finished,
     * which is unacceptable since we are doing long polling - means the db connection would be hold
     * for a very long time
     */
    // 手动关闭 EntityManager, 因为对于 async 请求，Spring 在请求完成之前不会这样做
    // 这是不可接受的，因为我们正在做长轮询——意味着 db 连接将被保留很长时间。
    // 实际上，下面的过程，我们已经不需要 db 连接，因此进行关闭。
    entityManagerUtil.closeEntityManager();
    // 获得新的 ApolloConfigNotification 通知数组
    List<ApolloConfigNotification> newNotifications =
        getApolloConfigNotifications(namespaces, clientSideNotifications, watchedKeysMap,
            latestReleaseMessages);

    // 若有新的通知，直接设置结果。
    if (!CollectionUtils.isEmpty(newNotifications)) {
      deferredResultWrapper.setResult(newNotifications);
    }

    return deferredResultWrapper.getResult();
  }

  /**
   * 过滤并创建 ApolloConfigNotification Map 。其中，KEY为名称空间名称
   *
   * @param appId         应用id
   * @param notifications 配置通知列表
   * @return 名称空间通知Map<名称空间名称, 通知>
   */
  private Map<String, ApolloConfigNotification> filterNotifications(String appId,
      List<ApolloConfigNotification> notifications) {
    Map<String, ApolloConfigNotification> filteredNotifications = Maps.newHashMap();
    for (ApolloConfigNotification notification : notifications) {
      if (Strings.isNullOrEmpty(notification.getNamespaceName())) {
        continue;
      }
      //strip out .properties suffix
      // 若 Namespace名以.properties 结尾，移除该结尾，并设置到 ApolloConfigNotification 中。例如 application.properties => application 。
      String originalNamespace = namespaceUtil.filterNamespaceName(notification.getNamespaceName());
      notification.setNamespaceName(originalNamespace);
      //fix the character case issue, such as FX.apollo <-> fx.apollo
      // 获得标准化的 Namespace 名字。因为，客户端 Namespace 会填写错大小写。例如，数据库中 Namespace 名
      // 为 Fx.Apollo ，而客户端 Namespace 名为 fx.Apollo，通过标准化后，统一为 Fx.Apollo
      String normalizedNamespace = namespaceUtil.normalizeNamespace(appId, originalNamespace);

      // in case client side namespace name has character case issue and has difference notification ids
      // such as FX.apollo = 1 but fx.apollo = 2, we should let FX.apollo have the chance to update its notification id
      // which means we should record FX.apollo = 1 here and ignore fx.apollo = 2
      // 如果客户端 Namespace 的名字有大小写的问题，并且恰好有不同的通知编号。例如 Namespace 名字
      // 为 FX.apollo 的通知编号是 1 ，但是 fx.apollo 的通知编号为 2 。我们应该让 FX.apollo 可以更新它
      // 的通知id，所以，我们使用 FX.apollo 的 ApolloConfigNotification 对象，添加到结果，而忽略 fx.apollo 。
      if (filteredNotifications.containsKey(normalizedNamespace) &&
          filteredNotifications.get(normalizedNamespace).getNotificationId() < notification
              .getNotificationId()) {
        continue;
      }

      filteredNotifications.put(normalizedNamespace, notification);
    }
    return filteredNotifications;
  }

  /**
   * 获得新的 Apollo配置通知数组
   *
   * @param namespaces              名称空间列表
   * @param clientSideNotifications 客户端通知Map
   * @param watchedKeysMap          监听key的Map
   * @param latestReleaseMessages   最新的发布消息
   * @return
   */
  private List<ApolloConfigNotification> getApolloConfigNotifications(Set<String> namespaces,
      Map<String, Long> clientSideNotifications, Multimap<String, String> watchedKeysMap,
      List<ReleaseMessage> latestReleaseMessages) {
    // 创建 ApolloConfigNotification 数组
    List<ApolloConfigNotification> newNotifications = Lists.newArrayList();
    if (!CollectionUtils.isEmpty(latestReleaseMessages)) {
      // 创建最新通知的 Map 。其中 Key 为 Watch Key 。
      Map<String, Long> latestNotifications = Maps.newHashMap();
      for (ReleaseMessage releaseMessage : latestReleaseMessages) {
        latestNotifications.put(releaseMessage.getMessage(), releaseMessage.getId());
      }

      // 循环 Namespace 的名字的集合，判断是否有配置更新
      for (String namespace : namespaces) {
        long clientSideId = clientSideNotifications.get(namespace);
        long latestId = ConfigConsts.NOTIFICATION_ID_PLACEHOLDER;

        // 获得 Namespace 对应的 Watch Key 集合
        Collection<String> namespaceWatchedKeys = watchedKeysMap.get(namespace);
        // 获得最大的通知编号
        for (String namespaceWatchedKey : namespaceWatchedKeys) {
          long namespaceNotificationId = latestNotifications.getOrDefault(namespaceWatchedKey,
              ConfigConsts.NOTIFICATION_ID_PLACEHOLDER);
          if (namespaceNotificationId > latestId) {
            latestId = namespaceNotificationId;
          }
        }

        // 若服务器的通知编号大于客户端的通知编号，意味着有配置更新
        if (latestId > clientSideId) {
          // 创建 ApolloConfigNotification 对象
          ApolloConfigNotification notification = new ApolloConfigNotification(namespace, latestId);
          // 循环添加通知编号到 ApolloConfigNotification 中。
          namespaceWatchedKeys.stream().filter(latestNotifications::containsKey)
              .forEach(namespaceWatchedKey -> notification.addMessage(namespaceWatchedKey,
                  latestNotifications.get(namespaceWatchedKey)));
          // 添加 ApolloConfigNotification 对象到结果
          newNotifications.add(notification);
        }
      }
    }
    return newNotifications;
  }

  @Override
  public void handleMessage(ReleaseMessage message, String channel) {
    // 当有新的 ReleaseMessage 时，通知其对应的 Namespace 的，并且正在等待的请求
    log.info("message received - channel: {}, message: {}", channel, message);

    String content = message.getMessage();
    Tracer.logEvent("Apollo.LongPoll.Messages", content);
    // 仅处理 APOLLO_RELEASE_TOPIC
    if (!Topics.APOLLO_RELEASE_TOPIC.equals(channel) || StringUtils.isBlank(content)) {
      return;
    }

    // 获得对应的 Namespace 的名字
    String changedNamespace = retrieveNamespaceFromReleaseMessage.apply(content);
    if (StringUtils.isBlank(changedNamespace)) {
      log.error("message format invalid - {}", content);
      return;
    }

    // `deferredResults` 存在对应的监听key
    if (!deferredResults.containsKey(content)) {
      return;
    }

    // 创建 DeferredResultWrapper 数组，避免并发问题。
    List<DeferredResultWrapper> results = Lists.newArrayList(deferredResults.get(content));

    // 创建 ApolloConfigNotification 对象
    ApolloConfigNotification configNotification = new ApolloConfigNotification(changedNamespace,
        message.getId());
    configNotification.addMessage(content, message.getId());

    //do async notification if too many clients
    // 若需要通知的客户端过多，使用 ExecutorService 异步通知，避免“惊群效应”
    if (results.size() > bizConfig.releaseMessageNotificationBatch()) {
      largeNotificationBatchExecutorService.submit(() -> {
        log.debug("Async notify {} clients for key {} with batch {}", results.size(), content,
            bizConfig.releaseMessageNotificationBatch());
        for (int i = 0; i < results.size(); i++) {
          // 每 N 个客户端，sleep 一段时间。
          if (i > 0 && i % bizConfig.releaseMessageNotificationBatch() == 0) {
            try {
              TimeUnit.MILLISECONDS
                  .sleep(bizConfig.releaseMessageNotificationBatchIntervalInMilli());
            } catch (InterruptedException e) {
              //ignore
            }
          }
          log.debug("Async notify {}", results.get(i));
          // 设置结果
          results.get(i).setResult(configNotification);
        }
      });
      return;
    }

    log.debug("Notify {} clients for key {}", results.size(), content);
    // 设置结果
    for (DeferredResultWrapper result : results) {
      result.setResult(configNotification);
    }
    log.debug("Notification completed");
  }

  /**
   * 通过 ReleaseMessage 的消息内容，获得对应 Namespace 的名字
   */
  private static final Function<String, String> retrieveNamespaceFromReleaseMessage =
      releaseMessage -> {
        if (StringUtils.isBlank(releaseMessage)) {
          return null;
        }
        List<String> keys = STRING_SPLITTER.splitToList(releaseMessage);
        //message should be appId+cluster+namespace
        if (keys.size() != 3) {
          log.error("message format invalid - {}", releaseMessage);
          return null;
        }
        return keys.get(2);
      };

  /**
   * 监听key日志
   *
   * @param watchedKeys 监听key的列表
   * @param eventName   事件名称
   */
  private void logWatchedKeys(Set<String> watchedKeys, String eventName) {
    for (String watchedKey : watchedKeys) {
      Tracer.logEvent(eventName, watchedKey);
    }
  }
}
