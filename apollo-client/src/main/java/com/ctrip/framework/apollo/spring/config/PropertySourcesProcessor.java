package com.ctrip.framework.apollo.spring.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.spring.property.AutoUpdateConfigChangeListener;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * Apollo属性源处理器，用于基于Spring注解的应用程序 <br />
 * <p>
 * 之所以PropertySourcesProcessor 实现 {@link BeanFactoryPostProcessor}代替 {@link
 * org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor} 的原因
 * 是较低版本的Spring(例如3.1.1)不支持注册 BeanDefinitionRegistryPostProcessor在 ImportBeanDefinitionRegistrar -
 * {@link com.ctrip.framework.apollo.spring.annotation.ApolloConfigRegistrar}注册
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class PropertySourcesProcessor implements BeanFactoryPostProcessor, EnvironmentAware,
    PriorityOrdered {

  /**
   * 名称空间名称集合
   * <p>
   * KEY：优先级 VALUE：名称空间名称集合
   */
  private static final Multimap<Integer, String> NAMESPACE_NAMES = LinkedHashMultimap.create();
  /**
   * 自动更新初始化的Bean工厂列表
   */
  private static final Set<BeanFactory> AUTO_UPDATE_INITIALIZED_BEAN_FACTORIES = Sets
      .newConcurrentHashSet();

  /**
   * ConfigPropertySource 工厂。在 NAMESPACE_NAMES 中的每一个 Namespace ， 都会创建成对应的 ConfigPropertySource 对象(
   * 基于Apollo Config 的 PropertySource 实现类 )， 并添加到 environment 中。
   * <p><br/>
   * 重点：通过这样的方式，Spring 的 <"property name="" value="" /> 和 @Value 注解， 就可以从 environment
   * 中，直接读取到对应的属性值。
   */
  private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
      .getInstance(ConfigPropertySourceFactory.class);

  /**
   * 配置工具类
   */
  private final ConfigUtil configUtil = ApolloInjector.getInstance(ConfigUtil.class);
  /**
   * Spring ConfigurableEnvironment 对象
   */
  private ConfigurableEnvironment environment;

  /**
   * 添加名称空间名称
   *
   * @param namespaces 名称空间名称
   * @param order      优先级
   * @return true, 添加成功，否则，false
   */
  public static boolean addNamespaces(Collection<String> namespaces, int order) {
    return NAMESPACE_NAMES.putAll(order, namespaces);
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    // 初始化 PropertySource集
    initializePropertySources();
    // 初始化 AutoUpdateConfigChangeListener 对象，实现属性的自动更新
    initializeAutoUpdatePropertiesFeature(beanFactory);
  }

  /**
   * 初始化属性源
   */
  private void initializePropertySources() {
    // 若 `environment` 已经有 APOLLO_PROPERTY_SOURCE_NAME 属性源，说明已经初始化，直接返回。
    if (environment.getPropertySources()
        .contains(PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME)) {
      //already initialized
      return;
    }
    // 创建 CompositePropertySource 对象，组合多个 Namespace 的 ConfigPropertySource 。
    CompositePropertySource composite = new CompositePropertySource(
        PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME);

    //sort by order asc
    // 按照优先级，顺序遍历 Namespace
    ImmutableSortedSet<Integer> orders = ImmutableSortedSet.copyOf(NAMESPACE_NAMES.keySet());
    Iterator<Integer> iterator = orders.iterator();

    while (iterator.hasNext()) {
      int order = iterator.next();
      for (String namespace : NAMESPACE_NAMES.get(order)) {
        // 创建 Apollo Config 对象
        Config config = ConfigService.getConfig(namespace);
        // 创建 Namespace 对应的 ConfigPropertySource 对象
        // 添加到 `composite` 中。
        composite.addPropertySource(
            configPropertySourceFactory.getConfigPropertySource(namespace, config));
      }
    }

    // 清理
    NAMESPACE_NAMES.clear();

    // add after the bootstrap property source or to the first
    // 若有 APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME 属性源，添加到其后
    if (environment.getPropertySources()
        .contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {

      // 确保 ApolloBootstrapPropertySources仍然是第一位的
      // ensure ApolloBootstrapPropertySources is still the first
      ensureBootstrapPropertyPrecedence(environment);

      environment.getPropertySources()
          .addAfter(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME, composite);
    } else {
      // 若没 APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME 属性源，添加到首个
      environment.getPropertySources().addFirst(composite);
    }
  }

  /**
   * 确保APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME属性的优先级
   *
   * @param environment 配置环境
   */
  private void ensureBootstrapPropertyPrecedence(ConfigurableEnvironment environment) {
    MutablePropertySources propertySources = environment.getPropertySources();
    // 获取bootstrap属性
    PropertySource<?> bootstrapPropertySource = propertySources
        .get(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);

    // 不存在或者已经存在，并且为第一条,跳过
    if (bootstrapPropertySource == null
        || propertySources.precedenceOf(bootstrapPropertySource) == 0) {
      return;
    }

    // 删除，并添加至第一条
    propertySources.remove(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
    propertySources.addFirst(bootstrapPropertySource);
  }

  /**
   * 初始化 AutoUpdateConfigChangeListener 对象，实现 Spring Placeholder 的自动更新功能
   *
   * @param beanFactory beanFactory对象
   */
  private void initializeAutoUpdatePropertiesFeature(ConfigurableListableBeanFactory beanFactory) {
    // 若未开启属性的自动更新功能，直接返回
    if (!configUtil.isAutoUpdateInjectedSpringPropertiesEnabled() ||
        !AUTO_UPDATE_INITIALIZED_BEAN_FACTORIES.add(beanFactory)) {
      return;
    }

    // 创建 AutoUpdateConfigChangeListener 对象
    AutoUpdateConfigChangeListener autoUpdateConfigChangeListener = new AutoUpdateConfigChangeListener(
        environment, beanFactory);
    // 循环，向 ConfigPropertySource 注册配置变更器
    List<ConfigPropertySource> configPropertySources = configPropertySourceFactory
        .getAllConfigPropertySources();
    for (ConfigPropertySource configPropertySource : configPropertySources) {
      configPropertySource.addChangeListener(autoUpdateConfigChangeListener);
    }
  }

  @Override
  public void setEnvironment(Environment environment) {
    //由于所有已知环境都是从ConfigurableEnvironment派生而来，因此可以安全地进行强制转换
    this.environment = (ConfigurableEnvironment) environment;
  }

  @Override
  public int getOrder() {
    //make it as early as possible
    // 最高优先级
    return Ordered.HIGHEST_PRECEDENCE;
  }

  /**
   * 重置
   */
  static void reset() {
    // 清空名称空间名称,
    NAMESPACE_NAMES.clear();
    // 清空自动更新初始化的Bean工厂列表
    AUTO_UPDATE_INITIALIZED_BEAN_FACTORIES.clear();
  }
}
