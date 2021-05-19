package com.ctrip.framework.apollo.core.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Delayed log printing utility class, used only for logging when Apollo is initialized
 *
 * @author kl (http://kailing.pub)
 * @since 2021/5/11
 */
public final class DeferredLogCache {

    public static final int MAX_LOG_SIZE = 1000;
    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);
    private static final AtomicInteger LOG_INDEX = new AtomicInteger(0);
    private static final Cache<Integer, Line> LOG_CACHE = CacheBuilder.newBuilder()
            .maximumSize(MAX_LOG_SIZE)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    private DeferredLogCache() { }

    public static void enableDeferredLog() {
        ENABLED.set(true);
    }

    public static void disableDeferredLog() {
        ENABLED.set(false);
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static void debug(Logger logger, String message, Object... objects) {
        add(logger, Level.DEBUG, message, objects, null);
    }

    public static void info(Logger logger, String message, Object... objects) {
        add(logger, Level.INFO, message, objects, null);
    }

    public static void warn(Logger logger, String message, Object... objects) {
        add(logger, Level.WARN, message, objects, null);
    }

    public static void error(Logger logger, String message, Throwable throwable) {
        add(logger, Level.WARN, message, null, throwable);
    }

    private static void add(Logger logger, Level level, String message, Object[] objects, Throwable throwable) {
        if (isEnabled()) {
            Line logLine = new Line(level, message, objects, throwable, logger);
            LOG_CACHE.put(LOG_INDEX.incrementAndGet(), logLine);
        }
    }

    public static void replayTo() {
        if (isEnabled()) {
            for (int i = 1; i <= LOG_INDEX.get(); i++) {
                Line logLine = LOG_CACHE.getIfPresent(i);
                assert logLine != null;
                Logger logger = logLine.getLogger();
                Level level = logLine.getLevel();
                String message = logLine.getMessage();
                Object[] objects = logLine.getObjects();
                Throwable throwable = logLine.getThrowable();
                logTo(logger, level, message, objects, throwable);
            }
            clear();
        }

    }

    private static void clear() {
        LOG_CACHE.invalidateAll();
        LOG_INDEX.set(0);
        disableDeferredLog();
    }

    public static long logSize() {
        return LOG_CACHE.size();
    }

    static void logTo(Logger logger, Level level, String message, Object[] objects, Throwable throwable) {
        switch (level) {
            case DEBUG:
                logger.debug(message, objects);
                return;
            case INFO:
                logger.info(message, objects);
                return;
            case WARN:
                logger.warn(message, objects);
                return;
            case ERROR:
                logger.error(message, throwable);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + level);
        }
    }

    static class Line {

        private final Level level;

        private final String message;

        private final Object[] objects;

        private final Throwable throwable;

        private final Logger logger;

        Line(Level level, String message, Object[] objects, Throwable throwable, Logger logger) {
            this.level = level;
            this.message = message;
            this.objects = objects;
            this.throwable = throwable;
            this.logger = logger;
        }

        public Object[] getObjects() {
            return objects;
        }

        public Logger getLogger() {
            return logger;
        }

        Level getLevel() {
            return this.level;
        }

        String getMessage() {
            return this.message;
        }

        Throwable getThrowable() {
            return this.throwable;
        }

    }
}
