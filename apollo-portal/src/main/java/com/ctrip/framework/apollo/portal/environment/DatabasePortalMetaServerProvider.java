package com.ctrip.framework.apollo.portal.environment;

import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据库元数据服务器地址提供者
 * <p>load meta server addressed from database. PortalDB.ServerConfig
 *
 * @author wxq
 */
@Slf4j
class DatabasePortalMetaServerProvider implements PortalMetaServerProvider {

  /**
   * 从数据库读取的配置
   */
  private final PortalConfig portalConfig;
  /**
   * 地址集合
   */
  private volatile Map<Env, String> addresses;

  /**
   * 数据库元数据服务器地址提供者 初始化
   *
   * @param portalConfig 从数据库读取的配置
   */
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
    // 加载数据库的配置数据
    Map<String, String> map = portalConfig.getMetaServers();
    addresses = Env.transformToEnvMap(map);
    log.info("Loaded meta server addresses from portal config: {}", addresses);
  }

}
