package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.ctrip.framework.apollo.util.factory.PropertiesFactory;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 配置文件抽象类
 * <pre>
 * 实现了
 * 1）异步通知监听器、
 * 2）计算属性变化等等特性，
 * 是 AbstractConfig + DefaultConfig 的功能子集。
 * </pre>
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public abstract class AbstractConfigFile implements ConfigFile, RepositoryChangeListener {

  /**
   * ExecutorService 对象，用于配置变化时，异步通知 ConfigChangeListener 监听器们
   * <p>
   * 静态属性，所有 Config 共享该线程池。
   */
  private static ExecutorService m_executorService;
  protected final ConfigRepository m_configRepository;
  /**
   * 名称空间名称
   */
  @Getter
  protected final String namespace;
  /**
   * 配置 Properties 的缓存引用
   */
  protected final AtomicReference<Properties> m_configProperties;
  /**
   * 配置变更监听器集合
   */
  private final List<ConfigFileChangeListener> m_listeners = Lists.newCopyOnWriteArrayList();
  /**
   * 构造properties实例的工厂
   */
  protected final PropertiesFactory propertiesFactory;

  /**
   * 配置源类型
   */
  private volatile ConfigSourceType sourceType = ConfigSourceType.NONE;

  static {
    // 初始化执行器
    m_executorService = Executors.newCachedThreadPool(ApolloThreadFactory
        .create("ConfigFile", true));
  }

  /**
   * 初始化AbstractConfigFile
   *
   * @param namespace        名称空间名称
   * @param configRepository 配置Repository
   */
  public AbstractConfigFile(String namespace, ConfigRepository configRepository) {
    m_configRepository = configRepository;
    this.namespace = namespace;
    m_configProperties = new AtomicReference<>();
    propertiesFactory = ApolloInjector.getInstance(PropertiesFactory.class);
    initialize();
  }

  /**
   * 初始化
   */
  private void initialize() {
    try {
      // 初始化 m_configProperties、数据源类型
      m_configProperties.set(m_configRepository.getConfig());
      sourceType = m_configRepository.getSourceType();
    } catch (Throwable ex) {
      Tracer.logError(ex);
      log.warn("Init Apollo Config File failed - namespace: {}, reason: {}.",
          namespace, ExceptionUtil.getDetailMessage(ex));
    } finally {
      //register the change listener no matter config repository is working or not
      //so that whenever config repository is recovered, config could get changed
      // 注册到 ConfigRepository 中，从而实现每次配置发生变更时，更新配置缓存 `m_configProperties`
      m_configRepository.addChangeListener(this);
    }
  }

  /**
   * 更新为【新】值
   *
   * @param newProperties 新properties
   */
  protected abstract void update(Properties newProperties);

  @Override
  public synchronized void onRepositoryChange(String namespace, Properties newProperties) {
    // 忽略，若未变更
    if (newProperties.equals(m_configProperties.get())) {
      return;
    }
    // 读取新的 Properties 对象
    Properties newConfigProperties = propertiesFactory.getPropertiesInstance();
    newConfigProperties.putAll(newProperties);
    // 获得【旧】值
    String oldValue = getContent();
    // 更新为【新】值
    update(newProperties);
    sourceType = m_configRepository.getSourceType();
    // 获得新值
    String newValue = getContent();

    // 计算变化类型
    PropertyChangeType changeType = PropertyChangeType.MODIFIED;
    if (oldValue == null) {
      changeType = PropertyChangeType.ADDED;
    } else if (newValue == null) {
      changeType = PropertyChangeType.DELETED;
    }

    // 通知监听器
    this.fireConfigChange(
        new ConfigFileChangeEvent(this.namespace, oldValue, newValue, changeType));

    Tracer.logEvent("Apollo.Client.ConfigChanges", this.namespace);
  }

  @Override
  public void addChangeListener(ConfigFileChangeListener listener) {
    // 添加配置变更监听器
    if (!m_listeners.contains(listener)) {
      m_listeners.add(listener);
    }
  }

  @Override
  public boolean removeChangeListener(ConfigFileChangeListener listener) {
    // 移除配置变更监听器
    return m_listeners.remove(listener);
  }

  @Override
  public ConfigSourceType getSourceType() {
    return sourceType;
  }

  /**
   * 缓存 ConfigChangeListener 数组
   *
   * @param changeEvent 配置文件更改事件
   */
  private void fireConfigChange(final ConfigFileChangeEvent changeEvent) {
    // 缓存 ConfigChangeListener 数组
    for (final ConfigFileChangeListener listener : m_listeners) {
      m_executorService.submit(new Runnable() {
        @Override
        public void run() {
          String listenerName = listener.getClass().getName();
          Transaction transaction = Tracer
              .newTransaction("Apollo.ConfigFileChangeListener", listenerName);
          try {
            // 通知监听器
            listener.onChange(changeEvent);
            transaction.setStatus(Transaction.SUCCESS);
          } catch (Throwable ex) {
            transaction.setStatus(ex);
            Tracer.logError(ex);
            log.error("Failed to invoke config file change listener {}", listenerName, ex);
          } finally {
            transaction.complete();
          }
        }
      });
    }
  }
}
