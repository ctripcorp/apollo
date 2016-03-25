package com.ctrip.apollo.core;

import java.util.HashMap;
import java.util.Map;

import com.ctrip.apollo.Apollo.Env;

public class MetaDomainConsts {

  public static final String DEFAULT_PORT = "8080";

  public static final String LOCAL = "http://localhost" + ":" + DEFAULT_PORT;

  public static final String DEV =
      "http://10.3.2.56" + ":" + DEFAULT_PORT;

  public static final String FAT =
      "http://ws.meta.apollo.fx.fat.nt.ctripcorp.com" + ":" + DEFAULT_PORT;

  public static final String FWS =
      "http://ws.meta.apollo.fx.fws.nt.ctripcorp.com" + ":" + DEFAULT_PORT;

  public static final String UAT =
      "http://ws.meta.apollo.fx.uat.nt.ctripcorp.com" + ":" + DEFAULT_PORT;

  public static final String LPT =
      "http://ws.meta.apollo.fx.lpt.nt.ctripcorp.com" + ":" + DEFAULT_PORT;

  public static final String TOOLS =
      "http://ws.meta.apollo.fx.tools.ctripcorp.com" + ":" + DEFAULT_PORT;

  public static final String PRD = "http://ws.meta.apollo.fx.ctripcorp.com" + ":" + DEFAULT_PORT;

  private static Map<Env, String> domains = new HashMap<>();

  static {
    domains.put(Env.LOCAL, LOCAL);
    domains.put(Env.DEV, DEV);
    domains.put(Env.FAT, FAT);
    domains.put(Env.FWS, FWS);
    domains.put(Env.UAT, UAT);
    domains.put(Env.LPT, LPT);
    domains.put(Env.TOOLS, TOOLS);
    domains.put(Env.PROD, PRD);
  }

  public static String getDomain(Env env) {
    return domains.get(env);
  }
}
