package com.ctrip.apollo.client.loader.impl;

import com.ctrip.apollo.client.loader.ConfigLoader;
import org.springframework.core.env.CompositePropertySource;

/**
 * Load config from local backup file
 * @author Jason Song(song_s@ctrip.com)
 */
public class LocalConfigLoader implements ConfigLoader {
    private static final String PROPERTY_SOURCE_NAME = "ApolloLocalConfigProperties";

    @Override
    public CompositePropertySource loadPropertySource() {
        return null;
    }
}
