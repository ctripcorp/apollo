package com.ctrip.apollo.client.loader.impl;

import com.ctrip.apollo.client.loader.ConfigLoader;
import com.ctrip.apollo.client.loader.ConfigServiceLocater;
import com.ctrip.apollo.client.model.ApolloRegistry;
import com.ctrip.apollo.client.util.ConfigUtil;
import com.ctrip.apollo.core.dto.ApolloConfig;
import com.ctrip.apollo.core.serivce.ApolloService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Load config from remote config server
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigLoader implements ConfigLoader {
    private static final String PROPERTY_SOURCE_NAME = "ApolloRemoteConfigProperties";
    private static final Logger logger = LoggerFactory.getLogger(RemoteConfigLoader.class);
    private final RestTemplate restTemplate;
    private final ConfigUtil configUtil;
    private final ExecutorService executorService;
    private final AtomicLong counter;
    private final ConfigServiceLocater serviceLocater;
    
    public RemoteConfigLoader() {
        this(new RestTemplate(), ConfigUtil.getInstance(), new ConfigServiceLocater());
    }

    public RemoteConfigLoader(RestTemplate restTemplate, ConfigUtil configUtil, ConfigServiceLocater serviceLocater) {
        this.restTemplate = restTemplate;
        this.configUtil = configUtil;
        this.counter = new AtomicLong();
        this.serviceLocater = serviceLocater;
        this.executorService = Executors.newFixedThreadPool(5, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "RemoteConfigLoader-" + counter.incrementAndGet());
                return thread;
            }
        });
    }

    @Override
    public CompositePropertySource loadPropertySource() {
        CompositePropertySource composite = new CompositePropertySource(PROPERTY_SOURCE_NAME);
        List<ApolloRegistry> apolloRegistries;
        try {
            apolloRegistries = configUtil.loadApolloRegistries();
        } catch (IOException e) {
            throw new RuntimeException("Load apollo config registry failed", e);
        }

        if (apolloRegistries == null || apolloRegistries.isEmpty()) {
            logger.warn("No Apollo Registry found!");
            return composite;
        }

        try {
            doLoadRemoteApolloConfig(apolloRegistries, composite);
        } catch (Throwable throwable) {
            throw new RuntimeException("Load remote property source failed", throwable);
        }
        return composite;
    }

    void doLoadRemoteApolloConfig(List<ApolloRegistry> apolloRegistries, CompositePropertySource compositePropertySource) throws Throwable {
        List<Future<MapPropertySource>> futures = Lists.newArrayList();
        for (final ApolloRegistry apolloRegistry : apolloRegistries) {
            futures.add(executorService.submit(new Callable<MapPropertySource>() {
                @Override
                public MapPropertySource call() throws Exception {
                    return loadSingleApolloConfig(apolloRegistry.getAppId(), apolloRegistry.getVersion());
                }
            }));
        }
        for (Future<MapPropertySource> future : futures) {
            try {
                MapPropertySource result = future.get();
                if (result == null) {
                    continue;
                }
                compositePropertySource.addPropertySource(result);
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        }
    }

    MapPropertySource loadSingleApolloConfig(long appId, String version) {
        List<ApolloService> services = serviceLocater.getConfigServices();
        if(services.size()==0){
          throw new RuntimeException("Could not find availble config services");
        }
        ApolloService service = services.get(0);
        ApolloConfig result =
            this.getRemoteConfig(restTemplate, service.getHomepageUrl(), appId, configUtil.getCluster(), version);
        if (result == null) {
            logger.error("Loaded config null...");
            return null;
        }
        logger.info("Loaded config: {}", result);

        return new MapPropertySource(assemblePropertySourceName(result), result.getConfigurations());
    }

    private String assemblePropertySourceName(ApolloConfig apolloConfig) {
        return String.format("%d-%s-%s-%d", apolloConfig.getAppId(), apolloConfig.getCluster(), apolloConfig.getVersion(), apolloConfig.getReleaseId());
    }

    ApolloConfig getRemoteConfig(RestTemplate restTemplate, String uri, long appId, String cluster, String version) {
        logger.info("Loading config from {}, appId={}, cluster={}, version={}", uri, appId, cluster, version);
        String path = "config/{appId}/{cluster}";
        Object[] args = new String[] {String.valueOf(appId), cluster};
        if (StringUtils.hasText(version)) {
            args = new String[] {String.valueOf(appId), cluster, version};
            path = path + "/{version}";
        }
        ResponseEntity<ApolloConfig> response = null;

        try {
            // TODO retry
            response = restTemplate.exchange(uri
                + path, HttpMethod.GET, new HttpEntity<Void>((Void) null), ApolloConfig.class, args);
        } catch (Exception e) {
            throw e;
        }

        if (response == null || response.getStatusCode() != HttpStatus.OK) {
            return null;
        }
        ApolloConfig result = response.getBody();
        return result;
    }

}
