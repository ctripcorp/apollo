package com.ctrip.apollo.client.loader;

import org.springframework.core.env.CompositePropertySource;

/**
 * @author Jason Song(songs_ctrip.com)
 */
public interface ConfigLoader {
    /**
     * Load property source for client use
     * @return property source
     */
    CompositePropertySource loadPropertySource();
}
