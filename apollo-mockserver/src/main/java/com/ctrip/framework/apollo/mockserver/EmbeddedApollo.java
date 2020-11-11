package com.ctrip.framework.apollo.mockserver;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.framework.apollo.core.utils.ResourceUtils;
import com.ctrip.framework.apollo.internals.ConfigServiceLocator;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.rules.ExternalResource;

/**
 * 嵌入式Apollo Create by zhangzheng on 8/22/18 Email:zhangzheng@youzan.com
 */
@Slf4j
public class EmbeddedApollo extends ExternalResource {

  /**
   * 通知类型
   */
  private static final Type notificationType = new TypeToken<List<ApolloConfigNotification>>() {
  }.getType();

  /**
   * 清除配置服务定位器
   */
  private static Method CONFIG_SERVICE_LOCATOR_CLEAR;
  /**
   * 配置服务定位器
   */
  private static ConfigServiceLocator CONFIG_SERVICE_LOCATOR;

  private static final Gson GSON = new Gson();
  /**
   * 添加或者修改的名称空间的Properties
   */
  private final Map<String, Map<String, String>> addedOrModifiedPropertiesOfNamespace = Maps
      .newConcurrentMap();
  /**
   * 名称空间删除的Key
   */
  private final Map<String, Set<String>> deletedKeysOfNamespace = Maps.newConcurrentMap();

  private MockWebServer server;

  static {
    try {
      System.setProperty("apollo.longPollingInitialDelayInMills", "0");
      CONFIG_SERVICE_LOCATOR = ApolloInjector.getInstance(ConfigServiceLocator.class);
      CONFIG_SERVICE_LOCATOR_CLEAR = ConfigServiceLocator.class
          .getDeclaredMethod("initConfigServices");
      CONFIG_SERVICE_LOCATOR_CLEAR.setAccessible(true);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void before() throws Throwable {
    clear();
    // 虚拟的服务
    server = new MockWebServer();
    final Dispatcher dispatcher = new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        // 轮询通知
        if (request.getPath().startsWith("/notifications/v2")) {
          String notifications = request.getRequestUrl().queryParameter("notifications");
          return new MockResponse().setResponseCode(200).setBody(mockLongPollBody(notifications));
        }
        // 获取配置
        if (request.getPath().startsWith("/configs")) {
          List<String> pathSegments = request.getRequestUrl().pathSegments();
          // appId and cluster might be used in the future
          String appId = pathSegments.get(1);
          String cluster = pathSegments.get(2);
          String namespace = pathSegments.get(3);
          return new MockResponse().setResponseCode(200).setBody(loadConfigFor(namespace));
        }
        return new MockResponse().setResponseCode(404);
      }
    };

    server.setDispatcher(dispatcher);
    server.start();

    mockConfigServiceUrl("http://localhost:" + server.getPort());

    super.before();
  }

  @Override
  protected void after() {
    try {
      // 关闭服务
      clear();
      server.close();
    } catch (Exception e) {
      log.error("stop apollo server error", e);
    }
  }

  /**
   * 清空
   */
  private void clear() {
    resetOverriddenProperties();
  }

  /**
   * 虚拟的配置服务Url
   *
   * @param url 配置服务Url
   * @throws Exception 如果出现异常，抛出
   */
  private void mockConfigServiceUrl(String url) throws Exception {
    System.setProperty("apollo.configService", url);

    CONFIG_SERVICE_LOCATOR_CLEAR.invoke(CONFIG_SERVICE_LOCATOR);
  }

  /**
   * 加载指定名称空间配置
   *
   * @param namespace 指定的名称空间
   * @return 配置Json字符串
   */
  private String loadConfigFor(String namespace) {
    // 文件名称
    String filename = String.format("mockdata-%s.properties", namespace);
    // 指定文件加载的属性
    final Properties prop = ResourceUtils.readConfigFile(filename, new Properties());
    Map<String, String> configurations = Maps.newHashMap();
    for (String propertyName : prop.stringPropertyNames()) {
      configurations.put(propertyName, prop.getProperty(propertyName));
    }
    // 构建配置信息
    ApolloConfig apolloConfig = new ApolloConfig("someAppId", "someCluster", namespace,
        "someReleaseKey");

    Map<String, String> mergedConfigurations = mergeOverriddenProperties(namespace, configurations);
    apolloConfig.setConfigurations(mergedConfigurations);
    return GSON.toJson(apolloConfig);
  }

  /**
   * 虚拟的长轮询
   *
   * @param notificationsStr 通知
   * @return 通知信息
   */
  private String mockLongPollBody(String notificationsStr) {
    // 旧的通知列表
    List<ApolloConfigNotification> oldNotifications = GSON
        .fromJson(notificationsStr, notificationType);
    List<ApolloConfigNotification> newNotifications = new ArrayList<>();
    // 模拟的新通知id
    for (ApolloConfigNotification notification : oldNotifications) {
      newNotifications.add(new ApolloConfigNotification(notification.getNamespaceName(),
          notification.getNotificationId() + 1));
    }
    return GSON.toJson(newNotifications);
  }

  /**
   * 合并用户对namespace的修改
   *
   * @param namespace      名称空间
   * @param configurations 配置集合
   * @return
   */
  private Map<String, String> mergeOverriddenProperties(String namespace,
      Map<String, String> configurations) {
    if (addedOrModifiedPropertiesOfNamespace.containsKey(namespace)) {
      configurations.putAll(addedOrModifiedPropertiesOfNamespace.get(namespace));
    }
    if (deletedKeysOfNamespace.containsKey(namespace)) {
      for (String k : deletedKeysOfNamespace.get(namespace)) {
        configurations.remove(k);
      }
    }
    return configurations;
  }

  /**
   * 添加新属性或更新现有属性
   *
   * @param namespace 名称空间
   * @param someKey   key列表
   * @param someValue 值列表
   */
  public void addOrModifyProperty(String namespace, String someKey, String someValue) {
    if (addedOrModifiedPropertiesOfNamespace.containsKey(namespace)) {
      addedOrModifiedPropertiesOfNamespace.get(namespace).put(someKey, someValue);
    } else {
      Map<String, String> m = Maps.newConcurrentMap();
      m.put(someKey, someValue);
      addedOrModifiedPropertiesOfNamespace.put(namespace, m);
    }
  }

  /**
   * 删除存在属性
   *
   * @param namespace 名称空间
   * @param someKey   key列表
   */
  public void deleteProperty(String namespace, String someKey) {
    if (deletedKeysOfNamespace.containsKey(namespace)) {
      deletedKeysOfNamespace.get(namespace).add(someKey);
    } else {
      Set<String> m = Sets.newConcurrentHashSet();
      m.add(someKey);
      deletedKeysOfNamespace.put(namespace, m);
    }
  }

  /**
   * 重置重写的属性
   */
  public void resetOverriddenProperties() {
    addedOrModifiedPropertiesOfNamespace.clear();
    deletedKeysOfNamespace.clear();
  }
}
