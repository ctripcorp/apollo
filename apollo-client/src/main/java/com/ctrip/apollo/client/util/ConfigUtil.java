package com.ctrip.apollo.client.util;

import com.ctrip.apollo.client.constants.Constants;
import com.ctrip.apollo.client.model.ApolloRegistry;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigUtil {
    public static final String APOLLO_PROPERTY = "apollo.properties";
    private static ConfigUtil configUtil = new ConfigUtil();
    private ApplicationContext applicationContext;

    private ConfigUtil() {
    }

    public static ConfigUtil getInstance() {
        return configUtil;
    }

    public String getCluster() {
        // TODO return the actual cluster
        return "default";
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public List<ApolloRegistry> loadApolloRegistries() throws IOException {
        List<URL> resourceUrls =
            Collections.list(ClassLoaderUtil.getLoader().getResources(APOLLO_PROPERTY));
        List<ApolloRegistry> registries =
            FluentIterable.from(resourceUrls).transform(new Function<URL, ApolloRegistry>() {
                @Override
                public ApolloRegistry apply(URL input) {
                    Properties properties = loadPropertiesFromResourceURL(input);
                    if (properties == null || !properties.containsKey(Constants.APP_ID)) {
                        return null;
                    }
                    ApolloRegistry registry = new ApolloRegistry();
                    registry.setAppId(Long.parseLong(properties.getProperty(Constants.APP_ID)));
                    registry.setVersion(properties.getProperty(Constants.VERSION, Constants.DEFAULT_VERSION_NAME));
                    return registry;
                }
            }).filter(Predicates.notNull()).toList();
        return registries;
    }

    Properties loadPropertiesFromResourceURL(URL resourceUrl) {
        Resource resource = applicationContext.getResource(resourceUrl.toExternalForm());
        if (resource == null || !resource.exists()) {
            return null;
        }
        try {
            return PropertiesLoaderUtils.loadProperties(new EncodedResource(resource, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
