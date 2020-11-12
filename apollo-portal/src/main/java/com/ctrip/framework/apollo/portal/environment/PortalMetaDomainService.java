package com.ctrip.framework.apollo.portal.environment;

import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.core.utils.NetUtil;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 界面（门户）元领域 Service
 * <p>仅在apollo门户提供程序中使用可用的元服务器url。如果给定环境没有可用的元服务器url，则将使用默认的元服务器url(http://apollo.meta).
 *
 * @author wxq
 * @see com.ctrip.framework.apollo.core.MetaDomainConsts
 */
@Slf4j
@Service
public class PortalMetaDomainService {

  /**
   * 刷新间隔(秒)
   */
  private static final long REFRESH_INTERVAL_IN_SECOND = TimeUnit.MINUTES.toSeconds(1);
  /**
   * 默认的元服务器地址
   */
  static final String DEFAULT_META_URL = "http://apollo.meta";
  /**
   * 元服务器地址缓存（Env,address）
   */
  private final Map<Env, String> metaServerAddressCache = Maps.newConcurrentMap();

  /**
   * 元服务器提供器缓存. 多个{@link PortalMetaServerProvider}
   */
  private final List<PortalMetaServerProvider> portalMetaServerProviders = new ArrayList<>();
  /**
   * 选定的元服务缓存(元服务器地址<多个用逗号分隔>,address),逗号分隔的元服务器地址->选定的单个元服务器地址缓存
   */
  private final Map<String, String> selectedMetaServerAddressCache = Maps.newConcurrentMap();
  /**
   * 是否启动定期刷新
   */
  private final AtomicBoolean periodicRefreshStarted = new AtomicBoolean(false);

  PortalMetaDomainService(final PortalConfig portalConfig) {
    // 数据库中的数据具有高优先级
    portalMetaServerProviders.add(new DatabasePortalMetaServerProvider(portalConfig));

    // 从系统属性、操作系统环境变量、配置文件中加载
    portalMetaServerProviders.add(new DefaultPortalMetaServerProvider());
  }

  /**
   * 返回一个元服务器地址。如果配置了多个元服务器地址，将选择一个
   */
  public String getDomain(Env env) {
    String metaServerAddress = getMetaServerAddress(env);
    // if there is more than one address, need to select one
    if (metaServerAddress.contains(",")) {
      return selectMetaServerAddress(metaServerAddress);
    }
    return metaServerAddress;
  }

  /**
   * 返回元服务器地址。如果配置了多个元服务器地址，将返回逗号分隔的字符串。
   *
   * @return 返回元服务器地址，多个地址用逗号分隔
   */
  public String getMetaServerAddress(Env env) {
    // 元服务器地址缓存没找到就去供应器缓存中找
    if (!metaServerAddressCache.containsKey(env)) {
      // 从元服务供应器缓存找指定环境的元服务器地址,找到后放入元服务器地址缓存
      metaServerAddressCache
          .put(env, getMetaServerAddressCacheValue(portalMetaServerProviders, env));
    }

    // 获取缓存中的元服务器地址
    return metaServerAddressCache.get(env);
  }

  /**
   * 通过给定的环境从提供程序获取元服务器。如果给定环境没有可用的元服务器url，则将使用默认的元服务器url(http://apollo.meta)
   *
   * @param providers 提供环境的元服务器地址
   * @param env       指定的环境
   * @return 元服务器地址
   */
  private String getMetaServerAddressCacheValue(
      Collection<PortalMetaServerProvider> providers, Env env) {

    // null value
    String metaAddress = null;

    // 通过指定的环境获取远服务器地址
    for (PortalMetaServerProvider portalMetaServerProvider : providers) {
      if (portalMetaServerProvider.exists(env)) {
        metaAddress = portalMetaServerProvider.getMetaServerAddress(env);
        log.info("Located meta server address [{}] for env [{}]", metaAddress, env);
        break;
      }
    }

    // 如果给定环境没有可用的元服务器url，则将使用默认的元服务器url
    if (StringUtils.isBlank(metaAddress)) {
      // Fallback to default meta address
      metaAddress = DEFAULT_META_URL;
      log.warn(
          "Meta server address fallback to [{}] for env [{}], because it is not available in MetaServerProvider",
          metaAddress, env);
    }
    return metaAddress.trim();
  }

  /**
   * 重新加载所有的{@link PortalMetaServerProvider}. 清空缓存{@link this#metaServerAddressCache}
   */
  public void reload() {
    for (PortalMetaServerProvider portalMetaServerProvider : portalMetaServerProviders) {
      portalMetaServerProvider.reload();
    }
    metaServerAddressCache.clear();
  }

  /**
   * 从逗号分隔的元服务器地址中选择一个可用的元服务器，例如。http://1.2.3.4:8080，http://2.3.4.5:8080个
   * <p>
   * <br />
   * <p>
   * 在生产环境中，我们仍然建议使用单个域，例如http://config.xxx.com（由nginx等软件负载平衡器支持）而不是多个ip地址
   */
  private String selectMetaServerAddress(String metaServerAddresses) {
    String metaAddressSelected = selectedMetaServerAddressCache.get(metaServerAddresses);
    if (metaAddressSelected == null) {
      // 更新存在的数据
      if (periodicRefreshStarted.compareAndSet(false, true)) {
        schedulePeriodicRefresh();
      }
      //更新当前的数据
      updateMetaServerAddresses(metaServerAddresses);
      metaAddressSelected = selectedMetaServerAddressCache.get(metaServerAddresses);
    }

    return metaAddressSelected;
  }

  /**
   * 更新元服务器地址
   *
   * @param metaServerAddresses 选中的元服务器地址（多个用逗号分隔）
   */
  private void updateMetaServerAddresses(String metaServerAddresses) {
    log.debug("Selecting meta server address for: {}", metaServerAddresses);

    Transaction transaction = Tracer
        .newTransaction("Apollo.MetaService", "refreshMetaServerAddress");
    transaction.addData("Url", metaServerAddresses);

    try {
      // 元服务器地址列表
      List<String> metaServers = Lists.newArrayList(metaServerAddresses.split(","));
      // 随机打乱原来的顺序
      Collections.shuffle(metaServers);

      //服务是否可用
      boolean serverAvailable = false;

      for (String address : metaServers) {
        address = address.trim();
        // 检查服务/services/config是否可访问
        if (NetUtil.pingUrl(address + "/services/config")) {
          // 选择第一个可用的元服务器
          selectedMetaServerAddressCache.put(metaServerAddresses, address);
          serverAvailable = true;
          log.debug("Selected meta server address {} for {}", address, metaServerAddresses);
          break;
        }
      }

      // 需要确保映射不是空的，例如第一次更新可能失败
      if (!selectedMetaServerAddressCache.containsKey(metaServerAddresses)) {
        selectedMetaServerAddressCache.put(metaServerAddresses, metaServers.get(0).trim());
      }

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
   * 定时更新元服务器地址
   */
  private void schedulePeriodicRefresh() {
    ScheduledExecutorService scheduledExecutorService =
        Executors.newScheduledThreadPool(1, ApolloThreadFactory.create("MetaServiceLocator", true));
    // 定时更新
    scheduledExecutorService.scheduleAtFixedRate(() -> {
      try {
        for (String metaServerAddresses : selectedMetaServerAddressCache.keySet()) {
          updateMetaServerAddresses(metaServerAddresses);
        }
      } catch (Throwable ex) {
        log
            .warn(String.format("Refreshing meta server address failed, will retry in %d seconds",
                REFRESH_INTERVAL_IN_SECOND), ex);
      }
    }, REFRESH_INTERVAL_IN_SECOND, REFRESH_INTERVAL_IN_SECOND, TimeUnit.SECONDS);
  }

}
