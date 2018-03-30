package com.ctrip.framework.apollo.demo.spring.springBootDemo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * auto update ConfigurationProperties without RefreshScope
 *
 * Create by sunshanpeng on 2018/3/30
 */
@Component
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolConfig {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);

    private Integer corePoolSize;

    private Integer maxPoolSize;

    private Integer maxQueueSize;

    private Long keepAliveSeconds = 60L;

    @PostConstruct
    private void initialize() {
        logger.info(
                "ThreadPoolConfig initialized - corePoolSize: {}, maxPoolSize: {}, maxQueueSize: {}, keepAliveSeconds: {}",
                corePoolSize, maxPoolSize, maxQueueSize, keepAliveSeconds);
    }

    public Integer getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(Integer corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public Integer getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(Integer maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public Long getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(Long keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    @Override
    public String toString() {
        return "ThreadPoolConfig{" +
                "corePoolSize=" + corePoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", maxQueueSize=" + maxQueueSize +
                ", keepAliveSeconds=" + keepAliveSeconds +
                '}';
    }
}
