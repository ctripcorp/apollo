package com.ctrip.framework.apollo.spring.property;

import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.springframework.beans.factory.BeanFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpringValueRegistry {
  private static final long CLEAN_INTERVAL_IN_SECONDS = 5;
  private final Map<BeanFactory, Multimap<String, SpringValue>> registry = Maps.newConcurrentMap();
  private final AtomicBoolean initialized = new AtomicBoolean(false);
  private final Object LOCK = new Object();

  public void register(BeanFactory beanFactory, String key, SpringValue springValue) {
    if (!registry.containsKey(beanFactory)) {
      synchronized (LOCK) {
        if (!registry.containsKey(beanFactory)) {
          registry.put(beanFactory, Multimaps.synchronizedListMultimap(LinkedListMultimap.<String, SpringValue>create()));
        }
      }
    }

    registry.get(beanFactory).put(key, springValue);

    // lazy initialize
    if (initialized.compareAndSet(false, true)) {
      initialize();
    }
  }

  public Collection<SpringValue> get(BeanFactory beanFactory, String key) {
    Multimap<String, SpringValue> beanFactorySpringValues = registry.get(beanFactory);
    if (beanFactorySpringValues == null) {
      return null;
    }
    return beanFactorySpringValues.get(key);
  }

  private void initialize() {
    Executors.newSingleThreadScheduledExecutor(ApolloThreadFactory.create("SpringValueRegistry", true)).scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            try {
              scanAndClean();
            } catch (Throwable ex) {
              ex.printStackTrace();
            }
          }
        }, CLEAN_INTERVAL_IN_SECONDS, CLEAN_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
  }

  private void scanAndClean() {
    Iterator<Multimap<String, SpringValue>> iterator = registry.values().iterator();
    while (!Thread.currentThread().isInterrupted() && iterator.hasNext()) {
      Multimap<String, SpringValue> springValues = iterator.next();
      Iterator<Entry<String, SpringValue>> springValueIterator = springValues.entries().iterator();
      while (springValueIterator.hasNext()) {
        Entry<String, SpringValue> springValue = springValueIterator.next();
        if (!springValue.getValue().isTargetBeanValid()) {
          // clear unused spring values
          springValueIterator.remove();
        }
      }
    }
  }
}
