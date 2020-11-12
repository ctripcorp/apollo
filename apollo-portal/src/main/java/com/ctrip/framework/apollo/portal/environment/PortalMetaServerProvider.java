package com.ctrip.framework.apollo.portal.environment;

/**
 * 支持多个元服务器地址提供器。 从配置文件、操作系统环境、从数据库...只需实现这个接口.
 *
 * @author wxq
 */
public interface PortalMetaServerProvider {

  /**
   * 通过指定的环境获取远服务器地址.
   *
   * @param targetEnv 指定的环境
   * @return 与环境匹配的元服务器地址
   */
  String getMetaServerAddress(Env targetEnv);

  /**
   * 是否存在指定环境的元服务器地址.
   *
   * @param targetEnv 指定的环境
   * @return 存在指定环境的元服务器地址，true,否则，false
   */
  boolean exists(Env targetEnv);

  /**
   * 在运行时重新加载元服务器地址.
   */
  void reload();

}
