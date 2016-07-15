package com.ctrip.framework.apollo.build;

import com.ctrip.framework.apollo.internals.ConfigServiceLocator;
import com.ctrip.framework.apollo.internals.DefaultConfigManager;
import com.ctrip.framework.apollo.internals.RemoteConfigLongPollService;
import com.ctrip.framework.apollo.spi.DefaultConfigFactory;
import com.ctrip.framework.apollo.spi.DefaultConfigFactoryManager;
import com.ctrip.framework.apollo.spi.DefaultConfigRegistry;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.http.HttpUtil;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ComponentConfigurator extends AbstractResourceConfigurator {
  public static void main(String[] args) {
    generatePlexusComponentsXmlFile(new ComponentConfigurator());
  }

  @Override
  public List<Component> defineComponents() {
    List<Component> all = new ArrayList<>();

    all.add(A(DefaultConfigManager.class));
    all.add(A(DefaultConfigFactory.class));
    all.add(A(DefaultConfigRegistry.class));
    all.add(A(DefaultConfigFactoryManager.class));
    all.add(A(ConfigUtil.class));
    all.add(A(HttpUtil.class));
    all.add(A(ConfigServiceLocator.class));
    all.add(A(RemoteConfigLongPollService.class));

    return all;
  }
}
