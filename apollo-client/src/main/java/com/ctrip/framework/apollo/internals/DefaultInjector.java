package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.ctrip.framework.apollo.spi.ConfigFactoryManager;
import com.ctrip.framework.apollo.spi.ConfigRegistry;
import com.ctrip.framework.apollo.spi.DefaultConfigFactory;
import com.ctrip.framework.apollo.spi.DefaultConfigFactoryManager;
import com.ctrip.framework.apollo.spi.DefaultConfigRegistry;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.factory.DefaultPropertiesFactory;
import com.ctrip.framework.apollo.util.factory.PropertiesFactory;
import com.ctrip.framework.apollo.util.http.HttpUtil;
import com.ctrip.framework.apollo.util.yaml.YamlParser;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;

/**
 * 基于 Guice 的注入器实现类
 * <p>考虑到 Apollo 会被引入项目中，尽量减少对 Spring 的依赖。但是呢，自身又有 DI 特性的需要，那么引入 Google Guice 是非常好的选择。</p>
 *
 * @author Jason Song(song_s@ctrip.com)
 * @see <a href="https://www.ibm.com/developerworks/cn/java/j-guice.html">《通过 Guice 进行依赖项注入》c</a>
 */
public class DefaultInjector implements Injector {

  /**
   * 注入器
   */
  private com.google.inject.Injector m_injector;

  /**
   * 初始化.
   */
  public DefaultInjector() {
    try {
      m_injector = Guice.createInjector(new ApolloModule());
    } catch (Throwable ex) {
      ApolloConfigException exception = new ApolloConfigException(
          "Unable to initialize Guice Injector!", ex);
      Tracer.logError(exception);
      throw exception;
    }
  }

  @Override
  public <T> T getInstance(Class<T> clazz) {
    try {
      return m_injector.getInstance(clazz);
    } catch (Throwable ex) {
      Tracer.logError(ex);
      throw new ApolloConfigException(
          String.format("Unable to load instance for %s!", clazz.getName()), ex);
    }
  }

  @Override
  public <T> T getInstance(Class<T> clazz, String name) {
    // Guice不支持按类型和名称获取实例
    return null;
  }

  /**
   * apollo模块，告诉 Guice 需要 DI 的配置
   */
  private static class ApolloModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(ConfigManager.class).to(DefaultConfigManager.class).in(Singleton.class);
      bind(ConfigFactoryManager.class).to(DefaultConfigFactoryManager.class).in(Singleton.class);
      bind(ConfigRegistry.class).to(DefaultConfigRegistry.class).in(Singleton.class);
      bind(ConfigFactory.class).to(DefaultConfigFactory.class).in(Singleton.class);
      bind(ConfigUtil.class).in(Singleton.class);
      bind(HttpUtil.class).in(Singleton.class);
      bind(ConfigServiceLocator.class).in(Singleton.class);
      bind(RemoteConfigLongPollService.class).in(Singleton.class);
      bind(YamlParser.class).in(Singleton.class);
      bind(PropertiesFactory.class).to(DefaultPropertiesFactory.class).in(Singleton.class);
    }
  }
}
