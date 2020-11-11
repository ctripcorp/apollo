package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.factory.PropertiesFactory;
import com.ctrip.framework.apollo.util.function.Functions;
import com.ctrip.framework.apollo.util.parser.Parsers;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Config 抽象类
 * <pre>
 *   实现了
 *   1）缓存读取属性值
 *   2）异步通知监听器
 *   3）计算属性变化等等特性。
 * </pre>
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public abstract class AbstractConfig implements Config {

  /**
   * ExecutorService 对象，用于配置变化时，异步通知 ConfigChangeListener 监听器
   * <p>
   * 静态属性，所有 Config 共享该线程池。
   */
  private static final ExecutorService m_executorService;
  /**
   * ConfigChangeListener 集合
   */
  private final List<ConfigChangeListener> m_listeners = Lists.newCopyOnWriteArrayList();
  /**
   * 感觉兴趣的Key,KEY：配置变更监听器 VALUE：感兴趣的Key
   */
  private final Map<ConfigChangeListener, Set<String>> m_interestedKeys = Maps.newConcurrentMap();
  /**
   * 不感觉兴趣的Key,KEY：配置变更监听器 VALUE：感兴趣的Key的前缀
   */
  private final Map<ConfigChangeListener, Set<String>> m_interestedKeyPrefixes = Maps
      .newConcurrentMap();
  private final ConfigUtil m_configUtil;
  private volatile Cache<String, Integer> m_integerCache;
  private volatile Cache<String, Long> m_longCache;
  private volatile Cache<String, Short> m_shortCache;
  private volatile Cache<String, Float> m_floatCache;
  private volatile Cache<String, Double> m_doubleCache;
  private volatile Cache<String, Byte> m_byteCache;
  private volatile Cache<String, Boolean> m_booleanCache;
  private volatile Cache<String, Date> m_dateCache;
  private volatile Cache<String, Long> m_durationCache;
  /**
   * 数组属性 Cache Map
   * <p>
   * KEY：分隔符 KEY2：属性建
   */
  private final Map<String, Cache<String, String[]>> m_arrayCache;
  /**
   * 上述 Cache 对象集合
   */
  private final List<Cache> allCaches;
  /**
   * 缓存版本号，用于解决更新缓存可能存在的并发问题。详细见 {@link #getValueAndStoreToCache(String, Function, Cache, Object)}
   * 方法
   */
  private final AtomicLong m_configVersion;

  protected PropertiesFactory propertiesFactory;

  static {
    // 构建执行器
    m_executorService = Executors.newCachedThreadPool(ApolloThreadFactory
        .create("Config", true));
  }

  /**
   * 初始化属性
   */
  public AbstractConfig() {
    m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    m_configVersion = new AtomicLong();
    m_arrayCache = Maps.newConcurrentMap();
    allCaches = Lists.newArrayList();
    propertiesFactory = ApolloInjector.getInstance(PropertiesFactory.class);
  }

  @Override
  public void addChangeListener(ConfigChangeListener listener) {
    // 添加配置变更监听器
    addChangeListener(listener, null);
  }

  @Override
  public void addChangeListener(ConfigChangeListener listener, Set<String> interestedKeys) {
    // 添加配置变更监听器
    addChangeListener(listener, interestedKeys, null);
  }

  @Override
  public void addChangeListener(ConfigChangeListener listener, Set<String> interestedKeys,
      Set<String> interestedKeyPrefixes) {
    if (!m_listeners.contains(listener)) {
      // 不存在就添加缓存
      m_listeners.add(listener);
      if (CollectionUtils.isNotEmpty(interestedKeys)) {
        // 设置感兴趣的Key
        m_interestedKeys.put(listener, Sets.newHashSet(interestedKeys));
      }
      if (interestedKeyPrefixes != null && !interestedKeyPrefixes.isEmpty()) {
        // 设置不感兴趣的Key
        m_interestedKeyPrefixes.put(listener, Sets.newHashSet(interestedKeyPrefixes));
      }
    }
  }

  @Override
  public boolean removeChangeListener(ConfigChangeListener listener) {
    // 移除配置变更监听器
    m_interestedKeys.remove(listener);
    m_interestedKeyPrefixes.remove(listener);
    return m_listeners.remove(listener);
  }

  @Override
  public Integer getIntProperty(String key, Integer defaultValue) {
    try {
      // 初始化缓存
      if (m_integerCache == null) {
        synchronized (this) {
          if (m_integerCache == null) {
            m_integerCache = newCache();
          }
        }
      }

      // 从缓存中，读取属性值
      return getValueFromCache(key, Functions.TO_INT_FUNCTION, m_integerCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getIntProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public Long getLongProperty(String key, Long defaultValue) {
    try {
      // 初始化缓存
      if (m_longCache == null) {
        synchronized (this) {
          if (m_longCache == null) {
            m_longCache = newCache();
          }
        }
      }
      // 从缓存中，读取属性值
      return getValueFromCache(key, Functions.TO_LONG_FUNCTION, m_longCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getLongProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public Short getShortProperty(String key, Short defaultValue) {
    try {
      // 初始化缓存
      if (m_shortCache == null) {
        synchronized (this) {
          if (m_shortCache == null) {
            m_shortCache = newCache();
          }
        }
      }
      // 从缓存中，读取属性值
      return getValueFromCache(key, Functions.TO_SHORT_FUNCTION, m_shortCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getShortProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public Float getFloatProperty(String key, Float defaultValue) {
    try {
      // 初始化缓存
      if (m_floatCache == null) {
        synchronized (this) {
          if (m_floatCache == null) {
            m_floatCache = newCache();
          }
        }
      }
      // 从缓存中，读取属性值
      return getValueFromCache(key, Functions.TO_FLOAT_FUNCTION, m_floatCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getFloatProperty for %s failed, return default value %f", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public Double getDoubleProperty(String key, Double defaultValue) {
    try {
      // 初始化缓存
      if (m_doubleCache == null) {
        synchronized (this) {
          if (m_doubleCache == null) {
            m_doubleCache = newCache();
          }
        }
      }
      // 从缓存中，读取属性值
      return getValueFromCache(key, Functions.TO_DOUBLE_FUNCTION, m_doubleCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getDoubleProperty for %s failed, return default value %f", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public Byte getByteProperty(String key, Byte defaultValue) {
    try {
      // 初始化缓存
      if (m_byteCache == null) {
        synchronized (this) {
          if (m_byteCache == null) {
            m_byteCache = newCache();
          }
        }
      }
      // 从缓存中，读取属性值
      return getValueFromCache(key, Functions.TO_BYTE_FUNCTION, m_byteCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getByteProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public Boolean getBooleanProperty(String key, Boolean defaultValue) {
    try {
      // 初始化缓存
      if (m_booleanCache == null) {
        synchronized (this) {
          if (m_booleanCache == null) {
            m_booleanCache = newCache();
          }
        }
      }
      // 从缓存中，读取属性值
      return getValueFromCache(key, Functions.TO_BOOLEAN_FUNCTION, m_booleanCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getBooleanProperty for %s failed, return default value %b", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public String[] getArrayProperty(String key, final String delimiter, String[] defaultValue) {
    try {
      // 初始化缓存
      if (!m_arrayCache.containsKey(delimiter)) {
        synchronized (this) {
          if (!m_arrayCache.containsKey(delimiter)) {
            m_arrayCache.put(delimiter, this.<String[]>newCache());
          }
        }
      }

      Cache<String, String[]> cache = m_arrayCache.get(delimiter);
      String[] result = cache.getIfPresent(key);

      if (result != null) {
        return result;
      }
      // 从缓存中，读取属性值
      return getValueAndStoreToCache(key, new Function<String, String[]>() {
        @Override
        public String[] apply(String input) {
          return input.split(delimiter);
        }
      }, cache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getArrayProperty for %s failed, return default value", key), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public <T extends Enum<T>> T getEnumProperty(String key, Class<T> enumType, T defaultValue) {
    try {
      // 获取值，通过值转换为指定枚举
      String value = getProperty(key, null);

      if (value != null) {
        return Enum.valueOf(enumType, value);
      }
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getEnumProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public Date getDateProperty(String key, Date defaultValue) {
    try {
      // 初始化缓存
      if (m_dateCache == null) {
        synchronized (this) {
          if (m_dateCache == null) {
            m_dateCache = newCache();
          }
        }
      }
      // 从缓存中，读取属性值
      return getValueFromCache(key, Functions.TO_DATE_FUNCTION, m_dateCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getDateProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public Date getDateProperty(String key, String format, Date defaultValue) {
    try {
      // 获取日期字符串，
      String value = getProperty(key, null);

      if (value != null) {
        // 通过日期字符串解析为Date对象
        return Parsers.forDate().parse(value, format);
      }
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getDateProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public Date getDateProperty(String key, String format, Locale locale, Date defaultValue) {
    try {
      // 获取日期字符串，
      String value = getProperty(key, null);

      if (value != null) {
        // 通过日期字符串解析为Date对象
        return Parsers.forDate().parse(value, format, locale);
      }
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getDateProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public long getDurationProperty(String key, long defaultValue) {
    try {
      // 初始化缓存
      if (m_durationCache == null) {
        synchronized (this) {
          if (m_durationCache == null) {
            m_durationCache = newCache();
          }
        }
      }
      // 从缓存中，读取属性值
      return getValueFromCache(key, Functions.TO_DURATION_FUNCTION, m_durationCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getDurationProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  @Override
  public <T> T getProperty(String key, Function<String, T> function, T defaultValue) {
    try {
      // 获取value值，
      String value = getProperty(key, null);

      if (value != null) {
        // 函数式接口调用后返回指定泛型的类型
        return function.apply(value);
      }
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }
    // 默认值
    return defaultValue;
  }

  /**
   * 获取缓存中指定KEY的value
   *
   * @param key          指定的key
   * @param parser       解析器
   * @param cache        缓存对象
   * @param defaultValue 默认的值
   * @param <T>          泛型
   * @return 缓存中指定KEY的value
   */
  private <T> T getValueFromCache(String key, Function<String, T> parser, Cache<String, T> cache,
      T defaultValue) {
    // 指定Key的值
    T result = cache.getIfPresent(key);

    if (result != null) {
      return result;
    }

    // 获取value并存储至缓存
    return getValueAndStoreToCache(key, parser, cache, defaultValue);
  }

  /**
   * 获取value并存储至缓存
   *
   * @param key          属性key
   * @param parser       解析器
   * @param cache        缓存对象
   * @param defaultValue 默认值
   * @param <T>          泛型
   * @return 缓存中获取的Value
   */
  private <T> T getValueAndStoreToCache(String key, Function<String, T> parser,
      Cache<String, T> cache, T defaultValue) {
    // 获得当前版本号
    long currentConfigVersion = m_configVersion.get();
    // 获得属性值
    String value = getProperty(key, null);
    // 若获得到属性，返回该属性值
    if (value != null) {
      // 解析属性值
      T result = parser.apply(value);
      // 若解析成功
      if (result != null) {
        // 若版本号未变化，则更新到缓存，从而解决并发的问题。
        synchronized (this) {
          if (m_configVersion.get() == currentConfigVersion) {
            cache.put(key, result);
          }
        }
        // 返回属性值
        return result;
      }
    }
    // 获得不到属性值，返回默认值
    return defaultValue;
  }

  /**
   * 创建 Cache 对象
   *
   * @param <T> 泛型
   * @return 缓存对象
   */
  private <T> Cache<String, T> newCache() {
    // 创建 Cache 对象
    Cache<String, T> cache = CacheBuilder.newBuilder()
        .maximumSize(m_configUtil.getMaxConfigCacheSize())
        .expireAfterAccess(m_configUtil.getConfigCacheExpireTime(),
            m_configUtil.getConfigCacheExpireTimeUnit())
        .build();
    // 添加到 Cache 集合
    allCaches.add(cache);
    return cache;
  }

  /**
   * 清空配置缓存
   */
  protected void clearConfigCache() {
    synchronized (this) {
      // 过期缓存
      for (Cache c : allCaches) {
        if (c != null) {
          c.invalidateAll();
        }
      }
      // 新增版本号
      m_configVersion.incrementAndGet();
    }
  }

  protected void fireConfigChange(final ConfigChangeEvent changeEvent) {
    // 缓存 ConfigChangeListener 数组
    for (final ConfigChangeListener listener : m_listeners) {
      // 检查侦听器是否对该更改事件感兴趣
      if (!isConfigChangeListenerInterested(listener, changeEvent)) {
        continue;
      }
      m_executorService.submit(new Runnable() {
        @Override
        public void run() {
          String listenerName = listener.getClass().getName();
          Transaction transaction = Tracer
              .newTransaction("Apollo.ConfigChangeListener", listenerName);
          try {
            // 通知监听器
            listener.onChange(changeEvent);
            transaction.setStatus(Transaction.SUCCESS);
          } catch (Throwable ex) {
            transaction.setStatus(ex);
            Tracer.logError(ex);
            log.error("Failed to invoke config change listener {}", listenerName, ex);
          } finally {
            transaction.complete();
          }
        }
      });
    }
  }

  /**
   * 配置更改监听器是否对该更改事件感兴趣
   *
   * @param configChangeListener 配置更改监听器
   * @param configChangeEvent    配置更改事件
   * @return true, 感兴趣，否则，false
   */
  private boolean isConfigChangeListenerInterested(ConfigChangeListener configChangeListener,
      ConfigChangeEvent configChangeEvent) {
    Set<String> interestedKeys = m_interestedKeys.get(configChangeListener);
    Set<String> interestedKeyPrefixes = m_interestedKeyPrefixes.get(configChangeListener);

    // 感兴趣的Key的前缀为空表示对所有键都感兴趣
    if (CollectionUtils.isEmpty(interestedKeys)
        && CollectionUtils.isEmpty(interestedKeyPrefixes)) {
      return true;
    }

    // 感兴趣的Key如果有更改事件，返回true
    if (interestedKeys != null) {
      for (String interestedKey : interestedKeys) {
        if (configChangeEvent.isChanged(interestedKey)) {
          return true;
        }
      }
    }

    // 不感兴趣的Key如果有更改事件，返回true
    if (interestedKeyPrefixes != null) {
      for (String prefix : interestedKeyPrefixes) {
        for (final String changedKey : configChangeEvent.changedKeys()) {
          if (changedKey.startsWith(prefix)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * 计算配置变更Properties
   *
   * @param namespace 名称空间名称
   * @param previous  之前的Properties
   * @param current   当前的Properties
   * @return 配置变更的列表
   */
  List<ConfigChange> calcPropertyChanges(String namespace, Properties previous,
      Properties current) {
    if (previous == null) {
      previous = propertiesFactory.getPropertiesInstance();
    }

    if (current == null) {
      current = propertiesFactory.getPropertiesInstance();
    }

    // 之前的key
    Set<String> previousKeys = previous.stringPropertyNames();
    // 当前的key
    Set<String> currentKeys = current.stringPropertyNames();

    // 共有的Key
    Set<String> commonKeys = Sets.intersection(previousKeys, currentKeys);
    // 新的key
    Set<String> newKeys = Sets.difference(currentKeys, commonKeys);
    // 要删除的Key
    Set<String> removedKeys = Sets.difference(previousKeys, commonKeys);

    List<ConfigChange> changes = Lists.newArrayList();
    // 计算新增的
    for (String newKey : newKeys) {
      changes.add(new ConfigChange(namespace, newKey, null, current.getProperty(newKey),
          PropertyChangeType.ADDED));
    }
    // 计算移除的
    for (String removedKey : removedKeys) {
      changes.add(new ConfigChange(namespace, removedKey, previous.getProperty(removedKey), null,
          PropertyChangeType.DELETED));
    }
    // 计算修改的
    for (String commonKey : commonKeys) {
      String previousValue = previous.getProperty(commonKey);
      String currentValue = current.getProperty(commonKey);
      if (Objects.equal(previousValue, currentValue)) {
        continue;
      }
      //添加配置变更信息
      changes.add(new ConfigChange(namespace, commonKey, previousValue, currentValue,
          PropertyChangeType.MODIFIED));
    }
    return changes;
  }
}
