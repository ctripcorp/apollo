package com.ctrip.framework.apollo.core;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.core.utils.NetUtil;
import com.ctrip.framework.apollo.core.utils.ResourceUtils;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.ctrip.framework.foundation.Foundation;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The meta domain will load the meta server from System environment first, if not exist, will load from
 * apollo-env.properties. If neither exists, will load the default meta url.
 *
 * Currently, apollo supports local/dev/fat/uat/lpt/pro environments.
 */
public class MetaDomainConsts {

  public static final String DEFAULT_META_URL = "http://config.local";

  private static final long REFRESH_INTERVAL_IN_SECOND = 60;//1 min
  private static final Logger logger = LoggerFactory.getLogger(MetaDomainConsts.class);

  private static final Map<Env, String> domains = new HashMap<>();
  private static final Map<String, String> metaServerAddressCache = Maps.newConcurrentMap();
  private static final AtomicBoolean periodicRefreshStarted = new AtomicBoolean(false);
  private static final AtomicBoolean customizedMetaServiceLogged = new AtomicBoolean(false);

  static {
    initialize();
  }

  static void initialize() {
    Properties prop = new Properties();
    prop = ResourceUtils.readConfigFile("apollo-env.properties", prop);
    Properties env = System.getProperties();
    domains.put(Env.LOCAL,
        env.getProperty("local_meta", prop.getProperty("local.meta", DEFAULT_META_URL)));
    domains.put(Env.DEV,
        env.getProperty("dev_meta", prop.getProperty("dev.meta", DEFAULT_META_URL)));
    domains.put(Env.FAT,
        env.getProperty("fat_meta", prop.getProperty("fat.meta", DEFAULT_META_URL)));
    domains.put(Env.UAT,
        env.getProperty("uat_meta", prop.getProperty("uat.meta", DEFAULT_META_URL)));
    domains.put(Env.LPT,
        env.getProperty("lpt_meta", prop.getProperty("lpt.meta", DEFAULT_META_URL)));
    domains.put(Env.PRO,
        env.getProperty("pro_meta", prop.getProperty("pro.meta", DEFAULT_META_URL)));
  }

  public static String getDomain(Env env) {
    // 1. Get meta server address from run time configurations
    String metaAddress = getCustomizedMetaServerAddress();
    if (Strings.isNullOrEmpty(metaAddress)) {
      // 2. Get meta server address from environment
      metaAddress = domains.get(env);
    }
    // 3. if there is more than one address, need to select one
    if (metaAddress != null && metaAddress.contains(",")) {
      return selectMetaServerAddress(metaAddress);
    }
    // 4. trim if necessary
    if (metaAddress != null) {
      metaAddress = metaAddress.trim();
    }
    return metaAddress;
  }

  private static String getCustomizedMetaServerAddress() {
    // 1. Get from System Property
    String metaAddress = System.getProperty("apollo.meta");
    if (Strings.isNullOrEmpty(metaAddress)) {
      // 2. Get from OS environment variable
      metaAddress = System.getenv("APOLLO.META");
    }
    if (Strings.isNullOrEmpty(metaAddress)) {
      metaAddress = Foundation.server().getProperty("apollo.meta", null);
    }

    if (!Strings.isNullOrEmpty(metaAddress) && customizedMetaServiceLogged.compareAndSet(false, true)) {
      logger.warn("Located meta services from apollo.meta configuration: {}, will not use meta services defined in apollo-env.properties!", metaAddress);
    }

    return metaAddress;
  }

  /**
   * Select one available meta server from the comma separated meta server addresses, e.g.
   * http://1.2.3.4:8080,http://2.3.4.5:8080
   *
   * <br />
   *
   * In production environment, we still suggest using one single domain
   * like http://config.xxx.com(backed by software load balancers like nginx) instead of multiple ip addresses
   */
  private static String selectMetaServerAddress(String metaServerAddresses) {
    String metaAddressSelected = metaServerAddressCache.get(metaServerAddresses);
    if (metaAddressSelected == null) {
      //initialize
      if (periodicRefreshStarted.compareAndSet(false, true)) {
        schedulePeriodicRefresh();
      }
      updateMetaServerAddresses(metaServerAddresses);
      metaAddressSelected = metaServerAddressCache.get(metaServerAddresses);
    }

    return metaAddressSelected;
  }

  private static void updateMetaServerAddresses(String metaServerAddresses) {
    logger.debug("Selecting meta server address for: {}", metaServerAddresses);

    Transaction transaction = Tracer.newTransaction("Apollo.MetaService", "refreshMetaServerAddress");
    transaction.addData("Url", metaServerAddresses);

    try {
      List<String> metaServers = Lists.newArrayList(metaServerAddresses.split(","));
      //random load balancing
      Collections.shuffle(metaServers);

      boolean serverAvailable = false;

      for (String address : metaServers) {
        address = address.trim();
        if (NetUtil.pingUrl(address)) {
          //select the first available meta server
          metaServerAddressCache.put(metaServerAddresses, address);
          serverAvailable = true;
          logger.debug("Selected meta server address {} for {}", address, metaServerAddresses);
          break;
        }
      }

      //we need to make sure the map is not empty, e.g. the first update might be failed
      if (!metaServerAddressCache.containsKey(metaServerAddresses)) {
        metaServerAddressCache.put(metaServerAddresses, metaServers.get(0).trim());
      }

      if (!serverAvailable) {
        logger.warn("Could not find available meta server for configured meta server addresses: {}, fallback to: {}", metaServerAddresses,
            metaServerAddressCache.get(metaServerAddresses));
      }

      transaction.setStatus(Transaction.SUCCESS);
    } catch (Throwable ex) {
      transaction.setStatus(ex);
      throw ex;
    } finally {
      transaction.complete();
    }
  }

  private static void schedulePeriodicRefresh() {
    ScheduledExecutorService scheduledExecutorService = Executors
        .newScheduledThreadPool(1, ApolloThreadFactory.create("MetaServiceLocator", true));

    scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          for (String metaServerAddresses : metaServerAddressCache.keySet()) {
            updateMetaServerAddresses(metaServerAddresses);
          }
        } catch (Throwable ex) {
          logger.warn(
              String.format("Refreshing meta server address failed, will retry in %d seconds",
                  REFRESH_INTERVAL_IN_SECOND), ex
          );
        }
      }
    }, REFRESH_INTERVAL_IN_SECOND, REFRESH_INTERVAL_IN_SECOND, TimeUnit.SECONDS);
  }
}
