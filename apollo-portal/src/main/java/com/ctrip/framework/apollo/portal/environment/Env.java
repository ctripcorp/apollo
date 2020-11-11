package com.ctrip.framework.apollo.portal.environment;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * 环境.
 *
 * @author wxq
 */
@AllArgsConstructor
@Data
@Slf4j
public class Env {

  /**
   * 环境的名称，不能为空.
   */
  private String name;

  /**
   * 环境缓存
   */
  private static final Map<String, Env> STRING_ENV_MAP = new ConcurrentHashMap<>();

  // 默认的环境
  /**
   * 本地开发环境
   */
  public static final Env LOCAL = addEnvironment("LOCAL");
  /**
   * 开发环境
   */
  public static final Env DEV = addEnvironment("DEV");
  /**
   * 功能Web服务测试环境
   */
  public static final Env FWS = addEnvironment("FWS");
  /**
   * 功能验收测试环境
   */
  public static final Env FAT = addEnvironment("FAT");
  /**
   * 用户验收测试环境
   */
  public static final Env UAT = addEnvironment("UAT");
  /**
   * 负载和性能测试环境
   */
  public static final Env LPT = addEnvironment("LPT");
  /**
   * 生产环境
   */
  public static final Env PRO = addEnvironment("PRO");
  /**
   * 工具集环境
   */
  public static final Env TOOLS = addEnvironment("TOOLS");
  /**
   * 未知环境.
   */
  public static final Env UNKNOWN = addEnvironment("UNKNOWN");

  /**
   * 对环境名称清空并大写.
   *
   * @param environmentName 环境名称
   * @return 调整后的环境名称
   */
  private static String getWellFormName(String environmentName) {
    return environmentName.trim().toUpperCase();
  }

  /**
   * 将环境名转化为Env
   *
   * @param envName 环境名称
   * @return 环境枚举
   * @see com.ctrip.framework.apollo.core.enums.EnvUtils transformEnv
   */
  public static Env transformEnv(String envName) {
    //缓存中有就从中取
    if (Env.exists(envName)) {
      return Env.valueOf(envName);
    }

    //为空就设置为未知环境
    if (StringUtils.isBlank(envName)) {
      return Env.UNKNOWN;
    }
    switch (envName.trim().toUpperCase()) {
      case "LPT":
        return Env.LPT;
      case "FAT":
      case "FWS":
        return Env.FAT;
      case "UAT":
        return Env.UAT;
      case "PRO":
        //just in case
      case "PROD":
        return Env.PRO;
      case "DEV":
        return Env.DEV;
      case "LOCAL":
        return Env.LOCAL;
      case "TOOLS":
        return Env.TOOLS;
      default:
        return Env.UNKNOWN;
    }
  }

  /**
   * 指定环境是否存在.
   *
   * @param name 环境名
   * @return 如果环境存在，true,否则，false
   */
  public static boolean exists(String name) {
    name = getWellFormName(name);
    return STRING_ENV_MAP.containsKey(name);
  }

  /**
   * 添加环境
   *
   * @param name 环境名称
   * @return 返回添加的环境名称
   */
  public static Env addEnvironment(String name) {
    if (StringUtils.isBlank(name)) {
      throw new RuntimeException("Cannot add a blank environment: " + "[" + name + "]");
    }

    //不存在才添加
    name = getWellFormName(name);
    if (STRING_ENV_MAP.containsKey(name)) {
      // has been existed
      log.debug("{} already exists.", name);
    } else {
      // not existed
      STRING_ENV_MAP.put(name, new Env(name));
    }
    return STRING_ENV_MAP.get(name);
  }

  /**
   * 将环境名称转换为环境枚举
   *
   * @param name 环境名称
   * @return 环境枚举
   * @throws IllegalArgumentException 如果不存在指定的环境，抛出 name
   */
  public static Env valueOf(String name) {
    name = getWellFormName(name);
    if (exists(name)) {
      return STRING_ENV_MAP.get(name);
    } else {
      throw new IllegalArgumentException(name + " not exist");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Env env = (Env) o;
    if (getName().equals(env.getName())) {
      throw new RuntimeException(getName() + " is same environment name, but their Env not same");
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * 将key从 {@link String} 转至 {@link Env}
   *
   * @param metaServerAddresses key 为环境, value是环境的元服务地址
   * @return{@link Env}与元服务地址之间的关系
   */
  static Map<Env, String> transformToEnvMap(Map<String, String> metaServerAddresses) {
    Map<Env, String> map = new ConcurrentHashMap<>();
    metaServerAddresses.forEach((key, value) -> {
      // 添加新的环境
      Env env = Env.addEnvironment(key);
      // 放入键值对
      map.put(env, value);
    });
    return map;
  }


}
