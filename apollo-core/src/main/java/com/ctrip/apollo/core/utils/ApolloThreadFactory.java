package com.ctrip.apollo.core.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApolloThreadFactory implements ThreadFactory {
    private static Logger log = LoggerFactory.getLogger(ApolloThreadFactory.class);

    private final AtomicLong m_threadNumber = new AtomicLong(1);

    private final String m_namePrefix;

    private final boolean m_daemon;

    private final static ThreadGroup m_threadGroup = new ThreadGroup("Apollo");

    public static ThreadGroup getThreadGroup() {
        return m_threadGroup;
    }

    public static ThreadFactory create(String namePrefix, boolean daemon) {
        return new ApolloThreadFactory(namePrefix, daemon);
    }

    public static boolean waitAllShutdown(int timeoutInMillis) {
        ThreadGroup group = getThreadGroup();
        Thread[] activeThreads = new Thread[group.activeCount()];
        group.enumerate(activeThreads);
        Set<Thread> alives = new HashSet<Thread>(Arrays.asList(activeThreads));
        Set<Thread> dies = new HashSet<Thread>();
        log.info("Current ACTIVE thread count is: {}", alives.size());
        long expire = System.currentTimeMillis() + timeoutInMillis;
        while (System.currentTimeMillis() < expire) {
            classify(alives, dies, new ClassifyStandard<Thread>() {
                @Override
                public boolean satisfy(Thread t) {
                    return !t.isAlive() || t.isInterrupted() || t.isDaemon();
                }
            });
            if (alives.size() > 0) {
                log.info("Alive apollo threads: {}", alives);
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    // ignore
                }
            } else {
                log.info("All apollo threads are shutdown.");
                return true;
            }
        }
        log.warn("Some apollo threads are still alive but expire time has reached, alive threads: {}", alives);
        return false;
    }

    private static interface ClassifyStandard<T> {
        boolean satisfy(T t);
    }

    private static <T> void classify(Set<T> src, Set<T> des, ClassifyStandard<T> standard) {
        Set<T> s = new HashSet<>();
        for (T t : src) {
            if (standard.satisfy(t)) {
                s.add(t);
            }
        }
        src.removeAll(s);
        des.addAll(s);
    }

    private ApolloThreadFactory(String namePrefix, boolean daemon) {
        m_namePrefix = namePrefix;
        m_daemon = daemon;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(m_threadGroup, r,//
              m_threadGroup.getName() + "-" + m_namePrefix + "-" + m_threadNumber.getAndIncrement());
        t.setDaemon(m_daemon);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
