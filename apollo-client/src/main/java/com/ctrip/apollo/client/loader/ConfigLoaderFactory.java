package com.ctrip.apollo.client.loader;

import com.ctrip.apollo.Apollo.Env;
import com.ctrip.apollo.client.env.ClientEnvironment;
import com.ctrip.apollo.client.loader.impl.InMemoryConfigLoader;
import com.ctrip.apollo.client.loader.impl.LocalFileConfigLoader;
import com.ctrip.apollo.client.loader.impl.RemoteConfigLoader;
import com.ctrip.apollo.client.util.ConfigUtil;
import org.springframework.web.client.RestTemplate;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigLoaderFactory {
    private static final ConfigLoaderFactory configLoaderFactory = new ConfigLoaderFactory();

    private ConfigLoaderFactory() {
    }

    public static ConfigLoaderFactory getInstance() {
        return configLoaderFactory;
    }

    public ConfigLoader getLocalFileConfigLoader() {
        ConfigLoader configLoader = new LocalFileConfigLoader();
        return configLoader;
    }

    public ConfigLoader getInMemoryConfigLoader() {
        ConfigLoader inMemoryConfigLoader = new InMemoryConfigLoader();
        inMemoryConfigLoader.setFallBackLoader(getLocalFileConfigLoader());
        return inMemoryConfigLoader;
    }

    public ConfigLoader getRemoteConfigLoader() {
        ConfigLoader remoteConfigLoader = new RemoteConfigLoader(new RestTemplate(), ConfigUtil.getInstance(), new ConfigServiceLocater());
//        remoteConfigLoader.setFallBackLoader(getInMemoryConfigLoader());
        return remoteConfigLoader;
    }

    public ConfigLoaderManager getConfigLoaderManager() {
      ClientEnvironment env = ClientEnvironment.getInstance();
      if (env.getEnv().equals(Env.LOCAL)) {
        return new ConfigLoaderManager(getLocalFileConfigLoader(), ConfigUtil.getInstance());
      }else{
        return new ConfigLoaderManager(getRemoteConfigLoader(), ConfigUtil.getInstance());
      }
    }
}
