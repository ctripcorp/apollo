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
import com.ctrip.framework.apollo.util.function.Functions;
import com.ctrip.framework.apollo.util.parser.Parsers;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractConfig implements Config {
  private static final Logger logger = LoggerFactory.getLogger(AbstractConfig.class);

  private static ExecutorService m_executorService;

  private List<ConfigChangeListener> m_listeners = Lists.newCopyOnWriteArrayList();
  private ConfigUtil m_configUtil;
  private volatile Cache<String, Integer> m_integerCache;
  private volatile Cache<String, Long> m_longCache;
  private volatile Cache<String, Short> m_shortCache;
  private volatile Cache<String, Float> m_floatCache;
  private volatile Cache<String, Double> m_doubleCache;
  private volatile Cache<String, Byte> m_byteCache;
  private volatile Cache<String, Boolean> m_booleanCache;
  private volatile Cache<String, Date> m_dateCache;
  private volatile Cache<String, Long> m_durationCache;
  private Map<String, Cache<String, String[]>> m_arrayCache;
  private Map<String, Cache<String, List<String>>> m_stringListCache;
  private Map<String, Cache<String, List<Integer>>> m_integerListCache;
  private Map<String, Cache<String, List<Long>>> m_longListCache;
  private Map<String, Cache<String, List<Short>>> m_shortListCache;
  private Map<String, Cache<String, List<Byte>>> m_byteListCache;
  private Map<String, Cache<String, List<Float>>> m_floatListCache;
  private Map<String, Cache<String, List<Boolean>>> m_booleanListCache;
  private Map<String, Cache<String, List<Double>>> m_doubleListCache;
  private List<Cache> allCaches;
  private AtomicLong m_configVersion; //indicate config version

  static {
    m_executorService = Executors.newCachedThreadPool(ApolloThreadFactory
        .create("Config", true));
  }

  public AbstractConfig() {
      m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
      m_configVersion = new AtomicLong();
      m_arrayCache = Maps.newConcurrentMap();
      allCaches = Lists.newArrayList();
  }

  @Override
  public void addChangeListener(ConfigChangeListener listener) {
    if (!m_listeners.contains(listener)) {
      m_listeners.add(listener);
    }
  }

  @Override
  public Integer getIntProperty(String key, Integer defaultValue) {
    try {
      if (m_integerCache == null) {
        synchronized (this) {
          if (m_integerCache == null) {
            m_integerCache = newCache();
          }
        }
      }

      return getValueFromCache(key, Functions.TO_INT_FUNCTION, m_integerCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getIntProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public Long getLongProperty(String key, Long defaultValue) {
    try {
      if (m_longCache == null) {
        synchronized (this) {
          if (m_longCache == null) {
            m_longCache = newCache();
          }
        }
      }

      return getValueFromCache(key, Functions.TO_LONG_FUNCTION, m_longCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getLongProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public Short getShortProperty(String key, Short defaultValue) {
    try {
      if (m_shortCache == null) {
        synchronized (this) {
          if (m_shortCache == null) {
            m_shortCache = newCache();
          }
        }
      }

      return getValueFromCache(key, Functions.TO_SHORT_FUNCTION, m_shortCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getShortProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public Float getFloatProperty(String key, Float defaultValue) {
    try {
      if (m_floatCache == null) {
        synchronized (this) {
          if (m_floatCache == null) {
            m_floatCache = newCache();
          }
        }
      }

      return getValueFromCache(key, Functions.TO_FLOAT_FUNCTION, m_floatCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getFloatProperty for %s failed, return default value %f", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public Double getDoubleProperty(String key, Double defaultValue) {
    try {
      if (m_doubleCache == null) {
        synchronized (this) {
          if (m_doubleCache == null) {
            m_doubleCache = newCache();
          }
        }
      }

      return getValueFromCache(key, Functions.TO_DOUBLE_FUNCTION, m_doubleCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getDoubleProperty for %s failed, return default value %f", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public Byte getByteProperty(String key, Byte defaultValue) {
    try {
      if (m_byteCache == null) {
        synchronized (this) {
          if (m_byteCache == null) {
            m_byteCache = newCache();
          }
        }
      }

      return getValueFromCache(key, Functions.TO_BYTE_FUNCTION, m_byteCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getByteProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public Boolean getBooleanProperty(String key, Boolean defaultValue) {
    try {
      if (m_booleanCache == null) {
        synchronized (this) {
          if (m_booleanCache == null) {
            m_booleanCache = newCache();
          }
        }
      }

      return getValueFromCache(key, Functions.TO_BOOLEAN_FUNCTION, m_booleanCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getBooleanProperty for %s failed, return default value %b", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public String[] getArrayProperty(String key, final String delimiter, String[] defaultValue) {
    try {
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
    return defaultValue;
  }

  @Override
  public <T extends Enum<T>> T getEnumProperty(String key, Class<T> enumType, T defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Enum.valueOf(enumType, value);
      }
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getEnumProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }

    return defaultValue;
  }

  @Override
  public Date getDateProperty(String key, Date defaultValue) {
    try {
      if (m_dateCache == null) {
        synchronized (this) {
          if (m_dateCache == null) {
            m_dateCache = newCache();
          }
        }
      }

      return getValueFromCache(key, Functions.TO_DATE_FUNCTION, m_dateCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getDateProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }

    return defaultValue;
  }

  @Override
  public Date getDateProperty(String key, String format, Date defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Parsers.forDate().parse(value, format);
      }
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getDateProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }

    return defaultValue;
  }

  @Override
  public Date getDateProperty(String key, String format, Locale locale, Date defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Parsers.forDate().parse(value, format, locale);
      }
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getDateProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }

    return defaultValue;
  }

  @Override
  public long getDurationProperty(String key, long defaultValue) {
    try {
      if (m_durationCache == null) {
        synchronized (this) {
          if (m_durationCache == null) {
            m_durationCache = newCache();
          }
        }
      }

      return getValueFromCache(key, Functions.TO_DURATION_FUNCTION, m_durationCache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
          String.format("getDurationProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }

    return defaultValue;
  }

  @Override
  public List<String> getStringListProperty(String key, final String delimiter, List<String> defaultValue) {
    try {
      if (!m_stringListCache.containsKey(delimiter)) {
        synchronized (this) {
          if (!m_stringListCache.containsKey(delimiter)) {
            m_stringListCache.put(delimiter, this.<List<String>>newCache());
          }
        }
      }

      Cache<String, List<String>> cache = m_stringListCache.get(delimiter);
      List<String> result = cache.getIfPresent(key);

      if (result != null) {
        return result;
      }

      return getValueAndStoreToCache(key, new Function<String, List<String>>() {
        @Override
        public List<String> apply(String input) {
          return Splitter.on(delimiter).trimResults().omitEmptyStrings().splitToList(input);
        }
      }, cache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
              String.format("getStringListProperty for %s failed, return default value", key), ex));
    }
    return defaultValue;
  }

  @Override
  public List<Integer> getIntegerListProperty(String key, final String delimiter, List<Integer> defaultValue) {
    try {
      if (!m_integerListCache.containsKey(delimiter)) {
        synchronized (this) {
          if (!m_integerListCache.containsKey(delimiter)) {
            m_integerListCache.put(delimiter, this.<List<Integer>>newCache());
          }
        }
      }

      Cache<String, List<Integer>> cache = m_integerListCache.get(delimiter);
      List<Integer> result = cache.getIfPresent(key);

      if (result != null) {
        return result;
      }

      return getValueAndStoreToCache(key, new Function<String, List<Integer>>() {
        @Override
        public List<Integer> apply(String input) {
          return Lists.transform(Splitter.on(delimiter).trimResults().omitEmptyStrings().splitToList(input),
                  Functions.TO_INT_FUNCTION);
        }
      }, cache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
              String.format("getIntegerListProperty for %s failed, return default value", key), ex));
    }
    return defaultValue;
  }

  @Override
  public List<Long> getLongListProperty(String key, final String delimiter, List<Long> defaultValue) {
    try {
      if (!m_longListCache.containsKey(delimiter)) {
        synchronized (this) {
          if (!m_longListCache.containsKey(delimiter)) {
            m_longListCache.put(delimiter, this.<List<Long>>newCache());
          }
        }
      }

      Cache<String, List<Long>> cache = m_longListCache.get(delimiter);
      List<Long> result = cache.getIfPresent(key);

      if (result != null) {
        return result;
      }

      return getValueAndStoreToCache(key, new Function<String, List<Long>>() {
        @Override
        public List<Long> apply(String input) {
          return Lists.transform(Splitter.on(delimiter).trimResults().omitEmptyStrings().splitToList(input),
                  Functions.TO_LONG_FUNCTION);
        }
      }, cache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
              String.format("getLongListProperty for %s failed, return default value", key), ex));
    }
    return defaultValue;
  }

  @Override
  public List<Double> getDoubleListProperty(String key, final String delimiter, List<Double> defaultValue) {
    try {
      if (!m_doubleListCache.containsKey(delimiter)) {
        synchronized (this) {
          if (!m_doubleListCache.containsKey(delimiter)) {
            m_doubleListCache.put(delimiter, this.<List<Double>>newCache());
          }
        }
      }

      Cache<String, List<Double>> cache = m_doubleListCache.get(delimiter);
      List<Double> result = cache.getIfPresent(key);

      if (result != null) {
        return result;
      }

      return getValueAndStoreToCache(key, new Function<String, List<Double>>() {
        @Override
        public List<Double> apply(String input) {
          return Lists.transform(Splitter.on(delimiter).trimResults().omitEmptyStrings().splitToList(input),
                  Functions.TO_DOUBLE_FUNCTION);
        }
      }, cache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
              String.format("getDoubleListProperty for %s failed, return default value", key), ex));
    }
    return defaultValue;
  }

  @Override
  public List<Float> getFloatListProperty(String key, final String delimiter, List<Float> defaultValue) {
    try {
      if (!m_floatListCache.containsKey(delimiter)) {
        synchronized (this) {
          if (!m_floatListCache.containsKey(delimiter)) {
            m_floatListCache.put(delimiter, this.<List<Float>>newCache());
          }
        }
      }

      Cache<String, List<Float>> cache = m_floatListCache.get(delimiter);
      List<Float> result = cache.getIfPresent(key);

      if (result != null) {
        return result;
      }

      return getValueAndStoreToCache(key, new Function<String, List<Float>>() {
        @Override
        public List<Float> apply(String input) {
          return Lists.transform(Splitter.on(delimiter).trimResults().omitEmptyStrings().splitToList(input),
                  Functions.TO_FLOAT_FUNCTION);
        }
      }, cache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
              String.format("getFloatListProperty for %s failed, return default value", key), ex));
    }
    return defaultValue;
  }

  @Override
  public List<Boolean> getBooleanListProperty(String key, final String delimiter, List<Boolean> defaultValue) {
    try {
      if (!m_booleanListCache.containsKey(delimiter)) {
        synchronized (this) {
          if (!m_booleanListCache.containsKey(delimiter)) {
            m_booleanListCache.put(delimiter, this.<List<Boolean>>newCache());
          }
        }
      }

      Cache<String, List<Boolean>> cache = m_booleanListCache.get(delimiter);
      List<Boolean> result = cache.getIfPresent(key);

      if (result != null) {
        return result;
      }

      return getValueAndStoreToCache(key, new Function<String, List<Boolean>>() {
        @Override
        public List<Boolean> apply(String input) {
          return Lists.transform(Splitter.on(delimiter).trimResults().omitEmptyStrings().splitToList(input),
                  Functions.TO_BOOLEAN_FUNCTION);
        }
      }, cache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
              String.format("getBooleanListProperty for %s failed, return default value", key), ex));
    }
    return defaultValue;
  }

  @Override
  public List<Byte> getByteListProperty(String key, final String delimiter, List<Byte> defaultValue) {
    try {
      if (!m_byteListCache.containsKey(delimiter)) {
        synchronized (this) {
          if (!m_byteListCache.containsKey(delimiter)) {
            m_byteListCache.put(delimiter, this.<List<Byte>>newCache());
          }
        }
      }

      Cache<String, List<Byte>> cache = m_byteListCache.get(delimiter);
      List<Byte> result = cache.getIfPresent(key);

      if (result != null) {
        return result;
      }

      return getValueAndStoreToCache(key, new Function<String, List<Byte>>() {
        @Override
        public List<Byte> apply(String input) {
          return Lists.transform(Splitter.on(delimiter).trimResults().omitEmptyStrings().splitToList(input),
                  Functions.TO_BYTE_FUNCTION);
        }
      }, cache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
              String.format("getByteListProperty for %s failed, return default value", key), ex));
    }
    return defaultValue;
  }

  @Override
  public List<Short> getShortListProperty(String key, final String delimiter, List<Short> defaultValue) {
    try {
      if (!m_shortListCache.containsKey(delimiter)) {
        synchronized (this) {
          if (!m_shortListCache.containsKey(delimiter)) {
            m_shortListCache.put(delimiter, this.<List<Short>>newCache());
          }
        }
      }

      Cache<String, List<Short>> cache = m_shortListCache.get(delimiter);
      List<Short> result = cache.getIfPresent(key);

      if (result != null) {
        return result;
      }

      return getValueAndStoreToCache(key, new Function<String, List<Short>>() {
        @Override
        public List<Short> apply(String input) {
          return Lists.transform(Splitter.on(delimiter).trimResults().omitEmptyStrings().splitToList(input),
                  Functions.TO_SHORT_FUNCTION);
        }
      }, cache, defaultValue);
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
              String.format("getShortListProperty for %s failed, return default value", key), ex));
    }
    return defaultValue;
  }

  @Override
  public <T> T getProperty(String key, Function<String, T> function, T defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return function.apply(value);
      }
    } catch (Throwable ex) {
      Tracer.logError(new ApolloConfigException(
              String.format("getProperty for %s failed, return default value %s", key,
                      defaultValue), ex));
    }

    return defaultValue;
  }

  private <T> T getValueFromCache(String key, Function<String, T> parser, Cache<String, T> cache, T defaultValue) {
    T result = cache.getIfPresent(key);

    if (result != null) {
      return result;
    }

    return getValueAndStoreToCache(key, parser, cache, defaultValue);
  }

  private <T> T getValueAndStoreToCache(String key, Function<String, T> parser, Cache<String, T> cache, T defaultValue) {
    long currentConfigVersion = m_configVersion.get();
    String value = getProperty(key, null);

    if (value != null) {
      T result = parser.apply(value);

      if (result != null) {
        synchronized (this) {
          if (m_configVersion.get() == currentConfigVersion) {
            cache.put(key, result);
          }
        }
        return result;
      }
    }

    return defaultValue;
  }

  private <T> Cache<String, T> newCache() {
    Cache<String, T> cache = CacheBuilder.newBuilder()
        .maximumSize(m_configUtil.getMaxConfigCacheSize())
        .expireAfterAccess(m_configUtil.getConfigCacheExpireTime(), m_configUtil.getConfigCacheExpireTimeUnit())
        .build();
    allCaches.add(cache);
    return cache;
  }

  /**
   * Clear config cache
   */
  protected void clearConfigCache() {
    synchronized (this) {
      for (Cache c : allCaches) {
        if (c != null) {
          c.invalidateAll();
        }
      }
      m_configVersion.incrementAndGet();
    }
  }

  protected void fireConfigChange(final ConfigChangeEvent changeEvent) {
    for (final ConfigChangeListener listener : m_listeners) {
      m_executorService.submit(new Runnable() {
        @Override
        public void run() {
          String listenerName = listener.getClass().getName();
          Transaction transaction = Tracer.newTransaction("Apollo.ConfigChangeListener", listenerName);
          try {
            listener.onChange(changeEvent);
            transaction.setStatus(Transaction.SUCCESS);
          } catch (Throwable ex) {
            transaction.setStatus(ex);
            Tracer.logError(ex);
            logger.error("Failed to invoke config change listener {}", listenerName, ex);
          } finally {
            transaction.complete();
          }
        }
      });
    }
  }

  List<ConfigChange> calcPropertyChanges(String namespace, Properties previous,
                                         Properties current) {
    if (previous == null) {
      previous = new Properties();
    }

    if (current == null) {
      current = new Properties();
    }

    Set<String> previousKeys = previous.stringPropertyNames();
    Set<String> currentKeys = current.stringPropertyNames();

    Set<String> commonKeys = Sets.intersection(previousKeys, currentKeys);
    Set<String> newKeys = Sets.difference(currentKeys, commonKeys);
    Set<String> removedKeys = Sets.difference(previousKeys, commonKeys);

    List<ConfigChange> changes = Lists.newArrayList();

    for (String newKey : newKeys) {
      changes.add(new ConfigChange(namespace, newKey, null, current.getProperty(newKey),
          PropertyChangeType.ADDED));
    }

    for (String removedKey : removedKeys) {
      changes.add(new ConfigChange(namespace, removedKey, previous.getProperty(removedKey), null,
          PropertyChangeType.DELETED));
    }

    for (String commonKey : commonKeys) {
      String previousValue = previous.getProperty(commonKey);
      String currentValue = current.getProperty(commonKey);
      if (Objects.equal(previousValue, currentValue)) {
        continue;
      }
      changes.add(new ConfigChange(namespace, commonKey, previousValue, currentValue,
          PropertyChangeType.MODIFIED));
    }

    return changes;
  }
}
