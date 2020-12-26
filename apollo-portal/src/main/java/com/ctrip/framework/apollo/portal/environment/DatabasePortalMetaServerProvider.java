package com.ctrip.framework.apollo.portal.environment;

import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * load meta server addressed from database.
 * PortalDB.ServerConfig
 */
class DatabasePortalMetaServerProvider implements PortalMetaServerProvider {
  private static final Logger logger = LoggerFactory.getLogger(DatabasePortalMetaServerProvider.class);

  /**
   * read config from database
   */
  private final PortalConfig portalConfig;

  private volatile Map<Env, String> addresses;

  DatabasePortalMetaServerProvider(final PortalConfig portalConfig) {
    this.portalConfig = portalConfig;
    reload();
  }

  @Override
  public String getMetaServerAddress(Env targetEnv) {
    return addresses.get(targetEnv);
  }

  @Override
  public boolean exists(Env targetEnv) {
    return addresses.containsKey(targetEnv);
  }

  @Override
  public void reload() {
    Map<String, String> map = portalConfig.getMetaServers();
    addresses = Env.transformToEnvMap(map);
    logger.info("Loaded meta server addresses from portal config: {}", addresses);
  }

}
