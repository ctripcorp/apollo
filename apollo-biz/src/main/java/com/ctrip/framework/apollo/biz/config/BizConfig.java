package com.ctrip.framework.apollo.biz.config;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.ctrip.framework.apollo.biz.service.BizDBPropertySource;
import com.ctrip.framework.apollo.common.config.RefreshableConfig;
import com.ctrip.framework.apollo.common.config.RefreshablePropertySource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class BizConfig extends RefreshableConfig {

  private Gson gson = new Gson();
  private static final Type namespaceValueLengthOverrideTypeReference =
      new TypeToken<Map<Long, Integer>>() {
      }.getType();

  @Autowired
  private BizDBPropertySource propertySource;

  @Override
  protected List<RefreshablePropertySource> getRefreshablePropertySources() {
    return Collections.singletonList(propertySource);
  }

  public List<String> eurekaServiceUrls() {
    String configuration = getValue("eureka.service.url", "");
    if (Strings.isNullOrEmpty(configuration)) {
      return Collections.emptyList();
    }

    return splitter.splitToList(configuration);
  }

  public int grayReleaseRuleScanInterval() {
    int interval = getIntProperty("apollo.gray-release-rule-scan.interval", 60);
    return checkInt(interval, 1, Integer.MAX_VALUE, 60);
  }

  public int itemKeyLengthLimit() {
    int limit = getIntProperty("item.key.length.limit", 128);
    return checkInt(limit, 5, Integer.MAX_VALUE, 128);
  }

  public int itemValueLengthLimit() {
    int limit = getIntProperty("item.value.length.limit", 20000);
    return checkInt(limit, 5, Integer.MAX_VALUE, 20000);
  }

  public Map<Long, Integer> namespaceValueLengthLimitOverride() {
    String namespaceValueLengthOverrideString = getValue("namespace.value.length.limit.override");
    Map<Long, Integer> namespaceValueLengthOverride = Maps.newHashMap();
    if (!Strings.isNullOrEmpty(namespaceValueLengthOverrideString)) {
      namespaceValueLengthOverride =
          gson.fromJson(namespaceValueLengthOverrideString, namespaceValueLengthOverrideTypeReference);
    }

    return namespaceValueLengthOverride;
  }

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

  public int appNamespaceCacheScanInterval() {
    int interval = getIntProperty("apollo.app-namespace-cache-scan.interval", 1);
    return checkInt(interval, 1, Integer.MAX_VALUE, 1);
  }

  public TimeUnit appNamespaceCacheScanIntervalTimeUnit() {
    return TimeUnit.SECONDS;
  }

  public int appNamespaceCacheRebuildInterval() {
    int interval = getIntProperty("apollo.app-namespace-cache-rebuild.interval", 60);
    return checkInt(interval, 1, Integer.MAX_VALUE, 60);
  }

  public TimeUnit appNamespaceCacheRebuildIntervalTimeUnit() {
    return TimeUnit.SECONDS;
  }

  public int releaseMessageCacheScanInterval() {
    int interval = getIntProperty("apollo.release-message-cache-scan.interval", 1);
    return checkInt(interval, 1, Integer.MAX_VALUE, 1);
  }

  public TimeUnit releaseMessageCacheScanIntervalTimeUnit() {
    return TimeUnit.SECONDS;
  }

  public int releaseMessageNotificationBatch() {
    int batch = getIntProperty("apollo.release-message.notification.batch", 100);
    return checkInt(batch, 1, Integer.MAX_VALUE, 100);
  }

  public int releaseMessageNotificationBatchIntervalInMilli() {
    int interval = getIntProperty("apollo.release-message.notification.batch.interval", 100);
    return checkInt(interval, 1, Integer.MAX_VALUE, 100);
  }

  int checkInt(int value, int min, int max, int defaultValue) {
    if (value >= min && value <= max) {
      return value;
    }
    return defaultValue;
  }
}
