package com.ctrip.framework.apollo.configservice.controller;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.message.ReleaseMessageListener;
import com.ctrip.framework.apollo.biz.message.Topics;
import com.ctrip.framework.apollo.biz.utils.EntityManagerUtil;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.configservice.service.ReleaseMessageServiceWithCache;
import com.ctrip.framework.apollo.configservice.util.NamespaceUtil;
import com.ctrip.framework.apollo.configservice.util.WatchKeysUtil;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.tracer.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/notifications/v2")
public class NotificationControllerV2 implements ReleaseMessageListener {
  private static final Logger logger = LoggerFactory.getLogger(NotificationControllerV2.class);
  private static final long TIMEOUT = 30 * 1000;//30 seconds
  private final Multimap<String, DeferredResult<ResponseEntity<List<ApolloConfigNotification>>>>
      deferredResults = Multimaps.synchronizedSetMultimap(HashMultimap.create());
  private static final ResponseEntity<List<ApolloConfigNotification>>
      NOT_MODIFIED_RESPONSE_LIST = new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
  private static final Splitter STRING_SPLITTER =
      Splitter.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR).omitEmptyStrings();
  private static final long NOTIFICATION_ID_PLACEHOLDER = -1;
  private static final Type notificationsTypeReference =
      new TypeToken<List<ApolloConfigNotification>>() {
      }.getType();

  private final ExecutorService largeNotificationBatchExecutorService;

  @Autowired
  private WatchKeysUtil watchKeysUtil;

  @Autowired
  private ReleaseMessageServiceWithCache releaseMessageService;

  @Autowired
  private EntityManagerUtil entityManagerUtil;

  @Autowired
  private NamespaceUtil namespaceUtil;

  @Autowired
  private Gson gson;

  @Autowired
  private BizConfig bizConfig;

  public NotificationControllerV2() {
    largeNotificationBatchExecutorService = Executors.newSingleThreadExecutor(ApolloThreadFactory.create
        ("NotificationControllerV2", true));
  }

  @RequestMapping(method = RequestMethod.GET)
  public DeferredResult<ResponseEntity<List<ApolloConfigNotification>>> pollNotification(
      @RequestParam(value = "appId") String appId,
      @RequestParam(value = "cluster") String cluster,
      @RequestParam(value = "notifications") String notificationsAsString,
      @RequestParam(value = "dataCenter", required = false) String dataCenter,
      @RequestParam(value = "ip", required = false) String clientIp) {
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

    Set<String> namespaces = Sets.newHashSet();
    Map<String, Long> clientSideNotifications = Maps.newHashMap();
    for (ApolloConfigNotification notification : notifications) {
      if (Strings.isNullOrEmpty(notification.getNamespaceName())) {
        continue;
      }
      //strip out .properties suffix
      String namespace = namespaceUtil.filterNamespaceName(notification.getNamespaceName());
      namespaces.add(namespace);
      clientSideNotifications.put(namespace, notification.getNotificationId());
    }

    if (CollectionUtils.isEmpty(namespaces)) {
      throw new BadRequestException("Invalid format of notifications: " + notificationsAsString);
    }

    Multimap<String, String> watchedKeysMap =
        watchKeysUtil.assembleAllWatchKeys(appId, cluster, namespaces, dataCenter);

    DeferredResult<ResponseEntity<List<ApolloConfigNotification>>> deferredResult =
        new DeferredResult<>(TIMEOUT, NOT_MODIFIED_RESPONSE_LIST);

    Set<String> watchedKeys = Sets.newHashSet(watchedKeysMap.values());

    List<ReleaseMessage> latestReleaseMessages =
        releaseMessageService.findLatestReleaseMessagesGroupByMessages(watchedKeys);

    /**
     * Manually close the entity manager.
     * Since for async request, Spring won't do so until the request is finished,
     * which is unacceptable since we are doing long polling - means the db connection would be hold
     * for a very long time
     */
    entityManagerUtil.closeEntityManager();

    List<ApolloConfigNotification> newNotifications =
        getApolloConfigNotifications(namespaces, clientSideNotifications, watchedKeysMap,
            latestReleaseMessages);

    if (!CollectionUtils.isEmpty(newNotifications)) {
      deferredResult.setResult(new ResponseEntity<>(newNotifications, HttpStatus.OK));
    } else {
      //register all keys
      for (String key : watchedKeys) {
        this.deferredResults.put(key, deferredResult);
      }

      deferredResult
          .onTimeout(() -> logWatchedKeysToCat(watchedKeys, "Apollo.LongPoll.TimeOutKeys"));

      deferredResult.onCompletion(() -> {
        //unregister all keys
        for (String key : watchedKeys) {
          deferredResults.remove(key, deferredResult);
        }
        logWatchedKeysToCat(watchedKeys, "Apollo.LongPoll.CompletedKeys");
      });

      logWatchedKeysToCat(watchedKeys, "Apollo.LongPoll.RegisteredKeys");
      logger.debug("Listening {} from appId: {}, cluster: {}, namespace: {}, datacenter: {}",
          watchedKeys, appId, cluster, namespaces, dataCenter);
    }

    return deferredResult;
  }

  private List<ApolloConfigNotification> getApolloConfigNotifications(Set<String> namespaces,
                                                                      Map<String, Long> clientSideNotifications,
                                                                      Multimap<String, String> watchedKeysMap,
                                                                      List<ReleaseMessage> latestReleaseMessages) {
    List<ApolloConfigNotification> newNotifications = Lists.newArrayList();
    if (!CollectionUtils.isEmpty(latestReleaseMessages)) {
      Map<String, Long> latestNotifications = Maps.newHashMap();
      for (ReleaseMessage releaseMessage : latestReleaseMessages) {
        latestNotifications.put(releaseMessage.getMessage(), releaseMessage.getId());
      }

      for (String namespace : namespaces) {
        long clientSideId = clientSideNotifications.get(namespace);
        long latestId = NOTIFICATION_ID_PLACEHOLDER;
        Collection<String> namespaceWatchedKeys = watchedKeysMap.get(namespace);
        for (String namespaceWatchedKey : namespaceWatchedKeys) {
          long namespaceNotificationId =
              latestNotifications.getOrDefault(namespaceWatchedKey, NOTIFICATION_ID_PLACEHOLDER);
          if (namespaceNotificationId > latestId) {
            latestId = namespaceNotificationId;
          }
        }
        if (latestId > clientSideId) {
          newNotifications.add(new ApolloConfigNotification(namespace, latestId));
        }
      }
    }
    return newNotifications;
  }

  @Override
  public void handleMessage(ReleaseMessage message, String channel) {
    logger.info("message received - channel: {}, message: {}", channel, message);

    String content = message.getMessage();
    Tracer.logEvent("Apollo.LongPoll.Messages", content);
    if (!Topics.APOLLO_RELEASE_TOPIC.equals(channel) || Strings.isNullOrEmpty(content)) {
      return;
    }

    String changedNamespace = retrieveNamespaceFromReleaseMessage.apply(content);

    if (Strings.isNullOrEmpty(changedNamespace)) {
      logger.error("message format invalid - {}", content);
      return;
    }

    ResponseEntity<List<ApolloConfigNotification>> notification =
        new ResponseEntity<>(
            Lists.newArrayList(new ApolloConfigNotification(changedNamespace, message.getId())),
            HttpStatus.OK);

    if (!deferredResults.containsKey(content)) {
      return;
    }
    //create a new list to avoid ConcurrentModificationException
    List<DeferredResult<ResponseEntity<List<ApolloConfigNotification>>>> results =
        Lists.newArrayList(deferredResults.get(content));

    //do async notification if too many clients
    if (results.size() > bizConfig.releaseMessageNotificationBatch()) {
      largeNotificationBatchExecutorService.submit(() -> {
        logger.debug("Async notify {} clients for key {} with batch {}", results.size(), content,
            bizConfig.releaseMessageNotificationBatch());
        for (int i = 0; i < results.size(); i++) {
          if (i > 0 && i % bizConfig.releaseMessageNotificationBatch() == 0) {
            try {
              TimeUnit.MILLISECONDS.sleep(bizConfig.releaseMessageNotificationBatchIntervalInMilli());
            } catch (InterruptedException e) {
              //ignore
            }
          }
          logger.debug("Async notify {}", results.get(i));
          results.get(i).setResult(notification);
        }
      });
      return;
    }

    logger.debug("Notify {} clients for key {}", results.size(), content);

    for (DeferredResult<ResponseEntity<List<ApolloConfigNotification>>> result : results) {
      result.setResult(notification);
    }
    logger.debug("Notification completed");
  }

  private static final Function<String, String> retrieveNamespaceFromReleaseMessage =
      releaseMessage -> {
        if (Strings.isNullOrEmpty(releaseMessage)) {
          return null;
        }
        List<String> keys = STRING_SPLITTER.splitToList(releaseMessage);
        //message should be appId+cluster+namespace
        if (keys.size() != 3) {
          logger.error("message format invalid - {}", releaseMessage);
          return null;
        }
        return keys.get(2);
      };

  private void logWatchedKeysToCat(Set<String> watchedKeys, String eventName) {
    for (String watchedKey : watchedKeys) {
      Tracer.logEvent(eventName, watchedKey);
    }
  }
}

