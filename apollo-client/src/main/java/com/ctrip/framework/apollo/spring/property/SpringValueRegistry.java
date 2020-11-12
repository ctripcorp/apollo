package com.ctrip.framework.apollo.spring.property;

import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.BeanFactory;

/**
 * Spring的@Value注册表
 */
public class SpringValueRegistry {

  /**
   * 清理时间间隔
   */
  private static final long CLEAN_INTERVAL_IN_SECONDS = TimeUnit.SECONDS.toSeconds(5);
  /**
   * 注册的SpringValue集合
   * <p>
   * KEY：属性 KEY ，即 Config 配置 KEY VALUE：SpringValue 数组
   */
  private final Map<BeanFactory, Multimap<String, SpringValue>> registry = Maps.newConcurrentMap();
  /**
   * 是否已经初始化
   */
  private final AtomicBoolean initialized = new AtomicBoolean(false);
  /**
   * 对象锁
   */
  private final Object LOCK = new Object();

  /**
   * 注册
   *
   * @param beanFactory bean工厂
   * @param key         配置的key
   * @param springValue springValue对象
   */
  public void register(BeanFactory beanFactory, String key, SpringValue springValue) {
    // 如果不存在指定的beanFactory，添加beanFactory
    if (!registry.containsKey(beanFactory)) {
      synchronized (LOCK) {
        if (!registry.containsKey(beanFactory)) {
          registry.put(beanFactory,
              Multimaps.synchronizedListMultimap(LinkedListMultimap.create()));
        }
      }
    }

    // 保存springValue
    registry.get(beanFactory).put(key, springValue);

    // 懒加载
    if (initialized.compareAndSet(false, true)) {
      initialize();
    }
  }

  /**
   * 获取指定beanFactory中指定key的SpringValue集合
   *
   * @param beanFactory beanFactory对象
   * @param key         指定的配置key
   * @return SpringValue集合
   */
  public Collection<SpringValue> get(BeanFactory beanFactory, String key) {
    // 指定beanFactory中指定key的SpringValue集合
    Multimap<String, SpringValue> beanFactorySpringValues = registry.get(beanFactory);
    if (beanFactorySpringValues == null) {
      return null;
    }

    return beanFactorySpringValues.get(key);
  }

  /**
   * 初始化
   */
  private void initialize() {
    Executors.newSingleThreadScheduledExecutor(ApolloThreadFactory.create("SpringValueRegistry",
        true)).scheduleAtFixedRate(() -> {
      try {
        scanAndClean();
      } catch (Throwable ex) {
        ex.printStackTrace();
      }
    }, CLEAN_INTERVAL_IN_SECONDS, CLEAN_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
  }

  /**
   * 扫描并清理
   */
  private void scanAndClean() {
    Iterator<Multimap<String, SpringValue>> iterator = registry.values().iterator();
    // 线程未中断并且注册表中还有数据
    while (!Thread.currentThread().isInterrupted() && iterator.hasNext()) {
      Multimap<String, SpringValue> springValues = iterator.next();
      // 清除无用的弹簧值
      springValues.entries().removeIf(springValue -> !springValue.getValue().isTargetBeanValid());
    }
  }
}
