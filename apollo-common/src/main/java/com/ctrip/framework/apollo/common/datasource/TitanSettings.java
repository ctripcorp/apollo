package com.ctrip.framework.apollo.common.datasource;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.enums.EnvUtils;
import com.ctrip.framework.foundation.Foundation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TitanSettings {

  @Value("${fat.titan.url:}")
  private String fatTitanUrl;

  @Value("${uat.titan.url:}")
  private String uatTitanUrl;

  @Value("${pro.titan.url:}")
  private String proTitanUrl;

  @Value("${fat.titan.dbname:}")
  private String fatTitanDbname;

  @Value("${uat.titan.dbname:}")
  private String uatTitanDbname;

  @Value("${pro.titan.dbname:}")
  private String proTitanDbname;

  public String getTitanUrl() {
    Env env = EnvUtils.transformEnv(Foundation.server().getEnvType());
    if (Env.FAT.equals(env) || Env.FWS.equals(env)) {
      return fatTitanUrl;
    } else if (Env.UAT.equals(env)) {
      return uatTitanUrl;
    } else if (Env.TOOLS.equals(env) || Env.PRO.equals(env)) {
      return proTitanUrl;
    }
    return "";
  }

  public String getTitanDbname() {
    Env env = EnvUtils.transformEnv(Foundation.server().getEnvType());
    if (Env.FAT.equals(env) || Env.FWS.equals(env)) {
      return fatTitanDbname;
    } else if (Env.UAT.equals(env)) {
      return uatTitanDbname;
    } else if (Env.TOOLS.equals(env) || Env.PRO.equals(env)) {
      return proTitanDbname;
    }
    return "";
  }

}
