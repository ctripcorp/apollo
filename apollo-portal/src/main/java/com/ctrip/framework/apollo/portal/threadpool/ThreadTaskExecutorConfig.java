package com.ctrip.framework.apollo.portal.threadpool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static java.lang.Boolean.TRUE;
import static java.lang.Runtime.getRuntime;

/**
 * @author renjiahua
 * @date 2020/12/26
 */
@Configuration
public class ThreadTaskExecutorConfig {
    /**
     * Number of core threads in thread pool
     */
    private static final int CORE_SIZE = getRuntime().availableProcessors() * 2 + 1;
    /**
     * Maximum number of threads in thread pool
     */
    private static final int MAX_SIZE = 200;
    /**
     * The length of the thread pool waiting queue
     */
    private static final int QUEUE_CAPACITY = 300;
    /**
     * The keep alive time of thread pool
     */
    private static final int KEEP_ALIVE = 10;

    /**
     * Create a executor bean named queryRemoteExecutor
     *
     * @return Executor
     */
    @Bean
    public Executor queryRemoteExecutor() {
        return createExecutor();
    }

    /**
     * Create a executor with default configurations
     *
     * @return Executor
     */
    private Executor createExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_SIZE);
        executor.setMaxPoolSize(MAX_SIZE);
        executor.setAllowCoreThreadTimeOut(TRUE);
        executor.setThreadNamePrefix("query-pool-");
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE);
        executor.initialize();
        return executor;
    }
}