package com.ctrip.framework.apollo.demo.api;

import com.google.common.base.Charsets;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.ctrip.framework.foundation.Foundation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigDemo {
  private static final Logger logger = LoggerFactory.getLogger(ApolloConfigDemo.class);
  private Config config;
  private Config publicConfig;
  private ConfigFile applicationConfigFile;
  private ConfigFile xmlConfigFile;

  public ApolloConfigDemo() {
    ConfigChangeListener changeListener = new ConfigChangeListener() {
      @Override
      public void onChange(ConfigChangeEvent changeEvent) {
        logger.info("Changes for namespace {}", changeEvent.getNamespace());
        for (String key : changeEvent.changedKeys()) {
          ConfigChange change = changeEvent.getChange(key);
          logger.info("Change - key: {}, oldValue: {}, newValue: {}, changeType: {}",
              change.getPropertyName(), change.getOldValue(), change.getNewValue(),
              change.getChangeType());
        }
      }
    };
    config = ConfigService.getAppConfig();
    config.addChangeListener(changeListener);
    publicConfig = ConfigService.getConfig("FX.apollo");
    publicConfig.addChangeListener(changeListener);
    applicationConfigFile = ConfigService.getConfigFile("application", ConfigFileFormat.Properties);
    xmlConfigFile = ConfigService.getConfigFile("dataSources", ConfigFileFormat.XML);
    xmlConfigFile.addChangeListener(new ConfigFileChangeListener() {
      @Override
      public void onChange(ConfigFileChangeEvent changeEvent) {
        logger.info(changeEvent.toString());
      }
    });
  }

  private String getConfig(String key) {
    String DEFAULT_VALUE = "undefined";
    String result = config.getProperty(key, DEFAULT_VALUE);
    if (DEFAULT_VALUE.equals(result)) {
      result = publicConfig.getProperty(key, DEFAULT_VALUE);
    }
    logger.info(String.format("Loading key : %s with value: %s", key, result));
    return result;
  }

  private void print(String namespace) {
    switch (namespace) {
      case "application":
        print(applicationConfigFile);
        return;
      case "xml":
        print(xmlConfigFile);
    }
  }

  private void print(ConfigFile configFile) {
    if (!configFile.hasContent()) {
      System.out.println("No config file content found for " + configFile.getNamespace());
      return;
    }
    System.out.println("=== Config File Content for " + configFile.getNamespace() + " is as follows: ");
    System.out.println(configFile.getContent());
  }

  private void printEnvInfo() {
    String message = String.format("AppId: %s, Env: %s, DC: %s, IP: %s", Foundation.app()
        .getAppId(), Foundation.server().getEnvType(), Foundation.server().getDataCenter(),
        Foundation.net().getHostAddress());
    System.out.println(message);
  }

  public static void main(String[] args) throws IOException {
    ApolloConfigDemo apolloConfigDemo = new ApolloConfigDemo();
    apolloConfigDemo.printEnvInfo();
    System.out.println(
        "Apollo Config Demo. Please input key to get the value.");
    while (true) {
      System.out.print("> ");
      String input = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8)).readLine();
      if (input == null || input.length() == 0) {
        continue;
      }
      input = input.trim();
      if (input.equalsIgnoreCase("application")) {
        apolloConfigDemo.print("application");
        continue;
      }
      if (input.equalsIgnoreCase("xml")) {
        apolloConfigDemo.print("xml");
        continue;
      }
      if (input.equalsIgnoreCase("quit")) {
        System.exit(0);
      }
      apolloConfigDemo.getConfig(input);
    }
  }
}
