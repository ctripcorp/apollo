package com.ctrip.apollo.client.loader.impl;

import com.ctrip.apollo.client.loader.ConfigServiceLocater;
import com.ctrip.apollo.client.model.ApolloRegistry;
import com.ctrip.apollo.client.util.ConfigUtil;
import com.ctrip.apollo.core.dto.ApolloConfig;
import com.ctrip.apollo.core.serivce.ApolloService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Load config from remote config server
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigLoader extends AbstractConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(RemoteConfigLoader.class);
    private final RestTemplate restTemplate;
    private final ConfigUtil configUtil;
    private final ConfigServiceLocater serviceLocater;

    public RemoteConfigLoader(RestTemplate restTemplate, ConfigUtil configUtil, ConfigServiceLocater serviceLocater) {
        this.restTemplate = restTemplate;
        this.configUtil = configUtil;
        this.serviceLocater = serviceLocater;
    }

    ApolloConfig getRemoteConfig(RestTemplate restTemplate, String uri, String cluster, ApolloRegistry apolloRegistry, ApolloConfig previousConfig) {
        long appId = apolloRegistry.getAppId();
        String version = apolloRegistry.getVersion();

        logger.info("Loading config from {}, appId={}, cluster={}, version={}", uri, appId, cluster, version);
        String path = "/config/{appId}/{cluster}";
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("appId", appId);
        paramMap.put("cluster", cluster);

        if (StringUtils.hasText(version)) {
            path = path + "/{version}";
            paramMap.put("version", version);
        }
        if (previousConfig != null) {
            path = path + "?releaseId={releaseId}";
            paramMap.put("releaseId", previousConfig.getReleaseId());
        }

        ResponseEntity<ApolloConfig> response;

        try {
            // TODO retry
            response = restTemplate.exchange(uri
                + path, HttpMethod.GET, new HttpEntity<Void>((Void) null), ApolloConfig.class, paramMap);
        } catch (Throwable e) {
            throw e;
        }

        if (response == null) {
            throw new RuntimeException("Load apollo config failed, response is null");
        }

        if (response.getStatusCode() == HttpStatus.NOT_MODIFIED) {
            return null;
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException(String.format("Load apollo config failed, response status %s", response.getStatusCode()));
        }

        ApolloConfig result = response.getBody();
        return result;
    }

    @Override
    protected ApolloConfig doLoadApolloConfig(ApolloRegistry apolloRegistry, ApolloConfig previous) {
      List<ApolloService> services = serviceLocater.getConfigServices();
      if (services.size() == 0) {
        throw new RuntimeException("Could not find availble config services");
      }
      ApolloService service = services.get(0);
      ApolloConfig result = this.getRemoteConfig(restTemplate,
                                        service.getHomepageUrl(), configUtil.getCluster(),
                                        apolloRegistry, previous);
        //When remote server return 304, we need to return the previous result
        return result == null ? previous : result;
    }
}