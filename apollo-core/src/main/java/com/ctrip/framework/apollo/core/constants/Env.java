package com.ctrip.framework.apollo.core.constants;

import com.ctrip.framework.apollo.core.utils.EnvUtils;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Here is the brief description for all the predefined environments:
 * <ul>
 *   <li>LOCAL: Local Development environment, assume you are working at the beach with no network access</li>
 *   <li>DEV: Development environment</li>
 *   <li>FWS: Feature Web Service Test environment</li>
 *   <li>FAT: Feature Acceptance Test environment</li>
 *   <li>UAT: User Acceptance Test environment</li>
 *   <li>LPT: Load and Performance Test environment</li>
 *   <li>PRO: Production environment</li>
 *   <li>TOOLS: Tooling environment, a special area in production environment which allows
 * access to test environment, e.g. Apollo Portal should be deployed in tools environment</li>
 * </ul>
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public final class Env {

  private static final Logger logger = LoggerFactory.getLogger(Env.class);

  public static final String LOCAL = Env.valueOf("LOCAL");
  public static final String DEV = Env.valueOf("DEV");
  public static final String FWS = Env.valueOf("FWS");
  public static final String FAT = Env.valueOf("FAT");
  public static final String UAT = Env.valueOf("UAT");
  public static final String LPT = Env.valueOf("LPT");
  public static final String PRO = Env.valueOf("PRO");
  public static final String TOOLS = Env.valueOf("TOOLS");
  public static final String UNKNOWN = Env.valueOf("UNKNOWN");

  private Env() {

  }

  public static String valueOf(String environmentName) {
    if(StringUtils.isBlank(environmentName)) {
      logger.warn("You pass a blank environment name: [{}], so give you a {}", environmentName, Env.UNKNOWN);
      return Env.UNKNOWN;
    } else {
      return EnvUtils.getWellFormName(environmentName);
    }
  }

  /**
   * ie valueOf method
   * @param env
   * @return
   */
  public static String fromString(String env) {
    return Env.valueOf(env);
  }
}
