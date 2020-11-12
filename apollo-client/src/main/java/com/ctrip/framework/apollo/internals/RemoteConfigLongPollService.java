package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.framework.apollo.core.dto.ApolloNotificationMessages;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.schedule.ExponentialSchedulePolicy;
import com.ctrip.framework.apollo.core.schedule.SchedulePolicy;
import com.ctrip.framework.apollo.core.signature.Signature;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.ctrip.framework.apollo.util.http.HttpRequest;
import com.ctrip.framework.apollo.util.http.HttpResponse;
import com.ctrip.framework.apollo.util.http.HttpUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * 远程配置长轮询服务。负责长轮询 Config Service 的配置变更通知 /notifications/v2 接口。
 * <p>当有新的通知时，触发 RemoteConfigRepository ，立即轮询 Config Service 的配置读取 /configs/{appId}/{clusterName}/{namespace:.+}
 * 接口。
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public class RemoteConfigLongPollService {

  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
  private static final Joiner.MapJoiner MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");
  private static final Escaper queryParamEscaper = UrlEscapers.urlFormParameterEscaper();
  /**
   * 初始的通知id
   */
  private static final long INIT_NOTIFICATION_ID = ConfigConsts.NOTIFICATION_ID_PLACEHOLDER;
  /**
   * 长轮询读取超时时间（90秒，应该比服务器端的长轮询超时(现在是60秒)还要长）
   */
  private static final long LONG_POLLING_READ_TIMEOUT = TimeUnit.SECONDS.toMillis(90);
  /**
   * 长轮询 执行器服务
   */
  private final ExecutorService longPollingService;
  /**
   * 是否停止长轮询的标识
   */
  private final AtomicBoolean longPollingStopped;
  /**
   * 失败定时重试策略，使用 {@link ExponentialSchedulePolicy}
   */
  private SchedulePolicy longPollFailSchedulePolicyInSecond;
  /**
   * 长轮询的限流器
   */
  private RateLimiter longPollRateLimiter;
  /**
   * 是否长轮询已经开始的标识
   */
  private final AtomicBoolean longPollStarted;
  /**
   * 长轮询的 Namespace Multimap 缓存
   * <p>
   * 通过 {@link #submit(String, RemoteConfigRepository)} 添加 RemoteConfigRepository 。
   * <p>
   * KEY：Namespace 的名字 VALUE：RemoteConfigRepository 集合
   */
  private final Multimap<String, RemoteConfigRepository> longPollNamespaces;
  /**
   * 通知编号 Map 缓存
   * <p>
   * KEY：Namespace 的名字 VALUE：最新的通知编号
   */
  private final ConcurrentMap<String, Long> notifications;
  /**
   * 通知消息 Map 缓存
   * <p>
   * KEY：Namespace 的名字 VALUE：ApolloNotificationMessages 对象
   */
  // namespaceName -> watchedKey -> notificationId
  private final Map<String, ApolloNotificationMessages> remoteNotificationMessages;
  /**
   * 响应的类型
   */
  private Type responseType;
  private static final Gson GSON = new Gson();
  private ConfigUtil configUtil;
  /**
   * 请求工具类
   */
  private HttpUtil httpUtil;
  /**
   * 配置服务定位器
   */
  private ConfigServiceLocator serviceLocator;

  /**
   * 初始化.
   */
  public RemoteConfigLongPollService() {
    longPollFailSchedulePolicyInSecond = new ExponentialSchedulePolicy(1, 120); //in second
    longPollingStopped = new AtomicBoolean(false);
    longPollingService = Executors.newSingleThreadExecutor(
        ApolloThreadFactory.create("RemoteConfigLongPollService", true));
    longPollStarted = new AtomicBoolean(false);
    longPollNamespaces =
        Multimaps.synchronizedSetMultimap(HashMultimap.<String, RemoteConfigRepository>create());
    notifications = Maps.newConcurrentMap();
    remoteNotificationMessages = Maps.newConcurrentMap();
    responseType = new TypeToken<List<ApolloConfigNotification>>() {
    }.getType();
    configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    httpUtil = ApolloInjector.getInstance(HttpUtil.class);
    serviceLocator = ApolloInjector.getInstance(ConfigServiceLocator.class);
    longPollRateLimiter = RateLimiter.create(configUtil.getLongPollQPS());
  }

  /**
   * 提交 RemoteConfigRepository 到长轮询任务
   *
   * @param namespace              名称空间
   * @param remoteConfigRepository 远程配置存储库
   * @return true，成功，否则，false
   */
  public boolean submit(String namespace, RemoteConfigRepository remoteConfigRepository) {
    // 添加到 longPollNamespaces 中
    boolean added = longPollNamespaces.put(namespace, remoteConfigRepository);
    // 添加到 notifications 中
    notifications.putIfAbsent(namespace, INIT_NOTIFICATION_ID);
    // 若未启动长轮询定时任务，进行启动
    if (!longPollStarted.get()) {
      startLongPolling();
    }
    return added;
  }

  /**
   * 启动长轮询任务
   */
  private void startLongPolling() {
    // CAS 设置长轮询任务已经启动。若已经启动，不重复启动。
    if (!longPollStarted.compareAndSet(false, true)) {
      //already started
      return;
    }
    try {
      // 获得 appId cluster dataCenter 配置信息
      final String appId = configUtil.getAppId();
      final String cluster = configUtil.getCluster();
      final String dataCenter = configUtil.getDataCenter();
      final String secret = configUtil.getAccessKeySecret();
      // 获得长轮询任务的初始化延迟时间，单位毫秒
      final long longPollingInitialDelayInMills = configUtil.getLongPollingInitialDelayInMills();
      // 提交长轮询任务。该任务会持续且循环执行。
      longPollingService.submit(new Runnable() {
        @Override
        public void run() {
          // 初始等待
          if (longPollingInitialDelayInMills > 0) {
            try {
              log.debug("Long polling will start in {} ms.", longPollingInitialDelayInMills);
              TimeUnit.MILLISECONDS.sleep(longPollingInitialDelayInMills);
            } catch (InterruptedException e) {
              //ignore
            }
          }
          // 执行长轮询
          doLongPollingRefresh(appId, cluster, dataCenter, secret);
        }
      });
    } catch (Throwable ex) {
      // 执行长轮询
      longPollStarted.set(false);
      ApolloConfigException exception =
          new ApolloConfigException("Schedule long polling refresh failed", ex);
      Tracer.logError(exception);
      log.warn(ExceptionUtil.getDetailMessage(exception));
    }
  }

  /**
   * 停止长轮询刷新
   */
  void stopLongPollingRefresh() {
    this.longPollingStopped.compareAndSet(false, true);
  }

  private void doLongPollingRefresh(String appId, String cluster, String dataCenter,
      String secret) {
    final Random random = new Random();
    ServiceDTO lastServiceDto = null;
    // 循环执行，直到停止或线程中断
    while (!longPollingStopped.get() && !Thread.currentThread().isInterrupted()) {
      // 限流c
      if (!longPollRateLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
        //wait at most 5 seconds
        try {
          TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
        }
      }
      Transaction transaction = Tracer.newTransaction("Apollo.ConfigService", "pollNotification");
      String url = null;
      try {
        // 获得 Config Service 的地址
        if (lastServiceDto == null) {
          // 获得所有的 Config Service 的地址
          List<ServiceDTO> configServices = getConfigServices();
          lastServiceDto = configServices.get(random.nextInt(configServices.size()));
        }

        // 组装长轮询通知变更的地址
        url = assembleLongPollRefreshUrl(lastServiceDto.getHomepageUrl(), appId, cluster,
            dataCenter, notifications);

        log.debug("Long polling from {}", url);

        // 创建 HttpRequest 对象，并设置超时时间
        HttpRequest request = new HttpRequest(url);
        request.setReadTimeout(LONG_POLLING_READ_TIMEOUT);
        // 设置签名
        if (!StringUtils.isBlank(secret)) {
          Map<String, String> headers = Signature.buildHttpHeaders(url, appId, secret);
          request.setHeaders(headers);
        }

        transaction.addData("Url", url);
        // 发起请求，返回 HttpResponse 对象
        final HttpResponse<List<ApolloConfigNotification>> response =
            httpUtil.doGet(request, responseType);
        log.debug("Long polling response: {}, url: {}", response.getStatusCode(), url);

        // 有新的通知，刷新本地的缓存
        if (response.getStatusCode() == 200 && response.getBody() != null) {
          // 更新 notifications
          updateNotifications(response.getBody());
          // 更新 remoteNotificationMessages
          updateRemoteNotifications(response.getBody());
          transaction.addData("Result", response.getBody().toString());
          // 通知对应的 RemoteConfigRepository 们
          notify(lastServiceDto, response.getBody());
        }

        //try to load balance
        // 无新的通知，重置连接的 Config Service 的地址，下次请求不同的 Config Service ，实现负载均衡c
        if (response.getStatusCode() == 304 && random.nextBoolean()) {
          lastServiceDto = null;
        }
        // 标记成功cc
        longPollFailSchedulePolicyInSecond.success();
        transaction.addData("StatusCode", response.getStatusCode());
        transaction.setStatus(Transaction.SUCCESS);
      } catch (Throwable ex) {
        // 重置连接的 Config Service 的地址，下次请求不同的 Config Service
        lastServiceDto = null;
        Tracer.logEvent("ApolloConfigException", ExceptionUtil.getDetailMessage(ex));
        transaction.setStatus(ex);
        // 标记失败，计算下一次延迟执行时间
        long sleepTimeInSecond = longPollFailSchedulePolicyInSecond.fail();
        log.warn(
            "Long polling failed, will retry in {} seconds. appId: {}, cluster: {}, namespaces: {}, long polling url: {}, reason: {}",
            sleepTimeInSecond, appId, cluster, assembleNamespaces(), url,
            ExceptionUtil.getDetailMessage(ex));

        // 等待一定时间，下次失败重试
        try {
          TimeUnit.SECONDS.sleep(sleepTimeInSecond);
        } catch (InterruptedException ie) {
          //ignore
        }
      } finally {
        transaction.complete();
      }
    }
  }

  /**
   * 更新 remoteNotificationMessages
   *
   * @param lastServiceDto 上一次的服务信息
   * @param notifications  apollo配置通知列表
   */
  private void notify(ServiceDTO lastServiceDto, List<ApolloConfigNotification> notifications) {
    if (CollectionUtils.isEmpty(notifications)) {
      return;
    }
    // 循环 ApolloConfigNotificationcc
    for (ApolloConfigNotification notification : notifications) {
      String namespaceName = notification.getNamespaceName();
      //create a new list to avoid ConcurrentModificationException
      // 创建 RemoteConfigRepository 数组，避免并发问题
      List<RemoteConfigRepository> toBeNotified = Lists
          .newArrayList(longPollNamespaces.get(namespaceName));

      // 获得远程的 ApolloNotificationMessages 对象，并克隆
      ApolloNotificationMessages originalMessages = remoteNotificationMessages.get(namespaceName);
      ApolloNotificationMessages remoteMessages =
          originalMessages == null ? null : originalMessages.clone();

      //since .properties are filtered out by default, so we need to check if there is any listener for it
      // 因为 .properties 在默认情况下被过滤掉，所以我们需要检查是否有监听器。若有，添加到 RemoteConfigRepository 数组
      toBeNotified.addAll(longPollNamespaces
          .get(String.format("%s.%s", namespaceName, ConfigFileFormat.Properties.getValue())));
      // 循环 RemoteConfigRepository ，进行通知
      for (RemoteConfigRepository remoteConfigRepository : toBeNotified) {
        try {
          // 进行通知
          remoteConfigRepository.onLongPollNotified(lastServiceDto, remoteMessages);
        } catch (Throwable ex) {
          Tracer.logError(ex);
        }
      }
    }
  }

  /**
   * 更新 notificationsc
   *
   * @param deltaNotifications 增量通知
   */
  private void updateNotifications(List<ApolloConfigNotification> deltaNotifications) {
    // 循环 ApolloConfigNotifications
    for (ApolloConfigNotification notification : deltaNotifications) {
      if (Strings.isNullOrEmpty(notification.getNamespaceName())) {
        continue;
      }

      // 更新 notifications
      String namespaceName = notification.getNamespaceName();
      if (notifications.containsKey(namespaceName)) {
        notifications.put(namespaceName, notification.getNotificationId());
      }
      //since .properties are filtered out by default, so we need to check if there is notification with .properties suffix
      // 因为 .properties 在默认情况下被过滤掉，所以我们需要检查是否有 .properties 后缀的通知。如有，更新 notificationscc
      String namespaceNameWithPropertiesSuffix =
          String.format("%s.%s", namespaceName, ConfigFileFormat.Properties.getValue());
      if (notifications.containsKey(namespaceNameWithPropertiesSuffix)) {
        notifications.put(namespaceNameWithPropertiesSuffix, notification.getNotificationId());
      }
    }
  }

  /**
   * 更新 remoteNotificationMessages
   *
   * @param deltaNotifications 增量通知
   */
  private void updateRemoteNotifications(List<ApolloConfigNotification> deltaNotifications) {
    // 循环 ApolloConfigNotifications
    for (ApolloConfigNotification notification : deltaNotifications) {
      if (Strings.isNullOrEmpty(notification.getNamespaceName())) {
        continue;
      }

      if (notification.getMessages() == null || notification.getMessages().isEmpty()) {
        continue;
      }

      // 若不存在 Namespace 对应的 ApolloNotificationMessages ，进行创建
      ApolloNotificationMessages localRemoteMessages =
          remoteNotificationMessages.get(notification.getNamespaceName());
      if (localRemoteMessages == null) {
        localRemoteMessages = new ApolloNotificationMessages();
        remoteNotificationMessages.put(notification.getNamespaceName(), localRemoteMessages);
      }
      // 合并通知消息到 ApolloNotificationMessages 中
      localRemoteMessages.mergeFrom(notification.getMessages());
    }
  }

  /**
   * 组装名称空间
   *
   * @return 名称空间字符串
   */
  private String assembleNamespaces() {
    return STRING_JOINER.join(longPollNamespaces.keySet());
  }

  /**
   * 组装长轮询Config Service的配置变更通知 /notifications/v2接口的URL
   *
   * @param uri              链接URI
   * @param appId            应用id
   * @param cluster          集群
   * @param dataCenter       数据中心
   * @param notificationsMap 通知Map
   * @return 组装后的长轮询Config Service的配置变更通知 /notifications/v2接口的URL
   */
  String assembleLongPollRefreshUrl(String uri, String appId, String cluster, String dataCenter,
      Map<String, Long> notificationsMap) {
    Map<String, String> queryParams = Maps.newHashMap();
    // 应用id
    queryParams.put("appId", queryParamEscaper.escape(appId));
    // 集群名称
    queryParams.put("cluster", queryParamEscaper.escape(cluster));
    // 通知集
    queryParams
        .put("notifications", queryParamEscaper.escape(assembleNotifications(notificationsMap)));
    // 数据中心
    if (!Strings.isNullOrEmpty(dataCenter)) {
      queryParams.put("dataCenter", queryParamEscaper.escape(dataCenter));
    }
    // ip地址
    String localIp = configUtil.getLocalIp();
    if (!Strings.isNullOrEmpty(localIp)) {
      queryParams.put("ip", queryParamEscaper.escape(localIp));
    }

    String params = MAP_JOINER.join(queryParams);
    // 拼接最终的请求 URL
    if (!uri.endsWith("/")) {
      uri += "/";
    }
    // 拼接 查询字符串
    return uri + "notifications/v2?" + params;
  }

  /**
   * 组装通知Map
   *
   * @param notificationsMap 通知Map
   * @return 通知Map的Json串
   */
  String assembleNotifications(Map<String, Long> notificationsMap) {
    // 创建 ApolloConfigNotification 数组
    List<ApolloConfigNotification> notifications = Lists.newArrayList();
    // 循环，添加 ApolloConfigNotification 对象
    for (Map.Entry<String, Long> entry : notificationsMap.entrySet()) {
      ApolloConfigNotification notification = new ApolloConfigNotification(entry.getKey(),
          entry.getValue());
      notifications.add(notification);
    }
    // JSON 化成字符串
    return GSON.toJson(notifications);
  }

  /**
   * 从远程元服务器获取配置服务信息
   *
   * @return 配置服务的信息
   */
  private List<ServiceDTO> getConfigServices() {
    List<ServiceDTO> services = serviceLocator.getConfigServices();
    if (services.isEmpty()) {
      throw new ApolloConfigException("No available config service");
    }

    return services;
  }
}
