package com.ctrip.framework.apollo.common.config;

import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.CollectionUtils;

/**
 * 可刷新的配置抽象类
 */
@Slf4j
public abstract class RefreshableConfig {

  /**
   * 列表分隔符
   */
  private static final String LIST_SEPARATOR = ",";
  /**
   * RefreshablePropertySource 刷新频率，单位：秒
   */
  private static final Long CONFIG_REFRESH_INTERVAL = TimeUnit.MINUTES.toSeconds(1);

  protected Splitter splitter = Splitter.on(LIST_SEPARATOR).omitEmptyStrings().trimResults();
  /**
   * 环境， ConfigurableEnvironment 对象。其 PropertySource 不仅仅包括 propertySources ，还包括 yaml properties 等
   * PropertySource
   */
  @Autowired
  private ConfigurableEnvironment environment;
  /**
   * RefreshablePropertySource 数组，通过 {@link #getRefreshablePropertySources} 获得
   */
  private List<RefreshablePropertySource> propertySources;

  /**
   * 注册可刷新属性源。注意：前属性源优先级较高
   *
   * @return 注册的可刷新属性源列表
   */
  protected abstract List<RefreshablePropertySource> getRefreshablePropertySources();

  @PostConstruct
  public void setup() {
    // 获得 RefreshablePropertySource 数组
    propertySources = getRefreshablePropertySources();
    if (CollectionUtils.isEmpty(propertySources)) {
      throw new IllegalStateException("Property sources can not be empty.");
    }

    // 将属性源添加到环境
    for (RefreshablePropertySource propertySource : propertySources) {
      // 刷新属性源
      propertySource.refresh();
      environment.getPropertySources().addLast(propertySource);
    }

    // 更新配置任务
    // 创建 ScheduledExecutorService 对象
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1,
        ApolloThreadFactory.create("ConfigRefresher", true));
    // 提交定时任务，每分钟刷新一次 RefreshablePropertySource 数组
    executorService.scheduleWithFixedDelay(() -> {
      try {
        propertySources.forEach(RefreshablePropertySource::refresh);
      } catch (Throwable t) {
        log.error("Refresh configs failed.", t);
        Tracer.logError("Refresh configs failed.", t);
      }
    }, CONFIG_REFRESH_INTERVAL, CONFIG_REFRESH_INTERVAL, TimeUnit.SECONDS);
  }

  /**
   * 获取Int属性值
   *
   * @param key          属性key
   * @param defaultValue 默认的值
   * @return 如果找到，直接返回，没有找到返回默认值
   */
  public int getIntProperty(String key, int defaultValue) {
    try {
      String value = getValue(key);
      return value == null ? defaultValue : Integer.parseInt(value);
    } catch (Throwable e) {
      Tracer.logError("Get int property failed.", e);
      return defaultValue;
    }
  }

  /**
   * 获取布尔属性值
   *
   * @param key          属性key
   * @param defaultValue 默认的值
   * @return 如果找到，直接返回，没有找到返回默认值
   */
  public boolean getBooleanProperty(String key, boolean defaultValue) {
    try {
      String value = getValue(key);
      return value == null ? defaultValue : "true".equals(value);
    } catch (Throwable e) {
      Tracer.logError("Get boolean property failed.", e);
      return defaultValue;
    }
  }

  /**
   * 获取数组属性值
   *
   * @param key          属性key
   * @param defaultValue 默认的值
   * @return 如果找到，直接返回，没有找到返回默认值
   */
  public String[] getArrayProperty(String key, String[] defaultValue) {
    try {
      String value = getValue(key);
      return Strings.isNullOrEmpty(value) ? defaultValue : value.split(LIST_SEPARATOR);
    } catch (Throwable e) {
      Tracer.logError("Get array property failed.", e);
      return defaultValue;
    }
  }

  /**
   * 获取字符串属性值
   *
   * @param key          属性key
   * @param defaultValue 默认的值
   * @return 如果找到，直接返回，没有找到返回默认值
   */
  public String getValue(String key, String defaultValue) {
    try {
      return environment.getProperty(key, defaultValue);
    } catch (Throwable e) {
      Tracer.logError("Get value failed.", e);
      return defaultValue;
    }
  }

  /**
   * 通过指定Key获取环境指定Value
   *
   * @param key 指定Key
   * @return 环境指定Value
   */
  public String getValue(String key) {
    return environment.getProperty(key);
  }

}
