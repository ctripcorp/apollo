package com.ctrip.framework.apollo.core;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.ctrip.framework.apollo.core.spi.Ordered;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.core.utils.NetUtil;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

/**
 * 元域将尝试从MetaServerProviders加载元服务器地址，默认为：
 *
 * <ul>
 * <li>com.ctrip.framework.apollo.core.internals.LegacyMetaServerProvider</li>
 * </ul>
 * <p>
 * 如果没有提供程序可以提供元服务器url，则将使用默认的元url(http://apollo.meta).
 * <br />
 * <p>
 * 第三方MetaServerProvider可以通过典型的Java服务加载程序模式注入。
 *
 * @see com.ctrip.framework.apollo.core.internals.LegacyMetaServerProvider
 */
@Slf4j
public class MetaDomainConsts {

  /**
   * 默认的元服务器地址
   */
  public static final String DEFAULT_META_URL = "http://apollo.meta";

  // env -> meta server address cache
  /**
   * 元服务器地址缓存
   */
  private static final Map<Env, String> metaServerAddressCache = Maps.newConcurrentMap();
  /**
   * 元服务器提供器列表
   */
  private static volatile List<MetaServerProvider> metaServerProviders = null;
  /**
   * 刷新间隔1分钟
   */
  private static final long REFRESH_INTERVAL_IN_SECOND = TimeUnit.MINUTES.toSeconds(1);
  // comma separated meta server address -> selected single meta server address cache
  /**
   * 所选的元服务器地址缓存
   */
  private static final Map<String, String> selectedMetaServerAddressCache = Maps.newConcurrentMap();
  /**
   * 是否周期性的刷新
   */
  private static final AtomicBoolean periodicRefreshStarted = new AtomicBoolean(false);
  /**
   * 对象锁
   */
  private static final Object LOCK = new Object();

  /**
   * 返回一个元服务器地址。如果配置了多个元服务器地址，将选择一个。
   *
   * @param env 环境
   * @return 选中的元服务器地址
   */
  public static String getDomain(Env env) {
    String metaServerAddress = getMetaServerAddress(env);
    // 如果需要选择多个地址，请选择一个以上的地址
    if (metaServerAddress.contains(",")) {
      //  从元服务器地址中选择一个可用的元服务器
      return selectMetaServerAddress(metaServerAddress);
    }
    return metaServerAddress;
  }

  /**
   * 返回元服务器地址。如果配置了多个元服务器地址，将返回逗号分隔的字符串
   *
   * @param env 环境
   * @return 元服务器地址，多个用逗号分隔
   */
  public static String getMetaServerAddress(Env env) {
    // 不存在就初始化
    if (!metaServerAddressCache.containsKey(env)) {
      initMetaServerAddress(env);
    }
    return metaServerAddressCache.get(env);
  }

  /**
   * 初始化元服务器地址
   *
   * @param env 环境
   */
  private static void initMetaServerAddress(Env env) {
    // 为空就初始化元服务器提供器列表
    if (metaServerProviders == null) {
      synchronized (LOCK) {
        if (metaServerProviders == null) {
          metaServerProviders = initMetaServerProviders();
        }
      }
    }

    String metaAddress = null;

    for (MetaServerProvider provider : metaServerProviders) {
      // 元服务器地址
      metaAddress = provider.getMetaServerAddress(env);
      if (!Strings.isNullOrEmpty(metaAddress)) {
        log.info("Located meta server address {} for env {} from {}", metaAddress, env,
            provider.getClass().getName());
        break;
      }
    }

    // 元服务地址默认为DEFAULT_META_URL
    if (Strings.isNullOrEmpty(metaAddress)) {
      // Fallback to default meta address
      metaAddress = DEFAULT_META_URL;
      log.warn(
          "Meta server address fallback to {} for env {}, because it is not available in all MetaServerProviders",
          metaAddress, env);
    }

    metaServerAddressCache.put(env, metaAddress.trim());
  }

  /**
   * 初始化元服务器提供器
   *
   * @return 元服务器提供器列表
   */
  private static List<MetaServerProvider> initMetaServerProviders() {
    // 加载MetaServerProvider接口的实现类.
    Iterator<MetaServerProvider> metaServerProviderIterator = ServiceBootstrap
        .loadAll(MetaServerProvider.class);

    List<MetaServerProvider> metaServerProviders = Lists.newArrayList(metaServerProviderIterator);
    // order越小，优先级越高
    Collections.sort(metaServerProviders, Comparator.comparingInt(Ordered::getOrder));
    return metaServerProviders;
  }

  /**
   * 从逗号分隔的元服务器地址中选择一个可用的元服务器, 例如. http://1.2.3.4:8080,http://2.3.4.5:8080
   * <p>
   * <br />
   * <p>
   * 在生产环境中，我们仍然建议使用单个域，例如http://config.xxx.com(由nginx等软件负载平衡器支持) 替换多个ip地址
   */
  private static String selectMetaServerAddress(String metaServerAddresses) {
    String metaAddressSelected = selectedMetaServerAddressCache.get(metaServerAddresses);
    if (metaAddressSelected == null) {
      // 元地址选中的为空时，定时刷新
      if (periodicRefreshStarted.compareAndSet(false, true)) {
        schedulePeriodicRefresh();
      }
      // 更新元服务地址
      updateMetaServerAddresses(metaServerAddresses);
      metaAddressSelected = selectedMetaServerAddressCache.get(metaServerAddresses);
    }

    return metaAddressSelected;
  }

  /**
   * 更新元服务器地址
   *
   * @param metaServerAddresses 元服务地址
   */
  private static void updateMetaServerAddresses(String metaServerAddresses) {
    log.debug("Selecting meta server address for: {}", metaServerAddresses);

    Transaction transaction = Tracer
        .newTransaction("Apollo.MetaService", "refreshMetaServerAddress");
    transaction.addData("Url", metaServerAddresses);

    try {
      // 元服务列表
      List<String> metaServers = Lists.newArrayList(metaServerAddresses.split(","));
      // 打乱顺序
      Collections.shuffle(metaServers);

      boolean serverAvailable = false;

      for (String address : metaServers) {
        address = address.trim();
        // 检查 /services/config 是否可用
        if (NetUtil.pingUrl(address + "/services/config")) {
          // 选中第一个可用的元服务
          selectedMetaServerAddressCache.put(metaServerAddresses, address);
          serverAvailable = true;
          log.debug("Selected meta server address {} for {}", address, metaServerAddresses);
          break;
        }
      }

      // 如果元服务地址不存缓存中，添加，需要确保映射不是空的，例如第一次更新可能失败
      if (!selectedMetaServerAddressCache.containsKey(metaServerAddresses)) {
        selectedMetaServerAddressCache.put(metaServerAddresses, metaServers.get(0).trim());
      }
      // 如果服务不可用，警告
      if (!serverAvailable) {
        log.warn(
            "Could not find available meta server for configured meta server addresses: {}, fallback to: {}",
            metaServerAddresses, selectedMetaServerAddressCache.get(metaServerAddresses));
      }

      transaction.setStatus(Transaction.SUCCESS);
    } catch (Throwable ex) {
      transaction.setStatus(ex);
      throw ex;
    } finally {
      transaction.complete();
    }
  }

  /**
   * 定时按周期刷新
   */
  private static void schedulePeriodicRefresh() {
    ScheduledExecutorService scheduledExecutorService =
        Executors.newScheduledThreadPool(1, ApolloThreadFactory.create("MetaServiceLocator", true));
    // 每分钟执行一次
    scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          // 遍历多个所选的元服务器地址,更新元服务器地址
          for (String metaServerAddresses : selectedMetaServerAddressCache.keySet()) {
            updateMetaServerAddresses(metaServerAddresses);
          }
        } catch (Throwable ex) {
          log.warn(String.format("Refreshing meta server address failed, will retry in %d seconds",
              REFRESH_INTERVAL_IN_SECOND), ex);
        }
      }
    }, REFRESH_INTERVAL_IN_SECOND, REFRESH_INTERVAL_IN_SECOND, TimeUnit.SECONDS);
  }
}
