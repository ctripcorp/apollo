package com.ctrip.framework.apollo.portal.environment;

/**
 * For the supporting of multiple meta server address providers.
 * From configuration file,
 * from OS environment,
 * From database,
 * ...
 * Just implement this interface
 * @author wxq
 */
public interface PortalMetaServerProvider {

  /**
   * @param targetEnv environment
   * @return meta server address matched environment
   */
  String getMetaServerAddress(Env targetEnv);

  /**
   * @param targetEnv environment
   * @return environment's meta server address exists or not
   */
  boolean exists(Env targetEnv);

  /**
   * reload the meta server address in runtime
   */
  void reload();

}
