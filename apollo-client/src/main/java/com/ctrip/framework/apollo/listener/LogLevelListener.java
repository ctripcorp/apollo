package com.ctrip.framework.apollo.listener;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lawrence on 19/12/16.
 */
@Service
public class LogLevelListener implements ConfigChangeListener {
    private static final String EMPTY = "";
    private static final String LOG_PACKAGE_PREFIX = "logging.level.";
    private static final LoggingSystem loggingSystem = LoggingSystem.get(Thread.currentThread().getContextClassLoader());

    @ApolloConfigChangeListener(interestedKeyPrefixes = LOG_PACKAGE_PREFIX)
    public void onChange(ConfigChangeEvent configChangeEvent) {
        refeshLogLevel(configChangeEvent);
    }

    private void refeshLogLevel(ConfigChangeEvent configChangeEvent) {
        Set<String> keyNames = configChangeEvent.changedKeys();
        Map<String, ConfigChange> map = configChangeEvent.getChangeValue();
        for (String key : keyNames) {
            String logLevel = map.get(key).getNewValue();
            if(logLevel.isEmpty()) continue;
            String packageName = key.replace(LOG_PACKAGE_PREFIX, "");
            changeLevel(packageName, logLevel);
        }
    }

    private void changeLevel(String packageName, String strLevel) {
        final LogLevel level = LogLevel.valueOf(strLevel.toUpperCase());
        List<String> names = getLoggerConfigurations();
        if(names.isEmpty()) return;
        if (EMPTY.equals(packageName)) {
            loggingSystem.setLogLevel(EMPTY, level);
        } else {
            List<String> enableName = names.stream().filter(entry -> entry.startsWith(packageName)).collect(Collectors.toList());
            if (enableName.isEmpty() || null == enableName) return;
            enableName.stream().forEach(item -> {
                loggingSystem.setLogLevel(item, level);
            });
        }
    }

    private List<String> getLoggerConfigurations() {
        return loggingSystem.getLoggerConfigurations().
                stream().map(entry -> entry.getName()).collect(Collectors.toList());
    }
}

