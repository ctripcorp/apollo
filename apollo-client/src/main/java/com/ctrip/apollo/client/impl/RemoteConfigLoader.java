package com.ctrip.apollo.client.impl;

import com.ctrip.apollo.client.ConfigLoader;
import org.springframework.core.env.CompositePropertySource;

/**
 * Load config from remote config server
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigLoader implements ConfigLoader {
    private static final String PROPERTY_SOURCE_NAME = "ApolloRemoteConfigProperties";

    @Override
    public CompositePropertySource loadPropertySource() {
        return null;
    }
}
