package com.ctrip.framework.apollo.common.datasource;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.enums.EnvUtils;
import com.ctrip.framework.foundation.Foundation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Titan数据库配置
 */
@Component
public class TitanSettings {

  /**
   * fat数据库连接Url
   */
  @Value("${fat.titan.url:}")
  private String fatTitanUrl;

  /**
   * uat数据库连接Url
   */
  @Value("${uat.titan.url:}")
  private String uatTitanUrl;

  /**
   * pro数据库连接Url
   */
  @Value("${pro.titan.url:}")
  private String proTitanUrl;

  /**
   * fat数据库名称
   */
  @Value("${fat.titan.dbname:}")
  private String fatTitanDbname;

  /**
   * uat数据库名称
   */
  @Value("${uat.titan.dbname:}")
  private String uatTitanDbname;

  /**
   * pro数据库名称
   */
  @Value("${pro.titan.dbname:}")
  private String proTitanDbname;

  /**
   * 获取titan数据库连接Url
   *
   * @return titan数据库连接Url
   */
  public String getTitanUrl() {
    // 获取指定环境的数据库连接URL
    Env env = EnvUtils.transformEnv(Foundation.server().getEnvType());
    switch (env) {
      case FAT:
      case FWS:
        return fatTitanUrl;
      case UAT:
        return uatTitanUrl;
      case TOOLS:
      case PRO:
        return proTitanUrl;
      default:
        return "";
    }
  }

  /**
   * 获取titan数据库名称
   *
   * @return titan数据库名称
   */
  public String getTitanDbname() {
    // 获取指定环境的titan数据库名称
    Env env = EnvUtils.transformEnv(Foundation.server().getEnvType());
    switch (env) {
      case FAT:
      case FWS:
        return fatTitanDbname;
      case UAT:
        return uatTitanDbname;
      case TOOLS:
      case PRO:
        return proTitanDbname;
      default:
        return "";
    }
  }

}
