package com.ctrip.framework.apollo.configservice.internal.impl;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.biz.utils.ReleaseMessageKeyGenerator;
import com.ctrip.framework.apollo.configservice.internal.ConfigCache;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.tracer.Tracer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

/**
 * @author lepdou 2017-05-04
 */
@Component
public class DefaultConfigCache implements ConfigCache {

  private static final String CAT_EVENT_CACHE_INVALIDATE = "Cache.Invalidate";
  private static final String CAT_EVENT_CACHE_LOAD = "Cache.LoadFromDB";
  private static final String CAT_EVENT_CACHE_GET = "Cache.Get";
  private static final Splitter STRING_SPLITTER =
      Splitter.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR).omitEmptyStrings();

  @Autowired
  private ReleaseService releaseService;

  private LoadingCache<String, Optional<Release>> configCache;


  @PostConstruct
  public void init() {
    configCache = CacheBuilder.newBuilder()
        .build(new CacheLoader<String, Optional<Release>>() {
          @Override
          public Optional<Release> load(String key) throws Exception {
            List<String> namespaceInfo = STRING_SPLITTER.splitToList(key);
            if (namespaceInfo.size() != 3) {
              Tracer.logError(String.format("Cache load key's size not equal 3. Key = %s", key),
                              new IllegalArgumentException());
              return Optional.absent();
            }
            Release release = releaseService.findLatestActiveRelease(namespaceInfo.get(0), namespaceInfo.get(1),
                                                                     namespaceInfo.get(2));
            Tracer.logEvent(CAT_EVENT_CACHE_LOAD, key);
            return release == null ? Optional.absent() : Optional.of(release);
          }
        });
  }

  @Override
  public Release get(String appId, String clusterName, String namespaceName) {
    String key = ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName);

    try {
      Optional<Release> cachedRelease = configCache.get(key);
      if (cachedRelease != null && cachedRelease.isPresent()) {
        return cachedRelease.get();
      } else {
        // release not exist in db
        return null;
      }
    } catch (ExecutionException e) {
      Tracer.logError("Load config from cache error.", e);
      return null;
    } finally {
      Tracer.logEvent(CAT_EVENT_CACHE_GET, key);
    }
  }

  @Override
  public void invalidate(String appId, String clusterName, String namespaceName) {
    configCache.invalidate(ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName));
    Tracer.logEvent(CAT_EVENT_CACHE_INVALIDATE, ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName));
  }

  @Override
  public void invalidate(String key) {
    configCache.invalidate(key);
    Tracer.logEvent(CAT_EVENT_CACHE_INVALIDATE, key);
  }

  @Override
  public void clear() {
    configCache.invalidateAll();
  }

}
