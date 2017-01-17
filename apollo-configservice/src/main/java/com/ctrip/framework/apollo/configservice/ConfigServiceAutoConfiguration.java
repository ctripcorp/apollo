package com.ctrip.framework.apollo.configservice;

import com.ctrip.framework.apollo.biz.grayReleaseRule.GrayReleaseRulesHolder;
import com.ctrip.framework.apollo.biz.message.ReleaseMessageScanner;
import com.ctrip.framework.apollo.configservice.controller.ConfigFileController;
import com.ctrip.framework.apollo.configservice.controller.NotificationController;
import com.ctrip.framework.apollo.configservice.controller.NotificationControllerV2;
import com.ctrip.framework.apollo.configservice.service.ReleaseMessageServiceWithCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Configuration
public class ConfigServiceAutoConfiguration {
  @Bean
  public GrayReleaseRulesHolder grayReleaseRulesHolder() {
    return new GrayReleaseRulesHolder();
  }

  @Configuration
  static class MessageScannerConfiguration {
    @Autowired
    private NotificationController notificationController;
    @Autowired
    private ConfigFileController configFileController;
    @Autowired
    private NotificationControllerV2 notificationControllerV2;
    @Autowired
    private GrayReleaseRulesHolder grayReleaseRulesHolder;
    @Autowired
    private ReleaseMessageServiceWithCache releaseMessageServiceWithCache;

    @Bean
    public ReleaseMessageScanner releaseMessageScanner() {
      ReleaseMessageScanner releaseMessageScanner = new ReleaseMessageScanner();
      //0. handle release message cache
      releaseMessageScanner.addMessageListener(releaseMessageServiceWithCache);
      //1. handle gray release rule
      releaseMessageScanner.addMessageListener(grayReleaseRulesHolder);
      //2. handle server cache
      releaseMessageScanner.addMessageListener(configFileController);
      //3. notify clients
      releaseMessageScanner.addMessageListener(notificationControllerV2);
      releaseMessageScanner.addMessageListener(notificationController);
      return releaseMessageScanner;
    }
  }

}
