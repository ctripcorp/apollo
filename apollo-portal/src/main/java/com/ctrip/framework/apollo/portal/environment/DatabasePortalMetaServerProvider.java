package com.ctrip.framework.apollo.portal.environment;

import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import java.util.Map;

/**
 * load meta server addressed from database.
 * PortalDB.ServerConfig
 */
class DatabasePortalMetaServerProvider implements PortalMetaServerProvider {

  /**
   * read config from database
   */
  private final PortalConfig portalConfig;

  private volatile Map<Env, String> addresses;

  public DatabasePortalMetaServerProvider(
      final PortalConfig portalConfig
  ) {
    this.portalConfig = portalConfig;
    // will cause NullPointException if portalConfig is null
    Map<String, String> map =  portalConfig.getMetaServers();
    addresses = Env.conversionKey(map);
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
    Map<String, String> map =  portalConfig.getMetaServers();
    addresses = Env.conversionKey(map);
  }

}
