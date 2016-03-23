package com.ctrip.apollo.client.loader;

import com.ctrip.apollo.Apollo.Env;
import com.ctrip.apollo.client.env.ClientEnvironment;
import com.ctrip.apollo.client.loader.impl.LocalConfigLoader;
import com.ctrip.apollo.client.loader.impl.RemoteConfigLoader;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigLoaderFactory {
  private static ConfigLoaderFactory configLoaderFactory = new ConfigLoaderFactory();

  private ConfigLoaderFactory() {}

  public static ConfigLoaderFactory getInstance() {
    return configLoaderFactory;
  }

  public ConfigLoader getConfigLoader() {
    ClientEnvironment env = ClientEnvironment.getInstance();
    if (env.getEnv().equals(Env.LOCAL)) {
      return new LocalConfigLoader();
    } else {
      return new RemoteConfigLoader();
    }
  }
}
