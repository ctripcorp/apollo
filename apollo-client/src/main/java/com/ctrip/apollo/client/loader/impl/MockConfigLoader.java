package com.ctrip.apollo.client.loader.impl;

import com.ctrip.apollo.client.loader.ConfigLoader;
import com.google.common.collect.Maps;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * Mock config
 * @author Jason Song(song_s@ctrip.com)
 */
public class MockConfigLoader implements ConfigLoader {
    private static final String PROPERTY_SOURCE_NAME = "ApolloMockConfigProperties";

    @Override
    public CompositePropertySource loadPropertySource() {
        CompositePropertySource composite = new CompositePropertySource(PROPERTY_SOURCE_NAME);
        composite.addPropertySource(mock());
        return composite;
    }

    private MapPropertySource mock() {
        Map<String, Object> source = Maps.newHashMap();
        source.put("apollo.foo" ,"bar");
        MapPropertySource propertySource = new MapPropertySource("mock source", source);

        return propertySource;
    }
}
