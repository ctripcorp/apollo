package com.ctrip.apollo.client.loader;

import com.ctrip.apollo.client.loader.impl.MockConfigLoader;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigLoaderFactory {
    private static ConfigLoaderFactory configLoaderFactory = new ConfigLoaderFactory();

    private ConfigLoaderFactory() {
    }

    public static ConfigLoaderFactory getInstance() {
        return configLoaderFactory;
    }

    public ConfigLoader getMockConfigLoader() {
        return new MockConfigLoader();
    }
}
