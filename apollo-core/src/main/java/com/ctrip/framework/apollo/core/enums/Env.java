package com.ctrip.framework.apollo.core.enums;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
 * @changeby wxq
 */
public final class  Env {

  private static final Logger logger = LoggerFactory.getLogger(Env.class);

  // name of environment, cannot be null
  private final String name;

  // use to cache Env
  private static final Map<String, Env> STRING_ENV_MAP = new ConcurrentHashMap<>();;

  // default environments
  public static final Env LOCAL = addEnv("LOCAL");
  public static final Env DEV = addEnv("DEV");
  public static final Env FWS = addEnv("FWS");
  public static final Env FAT = addEnv("FAT");
  public static final Env UAT = addEnv("UAT");
  public static final Env LPT = addEnv("LPT");
  public static final Env PRO = addEnv("PRO");
  public static final Env TOOLS = addEnv("TOOLS");
  public static final Env UNKNOWN = addEnv("UNKNOWN");

  /**
   * Cannot create by other
   * @param name
   */
  private Env(String name) {
    this.name = name;
  }

  /**
   * a environment name exist or not
   * @param name
   * @return
   */
  public static boolean exist(String name) {
    name = EnvUtils.getWellFormName(name);
    return STRING_ENV_MAP.containsKey(name);
  }

  /**
   * add an environment
   * @param name
   * @return
   */
  public static Env addEnv(String name) {
    if (StringUtils.isBlank(name)) {
      throw new RuntimeException("Cannot add a blank environment: " + "[" + name + "]");
    }

    name = EnvUtils.getWellFormName(name);
    if(STRING_ENV_MAP.containsKey(name)) {
      // has been existed
      logger.debug("{} already exists.", name);
    } else {
      // not existed
      STRING_ENV_MAP.put(name, new Env(name));
    }
    return STRING_ENV_MAP.get(name);
  }

  /**
   * replace valueOf in enum
   * But what would happened if environment not exist?
   *
   * @param name
   * @throws IllegalArgumentException if this existed environment has no Env with the specified name
   * @return
   */
  public static Env valueOf(String name) {
    name = EnvUtils.getWellFormName(name);
    if(exist(name)) {
      return STRING_ENV_MAP.get(name);
    } else {
      throw new IllegalArgumentException(name + " not exist");
    }
  }

  /**
   * Please use {@code Env.valueOf} instead this method
   * @param env
   * @return
   */
  @Deprecated
  public static Env fromString(String env) {
    Env environment = EnvUtils.transformEnv(env);
    Preconditions.checkArgument(environment != UNKNOWN, String.format("Env %s is invalid", env));
    return environment;
  }

  /**
   * Not just name in Env,
   * the address of Env must be same,
   * or it will throw {@code RuntimeException}
   * @param o
   * @throws RuntimeException When same name but different address
   * @return
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Env env = (Env) o;
    if(getName().equals(env.getName())) {
      throw new RuntimeException(getName() + " is same environment name, but their Env not same");
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }

  /**
   * a Env convert to string, ie its name.
   * @return
   */
  @Override
  public String toString() {
    return name;
  }

  /**
   * Backward compatibility with enum's name method
   * @return
   */
  @Deprecated
  public String name() {
    return name;
  }

  public String getName() {
    return name;
  }
}
