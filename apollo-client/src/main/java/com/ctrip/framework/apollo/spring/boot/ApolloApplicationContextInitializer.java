package com.ctrip.framework.apollo.spring.boot;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.ctrip.framework.apollo.util.factory.PropertiesFactory;
import com.google.common.base.Splitter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 初始化apollo系统属性并在springboot bootstrap阶段注入apollo配置
 * <p>
 * 在 Spring Boot 启动阶段( bootstrap phase )，注入配置的 Apollo Config 对象集 <p配置示例:</p>
 * <pre class="code">
 *   # 设置应用id
 *   app.id = 100004458
 *   # 启用apollo引导配置并在引导阶段插入“application”命名空间
 *   apollo.bootstrap.enabled = true
 * </pre>
 * <p>
 * or
 *
 * <pre class="code">
 *   # 设置应用id
 *   app.id = 100004458
 *   # 启用apollo bootstrap配置
 *   apollo.bootstrap.enabled = true
 *   # 将注入'application'和'FX.apollo'bootstrap的名称空间
 *   apollo.bootstrap.namespaces = application,FX.apollo
 * </pre>
 * <p>
 * <p>
 * 如果要在系统初始化阶段之前加载Apollo配置，请添加
 * <pre class="code">
 *   # 设置启用 apollo.bootstrap.eagerLoad.enabled
 *   apollo.bootstrap.eagerLoad.enabled = true
 * </pre>
 * <p>
 * 当您的日志配置由Apollo设置时，这将非常有用。
 * <p>例如，您定义了logback-spring.xml文件在你的项目中，你想在注入一些属性logback-spring.xml文件.
 */
@Slf4j
public class ApolloApplicationContextInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext>, EnvironmentPostProcessor,
    Ordered {

  /**
   * 默认的优先级
   */
  public static final int DEFAULT_ORDER = 0;

  /**
   * 名称空间分隔器
   */
  private static final Splitter NAMESPACE_SPLITTER = Splitter.on(",").omitEmptyStrings()
      .trimResults();
  /**
   * Apollo系统属性集
   */
  private static final String[] APOLLO_SYSTEM_PROPERTIES = {"app.id",
      ConfigConsts.APOLLO_CLUSTER_KEY,
      "apollo.cacheDir", "apollo.accesskey.secret", ConfigConsts.APOLLO_META_KEY,
      PropertiesFactory.APOLLO_PROPERTY_ORDER_ENABLE};

  /**
   * 配置属性源工厂
   */
  private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
      .getInstance(ConfigPropertySourceFactory.class);

  /**
   * 默认的优先级
   */
  private int order = DEFAULT_ORDER;

  @Override
  public void initialize(ConfigurableApplicationContext context) {
    ConfigurableEnvironment environment = context.getEnvironment();

    // 忽略，若未开启
    if (!environment
        .getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED, Boolean.class, false)) {
      log.debug("Apollo bootstrap config is not enabled for context {}, see property: ${{}}",
          context, PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED);
      return;
    }
    log.debug("Apollo bootstrap config is enabled for context {}", context);

    // 初始化
    initialize(environment);
  }


  /**
   * 在环境准备好之后初始化Apollo配置.
   *
   * @param environment 配置环境
   */
  protected void initialize(ConfigurableEnvironment environment) {
    // 忽略，若已经有 APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME 的 PropertySource
    if (environment.getPropertySources()
        .contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
      //already initialized
      return;
    }

    // 获得 "apollo.bootstrap.namespaces" 配置项
    String namespaces = environment
        .getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES,
            ConfigConsts.NAMESPACE_APPLICATION);
    log.debug("Apollo bootstrap namespaces: {}", namespaces);
    List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(namespaces);

    // 按照优先级，顺序遍历 Namespace
    CompositePropertySource composite = new CompositePropertySource(
        PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
    for (String namespace : namespaceList) {
      // 创建 Apollo Config 对象
      Config config = ConfigService.getConfig(namespace);
      // 创建 Namespace 对应的 ConfigPropertySource 对象，添加到 `composite` 中。
      composite.addPropertySource(
          configPropertySourceFactory.getConfigPropertySource(namespace, config));
    }
    // 添加到 `environment` 中，且优先级最高
    environment.getPropertySources().addFirst(composite);
  }

  /**
   * 从环境配置填充系统属性
   *
   * @param environment 配置环境
   */

  void initializeSystemProperty(ConfigurableEnvironment environment) {
    for (String propertyName : APOLLO_SYSTEM_PROPERTIES) {
      fillSystemPropertyFromEnvironment(environment, propertyName);
    }
  }

  /**
   * 填充系统属性环境
   *
   * @param environment  配置环境
   * @param propertyName 属性名称
   */
  private void fillSystemPropertyFromEnvironment(ConfigurableEnvironment environment,
      String propertyName) {
    // 系统属性中已经存在了，跳过
    if (System.getProperty(propertyName) != null) {
      return;
    }

    // 从环境中获取为空，跳过
    String propertyValue = environment.getProperty(propertyName);
    if (StringUtils.isEmpty(propertyValue)) {
      return;
    }
    System.setProperty(propertyName, propertyValue);
  }

  /**
   * 为了在Spring加载日志系统阶段之前就加载Apollo配置，可以在ConfigFileApplicationListener成功之后调用此环境后处理器。
   * <p><br />
   * 处理顺序如下：加载引导属性和应用程序属性 --> Load Apollo配置属性 --> Initialize Logging systems
   *
   * @param configurableEnvironment 配置环境
   * @param springApplication       SpringApplication对象
   */
  @Override
  public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment,
      SpringApplication springApplication) {

    //应该始终初始化系统属性，例如首先应用id
    initializeSystemProperty(configurableEnvironment);

    // 获得 "apollo.bootstrap.enabled" 配置项
    Boolean eagerLoadEnabled = configurableEnvironment
        .getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_EAGER_LOAD_ENABLED, Boolean.class,
            false);

    // EnvironmentPostProcessor should not be triggered if you don't want Apollo Loading before Logging System Initialization
    // 忽略，若未开启
    if (!eagerLoadEnabled) {
      return;
    }

    // 获得 "apollo.bootstrap.enabled" 配置项
    Boolean bootstrapEnabled = configurableEnvironment
        .getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED, Boolean.class, false);

    if (bootstrapEnabled) {
      initialize(configurableEnvironment);
    }

  }

  /**
   * @since 1.3.0
   */
  @Override
  public int getOrder() {
    return order;
  }

  /**
   * @since 1.3.0
   */
  public void setOrder(int order) {
    this.order = order;
  }
}
