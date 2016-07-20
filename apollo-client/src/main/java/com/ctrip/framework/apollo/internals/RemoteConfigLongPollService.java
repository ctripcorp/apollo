package com.ctrip.framework.apollo.internals;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.common.util.concurrent.RateLimiter;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.schedule.ExponentialSchedulePolicy;
import com.ctrip.framework.apollo.core.schedule.SchedulePolicy;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.ctrip.framework.apollo.util.http.HttpRequest;
import com.ctrip.framework.apollo.util.http.HttpResponse;
import com.ctrip.framework.apollo.util.http.HttpUtil;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Named(type = RemoteConfigLongPollService.class)
public class RemoteConfigLongPollService implements Initializable {
  private static final Logger logger = LoggerFactory.getLogger(RemoteConfigLongPollService.class);
  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
  private static final Joiner.MapJoiner MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");
  private static final Escaper queryParamEscaper = UrlEscapers.urlFormParameterEscaper();
  private final ExecutorService m_longPollingService;
  private final AtomicBoolean m_longPollingStopped;
  private SchedulePolicy m_longPollFailSchedulePolicyInSecond;
  private AtomicReference<ApolloConfigNotification> m_longPollResult;
  private RateLimiter m_longPollRateLimiter;
  private final AtomicBoolean m_longPollStarted;
  private final Multimap<String, RemoteConfigRepository> m_longPollNamespaces;
  @Inject
  private ConfigUtil m_configUtil;
  @Inject
  private HttpUtil m_httpUtil;
  @Inject
  private ConfigServiceLocator m_serviceLocator;

  /**
   * Constructor.
   */
  public RemoteConfigLongPollService() {
    m_longPollFailSchedulePolicyInSecond = new ExponentialSchedulePolicy(1, 120); //in second
    m_longPollingStopped = new AtomicBoolean(false);
    m_longPollingService = Executors.newSingleThreadExecutor(
        ApolloThreadFactory.create("RemoteConfigLongPollService", true));
    m_longPollResult = new AtomicReference<>();
    m_longPollStarted = new AtomicBoolean(false);
    m_longPollNamespaces =
        Multimaps.synchronizedSetMultimap(HashMultimap.<String, RemoteConfigRepository>create());
  }

  @Override
  public void initialize() throws InitializationException {
    m_longPollRateLimiter = RateLimiter.create(m_configUtil.getLongPollQPS());
  }

  public boolean submit(String namespace, RemoteConfigRepository remoteConfigRepository) {
    boolean added = m_longPollNamespaces.put(namespace, remoteConfigRepository);
    if (!m_longPollStarted.get()) {
      startLongPolling();
    }
    return added;
  }

  private void startLongPolling() {
    if (!m_longPollStarted.compareAndSet(false, true)) {
      //already started
      return;
    }
    try {
      final String appId = m_configUtil.getAppId();
      final String cluster = m_configUtil.getCluster();
      final String dataCenter = m_configUtil.getDataCenter();
      m_longPollingService.submit(new Runnable() {
        @Override
        public void run() {
          doLongPollingRefresh(appId, cluster, dataCenter);
        }
      });
    } catch (Throwable ex) {
      m_longPollStarted.set(false);
      ApolloConfigException exception =
          new ApolloConfigException("Schedule long polling refresh failed", ex);
      Cat.logError(exception);
      logger.warn(ExceptionUtil.getDetailMessage(exception));
    }
  }

  void stopLongPollingRefresh() {
    this.m_longPollingStopped.compareAndSet(false, true);
  }

  private void doLongPollingRefresh(String appId, String cluster, String dataCenter) {
    final Random random = new Random();
    ServiceDTO lastServiceDto = null;
    while (!m_longPollingStopped.get() && !Thread.currentThread().isInterrupted()) {
      if (!m_longPollRateLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
        //wait at most 5 seconds
        try {
          TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
        }
      }
      Transaction transaction = Cat.newTransaction("Apollo.ConfigService", "pollNotification");
      String namespaces = assembleNamespaces();
      try {
        if (lastServiceDto == null) {
          List<ServiceDTO> configServices = getConfigServices();
          lastServiceDto = configServices.get(random.nextInt(configServices.size()));
        }

        String url =
            assembleLongPollRefreshUrl(lastServiceDto.getHomepageUrl(), appId, cluster,
                namespaces, dataCenter, m_longPollResult.get());

        logger.debug("Long polling from {}", url);
        HttpRequest request = new HttpRequest(url);
        //longer timeout for read - 1 minute
        request.setReadTimeout(60000);

        transaction.addData("Url", url);

        final HttpResponse<ApolloConfigNotification> response =
            m_httpUtil.doGet(request, ApolloConfigNotification.class);

        logger.debug("Long polling response: {}, url: {}", response.getStatusCode(), url);
        if (response.getStatusCode() == 200) {
          if (response.getBody() != null) {
            m_longPollResult.set(response.getBody());
            transaction.addData("Result", response.getBody().toString());
          }
          notify(lastServiceDto, response.getBody());
        }

        m_longPollFailSchedulePolicyInSecond.success();
        transaction.addData("StatusCode", response.getStatusCode());
        transaction.setStatus(Message.SUCCESS);
      } catch (Throwable ex) {
        lastServiceDto = null;
        Cat.logError(ex);
        transaction.setStatus(ex);
        long sleepTimeInSecond = m_longPollFailSchedulePolicyInSecond.fail();
        logger.warn(
            "Long polling failed, will retry in {} seconds. appId: {}, cluster: {}, namespaces: {}, reason: {}",
            sleepTimeInSecond, appId, cluster, namespaces, ExceptionUtil.getDetailMessage(ex));
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

  private void notify(ServiceDTO lastServiceDto, ApolloConfigNotification notification) {
    if (notification == null) {
      return;
    }
    String namespaceName = notification.getNamespaceName();
    //create a new list to avoid ConcurrentModificationException
    List<RemoteConfigRepository> toBeNotified =
        Lists.newArrayList(m_longPollNamespaces.get(namespaceName));
    //since .properties are filtered out by default, so we need to check if there is any listener for it
    toBeNotified.addAll(m_longPollNamespaces
        .get(String.format("%s.%s", namespaceName, ConfigFileFormat.Properties.getValue())));
    for (RemoteConfigRepository remoteConfigRepository : toBeNotified) {
      remoteConfigRepository.onLongPollNotified(lastServiceDto);
    }
  }

  private String assembleNamespaces() {
    return STRING_JOINER.join(m_longPollNamespaces.keySet());
  }

  String assembleLongPollRefreshUrl(String uri, String appId, String cluster,
                                    String namespace, String dataCenter,
                                    ApolloConfigNotification previousResult) {
    Map<String, String> queryParams = Maps.newHashMap();
    queryParams.put("appId", queryParamEscaper.escape(appId));
    queryParams.put("cluster", queryParamEscaper.escape(cluster));

    if (!Strings.isNullOrEmpty(namespace)) {
      queryParams.put("namespace", queryParamEscaper.escape(namespace));
    }
    if (!Strings.isNullOrEmpty(dataCenter)) {
      queryParams.put("dataCenter", queryParamEscaper.escape(dataCenter));
    }
    String localIp = m_configUtil.getLocalIp();
    if (!Strings.isNullOrEmpty(localIp)) {
      queryParams.put("ip", queryParamEscaper.escape(localIp));
    }

    if (previousResult != null) {
      //number doesn't need encode
      queryParams.put("notificationId", String.valueOf(previousResult.getNotificationId()));
    }

    String params = MAP_JOINER.join(queryParams);
    if (!uri.endsWith("/")) {
      uri += "/";
    }

    return uri + "notifications?" + params;
  }

  private List<ServiceDTO> getConfigServices() {
    List<ServiceDTO> services = m_serviceLocator.getConfigServices();
    if (services.size() == 0) {
      throw new ApolloConfigException("No available config service");
    }

    return services;
  }
}
