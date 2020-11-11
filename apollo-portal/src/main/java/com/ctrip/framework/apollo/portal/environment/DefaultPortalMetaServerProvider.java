package com.ctrip.framework.apollo.portal.environment;

import static com.ctrip.framework.apollo.portal.environment.Env.transformToEnvMap;

import com.ctrip.framework.apollo.core.utils.ResourceUtils;
import com.ctrip.framework.apollo.portal.util.KeyValueUtils;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认的界面（门户）元服务器提供器
 * <p>
 * 仅用于apollo-portal
 * <p>当apollo-portal启动的时候通过以下方式来加载所有元服务器地址 ->
 * <ul>
 * <li>-系统属性[键以“_meta”结尾（不区分大小写）]</li>
 * <li>-OS环境变量[键以“_meta”结尾（不区分大小写）]</li>
 * <li>-apollo-portal启动时用户的配置文件[密钥以“.meta”（不区分大小写）结尾。</li>
 * </ul>
 *
 * @author wxq
 * @see com.ctrip.framework.apollo.core.internals.LegacyMetaServerProvider
 */
@Slf4j
class DefaultPortalMetaServerProvider implements PortalMetaServerProvider {

  /**
   * 环境及其元服务器地址属性文件路径
   */
  private static final String APOLLO_ENV_PROPERTIES_FILE_PATH = "apollo-env.properties";
  /**
   * 域<env，adress> （环境和地址的集合）
   */
  private volatile Map<Env, String> domains;

  DefaultPortalMetaServerProvider() {
    reload();
  }

  @Override
  public String getMetaServerAddress(Env targetEnv) {
    String metaServerAddress = domains.get(targetEnv);
    return metaServerAddress == null ? null : metaServerAddress.trim();
  }

  @Override
  public boolean exists(Env targetEnv) {
    return domains.containsKey(targetEnv);
  }

  @Override
  public void reload() {
    // 从系统属性、操作系统环境和属性文件加载元服务器地址
    domains = initializeDomains();
    log.info(
        "Loaded meta server addresses from system property, os environment and properties file: {}",
        domains);
  }

  /**
   * 当JVM加载这个类时，动态加载所有环境的元地址
   *
   * @return
   */
  private Map<Env, String> initializeDomains() {

    // add to domain
    Map<Env, String> map = new ConcurrentHashMap<>();
    // 低优先级先添加
    map.putAll(getDomainsFromPropertiesFile());
    map.putAll(getDomainsFromOSEnvironment());
    map.putAll(getDomainsFromSystemProperty());

    // 所有的记录
    return map;
  }

  /**
   * 从系统属性获取元服务器地址域（即<环境,地址>）.
   *
   * @return 返回元服务器地址域<环境, 地址>
   */
  private Map<Env, String> getDomainsFromSystemProperty() {
    // 从key为指定“_meta”结尾的系统属性中查找k-v（不区分大小写）
    Map<String, String> metaServerAddressesFromSystemProperty = KeyValueUtils
        .filterWithKeyIgnoreCaseEndsWith(System.getProperties(), "_meta");
    // 删除key的后缀“_meta”（不区分大小写）
    metaServerAddressesFromSystemProperty = KeyValueUtils
        .removeKeySuffix(metaServerAddressesFromSystemProperty, "_meta".length());

    // 将key从字符串转至Env对象
    return transformToEnvMap(metaServerAddressesFromSystemProperty);
  }

  /**
   * 从系统环境变量获取元服务器地址域（即<环境,地址>）.
   *
   * @return 返回元服务器地址域<环境, 地址>
   */
  private Map<Env, String> getDomainsFromOSEnvironment() {
    // 从key为指定“_meta”结尾的系统环境变量中查找k-v（不区分大小写）
    Map<String, String> metaServerAddressesFromOSEnvironment = KeyValueUtils
        .filterWithKeyIgnoreCaseEndsWith(System.getenv(), "_meta");
    // 删除key的后缀“_meta”（不区分大小写）
    metaServerAddressesFromOSEnvironment = KeyValueUtils
        .removeKeySuffix(metaServerAddressesFromOSEnvironment, "_meta".length());

    // 将key从字符串转至Env对象
    return transformToEnvMap(metaServerAddressesFromOSEnvironment);
  }

  /**
   * 从apollo-portal启动时用户的配置文件获取元服务器地址域（即<环境,地址>）.
   *
   * @return 返回元服务器地址域<环境, 地址>
   */
  private Map<Env, String> getDomainsFromPropertiesFile() {
    // 从key为指定“.meta”结尾的properties中查找k-v（不区分大小写）
    Properties properties = new Properties();
    properties = ResourceUtils.readConfigFile(APOLLO_ENV_PROPERTIES_FILE_PATH, properties);
    Map<String, String> metaServerAddressesFromPropertiesFile = KeyValueUtils
        .filterWithKeyIgnoreCaseEndsWith(properties, ".meta");
    // 删除key的后缀“.meta”（不区分大小写）
    metaServerAddressesFromPropertiesFile = KeyValueUtils
        .removeKeySuffix(metaServerAddressesFromPropertiesFile, ".meta".length());

    // 将key从字符串转至Env对象
    return transformToEnvMap(metaServerAddressesFromPropertiesFile);
  }

}
