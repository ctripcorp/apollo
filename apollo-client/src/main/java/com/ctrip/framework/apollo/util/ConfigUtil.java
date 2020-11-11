package com.ctrip.framework.apollo.util;

import static com.ctrip.framework.apollo.util.factory.PropertiesFactory.APOLLO_PROPERTY_ORDER_ENABLE;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.MetaDomainConsts;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.enums.EnvUtils;
import com.ctrip.framework.foundation.Foundation;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.RateLimiter;
import java.io.File;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * 配置工具类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public class ConfigUtil {

  /**
   * 刷新间隔
   */
  private Long refreshInterval = TimeUnit.MINUTES.toMinutes(5);
  /**
   * 刷新间隔单位
   */
  private TimeUnit refreshIntervalTimeUnit = TimeUnit.MINUTES;
  /**
   * 连接超时时间
   */
  private Long connectTimeout = TimeUnit.SECONDS.toMillis(1);
  /**
   * 写超时时间
   */
  private Long readTimeout = TimeUnit.SECONDS.toMillis(5);
  /**
   * 集群
   */
  private String cluster;
  /**
   * 加载配置每秒执行次数
   */
  private int loadConfigQPS = 2;
  /**
   * 长轮询的每秒执行次数
   */
  private int longPollQPS = 2;
  /**
   * 失败重试间隔
   */
  private long onErrorRetryInterval = TimeUnit.SECONDS.toSeconds(1);
  /**
   * 失败重试间隔时间单位
   */
  private TimeUnit onErrorRetryIntervalTimeUnit = TimeUnit.SECONDS;
  /**
   * 最大配置缓存的大小
   */
  private long maxConfigCacheSize = 500;
  /**
   * 配置缓存过期时间
   */
  private long configCacheExpireTime = TimeUnit.MINUTES.toMinutes(1);
  /**
   * 配置缓存过期时间单位
   */
  private TimeUnit configCacheExpireTimeUnit = TimeUnit.MINUTES;
  /**
   * 长轮询初始延迟时间
   */
  private long longPollingInitialDelayInMills = TimeUnit.SECONDS.toMillis(2);
  /**
   * 是否自动更新配置，如果不想自动更新则将该值设置为false，默认值为true
   */
  private boolean autoUpdateInjectedSpringProperties = true;
  /**
   * 警告日志限流器
   */
  private final RateLimiter warnLogRateLimiter;
  /**
   * Properties有序化开关
   */
  private boolean propertiesOrdered = false;

  /**
   * 构建ConfigUtil，并初始化
   */
  public ConfigUtil() {
    // 每分钟1个警告日志输出
    warnLogRateLimiter = RateLimiter.create(0.017);
    initRefreshInterval();
    initConnectTimeout();
    initReadTimeout();
    initCluster();
    initQPS();
    initMaxConfigCacheSize();
    initLongPollingInitialDelayInMills();
    initAutoUpdateInjectedSpringProperties();
    initPropertiesOrdered();
  }

  /**
   * 获取当前应用程序的应用id
   *
   * @return appId, 如果应用程序id不可用, ConfigConsts.NO_APPID占位
   */
  public String getAppId() {
    String appId = Foundation.app().getAppId();
    if (StringUtils.isBlank(appId)) {
      // 为空的AppId占位符
      appId = ConfigConsts.NO_APPID_PLACEHOLDER;
      if (warnLogRateLimiter.tryAcquire()) {
        log.warn(
            "app.id is not set, please make sure it is set in classpath:/META-INF/app.properties, now apollo will only load public namespace configurations!");
      }
    }
    return appId;
  }

  /**
   * 获取当前应用程序的访问密钥
   *
   * @return 当前访问密钥secret，如果没有该密钥返回为空。
   */
  public String getAccessKeySecret() {
    return Foundation.app().getAccessKeySecret();
  }

  /**
   * 获取当前应用程序的数据中心信息 .
   *
   * @return 当前数据中心，无此信息为空.
   */
  public String getDataCenter() {
    return Foundation.server().getDataCenter();
  }

  /**
   * 初始化集群
   */
  private void initCluster() {
    // 从系统属性加载
    cluster = System.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY);

    // 为空设置为数据中心
    if (StringUtils.isBlank(cluster)) {
      cluster = getDataCenter();
    }

    // 设置默认的集群名称
    if (StringUtils.isBlank(cluster)) {
      cluster = ConfigConsts.CLUSTER_NAME_DEFAULT;
    }
  }

  /**
   * 获取当前应用程序的集群名称
   *
   * @return 群集名称，如果未指定，则为"default"
   */
  public String getCluster() {
    return cluster;
  }

  /**
   * 获取当前环境.
   *
   * @return env，如果未设置或无效，则UNKNOWN
   */
  public Env getApolloEnv() {
    return EnvUtils.transformEnv(Foundation.server().getEnvType());
  }

  /**
   * 获取主机地址(ip)
   *
   * @return 主机地址(ip)
   */
  public String getLocalIp() {
    return Foundation.net().getHostAddress();
  }

  /**
   * 获取元服务器地址，多地址用逗号分隔
   *
   * @return 元服务器地址
   */
  public String getMetaServerDomainName() {
    return MetaDomainConsts.getDomain(getApolloEnv());
  }

  /**
   * 初始化连接超时时间
   */
  private void initConnectTimeout() {
    // 自定义的连接超时时间
    String customizedConnectTimeout = System.getProperty("apollo.connectTimeout");
    // 不为空，设置
    if (!Strings.isNullOrEmpty(customizedConnectTimeout)) {
      try {
        connectTimeout = Long.parseLong(customizedConnectTimeout);
      } catch (Throwable ex) {
        log.error("Config for apollo.connectTimeout is invalid: {}", customizedConnectTimeout);
      }
    }
  }

  /**
   * 获取连接超时时间
   *
   * @return 连接超时时间
   */
  public int getConnectTimeout() {
    return connectTimeout.intValue();
  }

  /**
   * 初始化写超时时间
   */
  private void initReadTimeout() {
    // 自定义的写超时时间
    String customizedReadTimeout = System.getProperty("apollo.readTimeout");
    // 不为空，设置
    if (!Strings.isNullOrEmpty(customizedReadTimeout)) {
      try {
        readTimeout = Long.parseLong(customizedReadTimeout);
      } catch (Throwable ex) {
        log.error("Config for apollo.readTimeout is invalid: {}", customizedReadTimeout);
      }
    }
  }

  /**
   * 获取写超时时间
   *
   * @return 写超时时间
   */
  public int getReadTimeout() {
    return readTimeout.intValue();
  }

  /**
   * 刷新间隔初始化
   */
  private void initRefreshInterval() {
    // 自定义的刷新间隔
    String customizedRefreshInterval = System.getProperty("apollo.refreshInterval");
    // 不为空，设置
    if (!Strings.isNullOrEmpty(customizedRefreshInterval)) {
      try {
        refreshInterval = Long.parseLong(customizedRefreshInterval);
      } catch (Throwable ex) {
        log.error("Config for apollo.refreshInterval is invalid: {}", customizedRefreshInterval);
      }
    }
  }

  /**
   * 获取刷新间隔
   *
   * @return 刷新间隔值
   */
  public int getRefreshInterval() {
    return refreshInterval.intValue();
  }

  /**
   * 获取刷新间隔时间 单位
   *
   * @return 刷新间隔值
   */
  public TimeUnit getRefreshIntervalTimeUnit() {
    return refreshIntervalTimeUnit;
  }

  /**
   * 初始化QPS
   */
  private void initQPS() {
    // 自定义的加载配置每秒执行次数
    String customizedLoadConfigQPS = System.getProperty("apollo.loadConfigQPS");
    if (StringUtils.isNotBlank(customizedLoadConfigQPS)) {
      try {
        loadConfigQPS = Integer.parseInt(customizedLoadConfigQPS);
      } catch (Throwable ex) {
        log.error("Config for apollo.loadConfigQPS is invalid: {}", customizedLoadConfigQPS);
      }
    }

    // 自定义的长轮询的每秒执行次数
    String customizedLongPollQPS = System.getProperty("apollo.longPollQPS");
    if (StringUtils.isNotBlank(customizedLongPollQPS)) {
      try {
        longPollQPS = Integer.parseInt(customizedLongPollQPS);
      } catch (Throwable ex) {
        log.error("Config for apollo.longPollQPS is invalid: {}", customizedLongPollQPS);
      }
    }
  }

  /**
   * 获取加载配置每秒执行次数
   *
   * @return 加载配置每秒执行次数
   */
  public int getLoadConfigQPS() {
    return loadConfigQPS;
  }

  /**
   * 获取长轮询的每秒执行次数
   *
   * @return 长轮询的每秒执行次数
   */
  public int getLongPollQPS() {
    return longPollQPS;
  }

  /**
   * 获取失败重试间隔
   *
   * @return 失败重试间隔
   */
  public long getOnErrorRetryInterval() {
    return onErrorRetryInterval;
  }

  /**
   * 获取失败重试间隔时间单位
   *
   * @return 失败重试间隔时间单位
   */
  public TimeUnit getOnErrorRetryIntervalTimeUnit() {
    return onErrorRetryIntervalTimeUnit;
  }

  /**
   * 获得默认缓存配置目录
   *
   * @return 默认缓存配置目录
   */
  public String getDefaultLocalCacheDir() {
    // 自定义的缓存目录
    String cacheRoot = getCustomizedCacheRoot();

    // 缓存目录
    if (StringUtils.isNotBlank(cacheRoot)) {
      return cacheRoot + File.separator + getAppId();
    }

    // 系统判断
    cacheRoot = isOSWindows() ? "C:\\opt\\data\\%s" : "/opt/data/%s";
    return String.format(cacheRoot, getAppId());
  }

  /**
   * 获取自定义的缓存目录
   *
   * @return 自义的缓存目录
   */
  private String getCustomizedCacheRoot() {
    // 1. 从系统属性中获取
    String cacheRoot = System.getProperty("apollo.cacheDir");
    if (Strings.isNullOrEmpty(cacheRoot)) {
      // 2. 从操作系统环境变量中获取
      cacheRoot = System.getenv("APOLLO_CACHEDIR");
    }
    if (Strings.isNullOrEmpty(cacheRoot)) {
      // 3. 从server.properties获取
      cacheRoot = Foundation.server().getProperty("apollo.cacheDir", null);
    }
    if (Strings.isNullOrEmpty(cacheRoot)) {
      // 4. 从app.properties获取
      cacheRoot = Foundation.app().getProperty("apollo.cacheDir", null);
    }

    return cacheRoot;
  }

  /**
   * 是否为本地模式
   *
   * @return true，本地模式，否则，false
   */
  public boolean isInLocalMode() {
    try {
      return Env.LOCAL == getApolloEnv();
    } catch (Throwable ex) {
      //ignore
    }
    return false;
  }

  /**
   * 是否Windeows操作系统
   *
   * @return true, windows系统，否则 ，false
   */
  public boolean isOSWindows() {
    // 系统名称
    String osName = System.getProperty("os.name");
    if (StringUtils.isBlank(osName)) {
      return false;
    }
    return osName.startsWith("Windows");
  }

  /**
   * 初始化最大配置缓存的大小
   */
  private void initMaxConfigCacheSize() {
    // 1. 从系统属性中获取
    String customizedConfigCacheSize = System.getProperty("apollo.configCacheSize");
    // 不为空设置
    if (StringUtils.isNotBlank(customizedConfigCacheSize)) {
      try {
        maxConfigCacheSize = Long.parseLong(customizedConfigCacheSize);
      } catch (Throwable ex) {
        log.error("Config for apollo.configCacheSize is invalid: {}", customizedConfigCacheSize);
      }
    }
  }

  /**
   * 获取最大配置缓存的大小
   *
   * @return 最大配置缓存的大小
   */
  public long getMaxConfigCacheSize() {
    return maxConfigCacheSize;
  }

  /**
   * 获取配置缓存过期时间
   *
   * @return 配置缓存过期时间
   */
  public long getConfigCacheExpireTime() {
    return configCacheExpireTime;
  }

  /**
   * 获取配置缓存过期时间单位
   *
   * @return 配置缓存过期时间单位
   */
  public TimeUnit getConfigCacheExpireTimeUnit() {
    return configCacheExpireTimeUnit;
  }

  /**
   * 初始化长轮询初始延迟时间
   */
  private void initLongPollingInitialDelayInMills() {
    // 1. 从系统属性中获取
    String customizedLongPollingInitialDelay = System
        .getProperty("apollo.longPollingInitialDelayInMills");
    // 如果不为空，就设置
    if (StringUtils.isNotBlank(customizedLongPollingInitialDelay)) {
      try {
        longPollingInitialDelayInMills = Long.parseLong(customizedLongPollingInitialDelay);
      } catch (Throwable ex) {
        log.error("Config for apollo.longPollingInitialDelayInMills is invalid: {}",
            customizedLongPollingInitialDelay);
      }
    }
  }

  /**
   * 获取长轮询初始延迟时间
   *
   * @return 长轮询初始延迟时间
   */
  public long getLongPollingInitialDelayInMills() {
    return longPollingInitialDelayInMills;
  }

  /**
   * 初始化自动更新配置属性
   */
  private void initAutoUpdateInjectedSpringProperties() {
    // 1. 从系统属性中获取
    String enableAutoUpdate = System.getProperty("apollo.autoUpdateInjectedSpringProperties");
    if (StringUtils.isBlank(enableAutoUpdate)) {
      // 2. 从app.properties获取
      enableAutoUpdate = Foundation.app()
          .getProperty("apollo.autoUpdateInjectedSpringProperties", null);
    }
    // 不为空时设置是否自动更新配置属性
    if (StringUtils.isNotBlank(enableAutoUpdate)) {
      autoUpdateInjectedSpringProperties = Boolean.parseBoolean(enableAutoUpdate.trim());
    }
  }

  /**
   * 是否开启自动更新机制
   *
   * @return true, 开启自动更新机制，否则，false
   */
  public boolean isAutoUpdateInjectedSpringPropertiesEnabled() {
    return autoUpdateInjectedSpringProperties;
  }

  /**
   * 初始化Properties有序化开关
   */
  private void initPropertiesOrdered() {
    // 1. 从系统属性中获取
    String enablePropertiesOrdered = System.getProperty(APOLLO_PROPERTY_ORDER_ENABLE);
    // 2. 从app.properties获取
    if (StringUtils.isBlank(enablePropertiesOrdered)) {
      enablePropertiesOrdered = Foundation.app().getProperty(APOLLO_PROPERTY_ORDER_ENABLE, "false");
    }

    // 不为空，设置Properties有序化
    if (StringUtils.isNotBlank(enablePropertiesOrdered)) {
      try {
        propertiesOrdered = Boolean.parseBoolean(enablePropertiesOrdered);
      } catch (Throwable ex) {
        log.warn("Config for {} is invalid: {}, set default value: false",
            APOLLO_PROPERTY_ORDER_ENABLE, enablePropertiesOrdered);
      }
    }
  }

  /**
   * 是否Properties有序化
   *
   * @return true, 有序化，否则，false
   */
  public boolean isPropertiesOrderEnabled() {
    return propertiesOrdered;
  }
}
