package com.ctrip.apollo.internals;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.ctrip.apollo.core.dto.ApolloConfig;
import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.apollo.util.ConfigUtil;
import com.ctrip.apollo.util.http.HttpRequest;
import com.ctrip.apollo.util.http.HttpResponse;
import com.ctrip.apollo.util.http.HttpUtil;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.ContainerLoader;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigRepository extends AbstractConfigRepository{
  private static final Logger logger = LoggerFactory.getLogger(RemoteConfigRepository.class);
  private PlexusContainer m_container;
  private final ConfigServiceLocator m_serviceLocator;
  private final HttpUtil m_httpUtil;
  private final ConfigUtil m_configUtil;
  private volatile AtomicReference<ApolloConfig> m_configCache;
  private final String m_namespace;
  private final ScheduledExecutorService m_executorService;

  public RemoteConfigRepository(String namespace) {
    m_namespace = namespace;
    m_configCache = new AtomicReference<>();
    m_container = ContainerLoader.getDefaultContainer();
    try {
      m_configUtil = m_container.lookup(ConfigUtil.class);
      m_httpUtil = m_container.lookup(HttpUtil.class);
      m_serviceLocator = m_container.lookup(ConfigServiceLocator.class);
    } catch (ComponentLookupException e) {
      throw new IllegalStateException("Unable to load component!", e);
    }
    this.m_executorService = Executors.newScheduledThreadPool(1,
            ApolloThreadFactory.create("RemoteConfigRepository", true));
    this.schedulePeriodicRefresh();
  }

  @Override
  public Properties getConfig() {
    if (m_configCache.get() == null) {
      this.loadRemoteConfig();
    }
    return transformApolloConfigToProperties(m_configCache.get());
  }

  @Override
  public void setFallback(ConfigRepository fallbackConfigRepository) {
    //remote config doesn't need fallback
  }

  private void schedulePeriodicRefresh() {
    logger.info("Schedule periodic refresh with interval: {} {}",
        m_configUtil.getRefreshInterval(), m_configUtil.getRefreshTimeUnit());
    this.m_executorService.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            try {
              loadRemoteConfig();
            } catch (Throwable ex) {
              logger.error("Refreshing config failed", ex);
            }
          }
        }, m_configUtil.getRefreshInterval(), m_configUtil.getRefreshInterval(),
        m_configUtil.getRefreshTimeUnit());
  }

  synchronized void loadRemoteConfig() {
    ApolloConfig previous = m_configCache.get();
    ApolloConfig current = loadApolloConfig();

    //HTTP 304, nothing changed
    if (previous == current) {
      return;
    }

    logger.info("Remote Config changes!");

    m_configCache.set(current);

    this.fireRepositoryChange(m_namespace, this.getConfig());
  }

  private Properties transformApolloConfigToProperties(ApolloConfig apolloConfig) {
    Properties result = new Properties();
    result.putAll(apolloConfig.getConfigurations());
    return result;
  }


  private ApolloConfig loadApolloConfig() {
    String appId = m_configUtil.getAppId();
    String cluster = m_configUtil.getCluster();
    String url = assembleUrl(getConfigServiceUrl(), appId, cluster, m_namespace, m_configCache.get());

    logger.info("Loading config from {}", url);
    HttpRequest request = new HttpRequest(url);

    try {
      HttpResponse<ApolloConfig> response = m_httpUtil.doGet(request, ApolloConfig.class);
      if (response.getStatusCode() == 304) {
        logger.info("Config server responds with 304 HTTP status code.");
        return m_configCache.get();
      }

      logger.info("Loaded config: {}", response.getBody());

      return response.getBody();
    } catch (Throwable t) {
      String message =
          String.format("Load Apollo Config failed - appId: %s, cluster: %s, namespace: %s", appId,
              cluster, m_namespace);
      logger.error(message, t);
      throw new RuntimeException(message, t);
    }
  }

  private String assembleUrl(String uri, String appId, String cluster, String namespace,
                             ApolloConfig previousConfig) {
    String path = "/config/%s/%s";
    List<String> params = Lists.newArrayList(appId, cluster);

    if (!Strings.isNullOrEmpty(namespace)) {
      path = path + "/%s";
      params.add(namespace);
    }
    if (previousConfig != null) {
      path = path + "?releaseId=%s";
      params.add(String.valueOf(previousConfig.getReleaseId()));
    }

    String pathExpanded = String.format(path, params.toArray());
    return uri + pathExpanded;
  }

  private String getConfigServiceUrl() {
    List<ServiceDTO> services = m_serviceLocator.getConfigServices();
    if (services.size() == 0) {
      throw new RuntimeException("No available config service");
    }
    return services.get(0).getHomepageUrl();
  }
}
