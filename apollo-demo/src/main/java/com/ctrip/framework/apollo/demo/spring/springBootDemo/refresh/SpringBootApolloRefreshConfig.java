package com.ctrip.framework.apollo.demo.spring.springBootDemo.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.stereotype.Component;

import com.ctrip.framework.apollo.demo.spring.springBootDemo.config.SampleRedisConfig;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@ConditionalOnProperty("redis.cache.enabled")
@Component
public class SpringBootApolloRefreshConfig {
  private static final Logger logger = LoggerFactory.getLogger(SpringBootApolloRefreshConfig.class);

  @Autowired
  private SampleRedisConfig sampleRedisConfig;

  @Autowired
  private RefreshScope refreshScope;

  @ApolloConfigChangeListener
  public void onChange(ConfigChangeEvent changeEvent) {
    boolean redisCacheKeysChanged = false;
    for (String changedKey : changeEvent.changedKeys()) {
      if (changedKey.startsWith("redis.cache")) {
        redisCacheKeysChanged = true;
        break;
      }
    }
    if (!redisCacheKeysChanged) {
      return;
    }

    logger.info("before refresh {}", sampleRedisConfig.toString());
    refreshScope.refresh("sampleRedisConfig");
    logger.info("after refresh {}", sampleRedisConfig.toString());
  }
}
