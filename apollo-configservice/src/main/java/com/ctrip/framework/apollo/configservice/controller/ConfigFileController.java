package com.ctrip.framework.apollo.configservice.controller;

import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.grayReleaseRule.GrayReleaseRulesHolder;
import com.ctrip.framework.apollo.biz.message.ReleaseMessageListener;
import com.ctrip.framework.apollo.biz.message.Topics;
import com.ctrip.framework.apollo.configservice.util.NamespaceUtil;
import com.ctrip.framework.apollo.configservice.util.WatchKeysUtil;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.core.utils.PropertiesUtil;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/configfiles")
public class ConfigFileController implements ReleaseMessageListener {

  private static final Logger logger = LoggerFactory.getLogger(ConfigFileController.class);
  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
  private static final Splitter X_FORWARDED_FOR_SPLITTER = Splitter.on(",").omitEmptyStrings()
      .trimResults();
  private static final long MAX_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
  private static final long EXPIRE_AFTER_WRITE = 30;
  private final HttpHeaders propertiesResponseHeaders;
  private final HttpHeaders jsonResponseHeaders;
  private final ResponseEntity<String> NOT_FOUND_RESPONSE;
  /**
   * 本地配置缓存，<灰色发布id,配置信息>
   */
  private Cache<String, String> localCache;
  private final Multimap<String, String>
      watchedKeys2CacheKey = Multimaps.synchronizedSetMultimap(HashMultimap.create());
  private final Multimap<String, String>
      cacheKey2WatchedKeys = Multimaps.synchronizedSetMultimap(HashMultimap.create());
  private static final Gson GSON = new Gson();

  private final ConfigController configController;
  private final NamespaceUtil namespaceUtil;
  private final WatchKeysUtil watchKeysUtil;
  private final GrayReleaseRulesHolder grayReleaseRulesHolder;

  public ConfigFileController(
      final ConfigController configController,
      final NamespaceUtil namespaceUtil,
      final WatchKeysUtil watchKeysUtil,
      final GrayReleaseRulesHolder grayReleaseRulesHolder) {
    localCache = CacheBuilder.newBuilder()
        .expireAfterWrite(EXPIRE_AFTER_WRITE, TimeUnit.MINUTES)
        .weigher((Weigher<String, String>) (key, value) -> value == null ? 0 : value.length())
        .maximumWeight(MAX_CACHE_SIZE)
        .removalListener(notification -> {
          String cacheKey = notification.getKey();
          logger.debug("removing cache key: {}", cacheKey);
          if (!cacheKey2WatchedKeys.containsKey(cacheKey)) {
            return;
          }
          //create a new list to avoid ConcurrentModificationException
          List<String> watchedKeys = new ArrayList<>(cacheKey2WatchedKeys.get(cacheKey));
          for (String watchedKey : watchedKeys) {
            watchedKeys2CacheKey.remove(watchedKey, cacheKey);
          }
          cacheKey2WatchedKeys.removeAll(cacheKey);
          logger.debug("removed cache key: {}", cacheKey);
        })
        .build();
    propertiesResponseHeaders = new HttpHeaders();
    propertiesResponseHeaders.add("Content-Type", "text/plain;charset=UTF-8");
    jsonResponseHeaders = new HttpHeaders();
    jsonResponseHeaders.add("Content-Type", "application/json;charset=UTF-8");
    NOT_FOUND_RESPONSE = new ResponseEntity<>(HttpStatus.NOT_FOUND);
    this.configController = configController;
    this.namespaceUtil = namespaceUtil;
    this.watchKeysUtil = watchKeysUtil;
    this.grayReleaseRulesHolder = grayReleaseRulesHolder;
  }

  /**
   * 查询配置信息
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @param namespace   名称空间名称
   * @param dataCenter  数据中心
   * @param clientIp    客户端ip
   * @param request     请求实体
   * @param response    响应实体
   * @return 给定文件格式配置信息
   * @throws IOException
   */
  @GetMapping(value = "/{appId}/{clusterName}/{namespace:.+}")
  public ResponseEntity<String> queryConfigAsProperties(@PathVariable String appId,
      @PathVariable String clusterName, @PathVariable String namespace,
      @RequestParam(value = "dataCenter", required = false) String dataCenter,
      @RequestParam(value = "ip", required = false) String clientIp,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    String result = queryConfig(ConfigFileOutputFormat.PROPERTIES, appId, clusterName, namespace,
        dataCenter, clientIp, request, response);

    // 没有找到
    if (result == null) {
      return NOT_FOUND_RESPONSE;
    }

    return new ResponseEntity<>(result, propertiesResponseHeaders, HttpStatus.OK);
  }

  /**
   * 查询配置JSON对象
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @param namespace   名称空间
   * @param dataCenter  数据中心
   * @param clientIp    客户端id
   * @param request     请求实体
   * @param response    响应实体
   * @return JSON文件格式配置信息
   * @throws IOException 如果发生输入或输出异常,抛出
   */
  @GetMapping(value = "/json/{appId}/{clusterName}/{namespace:.+}")
  public ResponseEntity<String> queryConfigAsJson(@PathVariable String appId,
      @PathVariable String clusterName,
      @PathVariable String namespace,
      @RequestParam(value = "dataCenter", required = false) String dataCenter,
      @RequestParam(value = "ip", required = false) String clientIp,
      HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    String result =
        queryConfig(ConfigFileOutputFormat.JSON, appId, clusterName, namespace, dataCenter,
            clientIp, request, response);
    // 没有找到
    if (result == null) {
      return NOT_FOUND_RESPONSE;
    }

    return new ResponseEntity<>(result, jsonResponseHeaders, HttpStatus.OK);
  }

  /**
   * 查询配置信息
   *
   * @param outputFormat 输出格式
   * @param appId        应用id
   * @param clusterName  集群名称
   * @param namespace    名称空间
   * @param dataCenter   数据中心
   * @param clientIp     客户端IP
   * @param request      请求信息
   * @param response     响应信息
   * @return 给定文件格式配置信息
   * @throws IOException
   */
  String queryConfig(ConfigFileOutputFormat outputFormat, String appId, String clusterName,
      String namespace, String dataCenter, String clientIp,
      HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    // 若 Namespace 名以 .properties 结尾，移除该结尾，并设置到 ApolloConfigNotification 中。例如 application.properties => application 。
    namespace = namespaceUtil.filterNamespaceName(namespace);
    // 获得标准化的 Namespace 名字。因为，客户端 Namespace 会填写错大小写。
    //fix the character case issue, such as FX.apollo <-> fx.apollo
    namespace = namespaceUtil.normalizeNamespace(appId, namespace);

    if (StringUtils.isBlank(clientIp)) {
      clientIp = tryToGetClientIp(request);
    }

    //1.检查客户端应用id、客户端ip和名称空间是否存在灰色发布规则
    boolean hasGrayReleaseRule = grayReleaseRulesHolder.hasGrayReleaseRule(appId, clientIp,
        namespace);

    // 缓存的key
    String cacheKey = assembleCacheKey(outputFormat, appId, clusterName, namespace, dataCenter);

    //2. 尝试加载灰色发布并返回
    if (hasGrayReleaseRule) {
      Tracer.logEvent("ConfigFile.Cache.GrayRelease", cacheKey);
      return loadConfig(outputFormat, appId, clusterName, namespace, dataCenter, clientIp,
          request, response);
    }

    //3. 检查本地缓存是否存在，如果存在，则返回
    String result = localCache.getIfPresent(cacheKey);

    //4. 如果不存在，则从ConfigController加载
    if (Strings.isNullOrEmpty(result)) {
      Tracer.logEvent("ConfigFile.Cache.Miss", cacheKey);
      result = loadConfig(outputFormat, appId, clusterName, namespace, dataCenter, clientIp,
          request, response);

      if (result == null) {
        return null;
      }
      //5. 再次检查此客户端是否需要加载灰度发布信息，如果需要，则从db加载.这一步主要是为了避免缓存污染
      if (grayReleaseRulesHolder.hasGrayReleaseRule(appId, clientIp, namespace)) {
        Tracer.logEvent("ConfigFile.Cache.GrayReleaseConflict", cacheKey);
        return loadConfig(outputFormat, appId, clusterName, namespace, dataCenter, clientIp,
            request, response);
      }

      localCache.put(cacheKey, result);
      logger.debug("adding cache for key: {}", cacheKey);

      Set<String> watchedKeys =
          watchKeysUtil.assembleAllWatchKeys(appId, clusterName, namespace, dataCenter);

      for (String watchedKey : watchedKeys) {
        watchedKeys2CacheKey.put(watchedKey, cacheKey);
      }

      cacheKey2WatchedKeys.putAll(cacheKey, watchedKeys);
      logger.debug("added cache for key: {}", cacheKey);
    } else {
      Tracer.logEvent("ConfigFile.Cache.Hit", cacheKey);
    }

    return result;
  }

  /**
   * 加载配置信息
   *
   * @param outputFormat 输出格式
   * @param appId        应用id
   * @param clusterName  集群名称
   * @param namespace    名称空间名称
   * @param dataCenter   数据中心
   * @param clientIp     客户端ip
   * @param request      请求实体
   * @param response     响应实体
   * @return 加载的配置信息字符串
   * @throws IOException 如果发生输入或输出异常，抛出
   */
  private String loadConfig(ConfigFileOutputFormat outputFormat, String appId, String clusterName,
      String namespace, String dataCenter, String clientIp, HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    // 查询配置
    ApolloConfig apolloConfig = configController.queryConfig(appId, clusterName, namespace,
        dataCenter, "-1", clientIp, null, request, response);

    // 为空直接返回
    if (apolloConfig == null || apolloConfig.getConfigurations() == null) {
      return null;
    }

    String result = null;
    // 根据格式转换
    switch (outputFormat) {
      case PROPERTIES:
        Properties properties = new Properties();
        properties.putAll(apolloConfig.getConfigurations());
        result = PropertiesUtil.toString(properties);
        break;
      case JSON:
        result = GSON.toJson(apolloConfig.getConfigurations());
        break;
    }

    return result;
  }

  /**
   * 组装发布Key
   *
   * @param outputFormat 输出格式
   * @param appId        应用id
   * @param clusterName  集群名称
   * @param namespace    名称空间名称
   * @param dataCenter   数据中心
   * @return 组装后的发布key
   */
  String assembleCacheKey(ConfigFileOutputFormat outputFormat, String appId, String clusterName,
      String namespace, String dataCenter) {
    List<String> keyParts = Lists.newArrayList(outputFormat.getValue(), appId, clusterName,
        namespace);
    if (StringUtils.isNotBlank(dataCenter)) {
      keyParts.add(dataCenter);
    }
    return STRING_JOINER.join(keyParts);
  }

  @Override
  public void handleMessage(ReleaseMessage message, String channel) {
    logger.info("message received - channel: {}, message: {}", channel, message);

    String content = message.getMessage();
    if (!Topics.APOLLO_RELEASE_TOPIC.equals(channel) || Strings.isNullOrEmpty(content)) {
      return;
    }

    if (!watchedKeys2CacheKey.containsKey(content)) {
      return;
    }

    //create a new list to avoid ConcurrentModificationException
    List<String> cacheKeys = new ArrayList<>(watchedKeys2CacheKey.get(content));

    for (String cacheKey : cacheKeys) {
      logger.debug("invalidate cache key: {}", cacheKey);
      localCache.invalidate(cacheKey);
    }
  }

  enum ConfigFileOutputFormat {
    PROPERTIES("properties"), JSON("json");

    private String value;

    ConfigFileOutputFormat(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  private String tryToGetClientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-FORWARDED-FOR");
    if (!Strings.isNullOrEmpty(forwardedFor)) {
      return X_FORWARDED_FOR_SPLITTER.splitToList(forwardedFor).get(0);
    }
    return request.getRemoteAddr();
  }
}
