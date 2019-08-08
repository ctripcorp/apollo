package com.ctrip.framework.apollo.spring.property;

import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.BeanFactory;

public class SpringValueRegistry {

  private static final long CLEAN_INTERVAL_IN_SECONDS = 5;
  private final Map<BeanFactory, Set<SpringValue>> registry = new ConcurrentHashMap<>();
  private final AtomicBoolean initialized = new AtomicBoolean(false);
  private final Object LOCK = new Object();

  public synchronized void register(BeanFactory beanFactory, SpringValue springValue) {
    if (!registry.containsKey(beanFactory)) {
      synchronized (LOCK) {
        if (!registry.containsKey(beanFactory)) {
          registry.put(beanFactory, new HashSet<SpringValue>());
        }
      }
    }

    registry.get(beanFactory).add(springValue);

    // lazy initialize
    if (initialized.compareAndSet(false, true)) {
      initialize();
    }
  }


  public Set<SpringValue> get(BeanFactory beanFactory) {
    return registry.get(beanFactory);
  }

  private void initialize() {
    Executors
        .newSingleThreadScheduledExecutor(ApolloThreadFactory.create("SpringValueRegistry", true))
        .scheduleAtFixedRate(
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
    Iterator<Set<SpringValue>> iterator = registry.values().iterator();
    while (!Thread.currentThread().isInterrupted() && iterator.hasNext()) {
      Set<SpringValue> springValues = iterator.next();
      Iterator<SpringValue> springValueIterator = springValues.iterator();
      while (springValueIterator.hasNext()) {
        SpringValue springValue = springValueIterator.next();
        if (!springValue.isTargetBeanValid()) {
          // clear unused spring values
          springValueIterator.remove();
        }
      }
    }
  }
}
