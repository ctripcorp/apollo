package com.ctrip.framework.apollo.configservice;

import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.grayReleaseRule.GrayReleaseRulesHolder;
import com.ctrip.framework.apollo.biz.message.ReleaseMessageScanner;
import com.ctrip.framework.apollo.configservice.controller.ConfigFileController;
import com.ctrip.framework.apollo.configservice.controller.NotificationController;
import com.ctrip.framework.apollo.configservice.controller.NotificationControllerV2;
import com.ctrip.framework.apollo.configservice.filter.ClientAuthenticationFilter;
import com.ctrip.framework.apollo.configservice.service.ReleaseMessageServiceWithCache;
import com.ctrip.framework.apollo.configservice.service.config.ConfigService;
import com.ctrip.framework.apollo.configservice.service.config.ConfigServiceWithCache;
import com.ctrip.framework.apollo.configservice.service.config.DefaultConfigService;
import com.ctrip.framework.apollo.configservice.util.AccessKeyUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

/**
 * 自动配置配置Service
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Configuration
public class ConfigServiceAutoConfiguration {

  private final BizConfig bizConfig;

  public ConfigServiceAutoConfiguration(final BizConfig bizConfig) {
    this.bizConfig = bizConfig;
  }

  /**
   * 灰度发布规则持有者Bean
   *
   * @return 灰度发布规则持有者Bean
   */
  @Bean
  public GrayReleaseRulesHolder grayReleaseRulesHolder() {
    return new GrayReleaseRulesHolder();
  }

  /**
   * 配置服务bean
   *
   * @return 配置服务bean
   */
  @Bean
  public ConfigService configService() {
    // 开启缓存，使用 ConfigServiceWithCache
    if (bizConfig.isConfigServiceCacheEnabled()) {
      return new ConfigServiceWithCache();
    }
    // 不开启缓存，使用 DefaultConfigService
    return new DefaultConfigService();
  }

  /**
   * 密码编码器bean
   *
   * @return 密码编码器bean
   */
  @Bean
  public static NoOpPasswordEncoder passwordEncoder() {
    return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
  }

  /**
   * 客户端认证过滤器bean
   *
   * @param accessKeyUtil 访问密钥工具类
   * @return 过滤器注册Bean
   */
  @Bean
  public FilterRegistrationBean clientAuthenticationFilter(AccessKeyUtil accessKeyUtil) {
    FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();

    filterRegistrationBean.setFilter(new ClientAuthenticationFilter(accessKeyUtil));
    filterRegistrationBean.addUrlPatterns("/configs/*");
    filterRegistrationBean.addUrlPatterns("/configfiles/*");
    filterRegistrationBean.addUrlPatterns("/notifications/v2/*");

    return filterRegistrationBean;
  }

  /**
   * 消息扫描配置
   */
  @Configuration
  static class MessageScannerConfiguration {

    private final NotificationController notificationController;
    private final ConfigFileController configFileController;
    private final NotificationControllerV2 notificationControllerV2;
    private final GrayReleaseRulesHolder grayReleaseRulesHolder;
    private final ReleaseMessageServiceWithCache releaseMessageServiceWithCache;
    private final ConfigService configService;

    public MessageScannerConfiguration(
        final NotificationController notificationController,
        final ConfigFileController configFileController,
        final NotificationControllerV2 notificationControllerV2,
        final GrayReleaseRulesHolder grayReleaseRulesHolder,
        final ReleaseMessageServiceWithCache releaseMessageServiceWithCache,
        final ConfigService configService) {
      this.notificationController = notificationController;
      this.configFileController = configFileController;
      this.notificationControllerV2 = notificationControllerV2;
      this.grayReleaseRulesHolder = grayReleaseRulesHolder;
      this.releaseMessageServiceWithCache = releaseMessageServiceWithCache;
      this.configService = configService;
    }

    /**
     * 发布消息扫描器
     *
     * @return 发布消息扫描器
     */
    @Bean
    public ReleaseMessageScanner releaseMessageScanner() {
      ReleaseMessageScanner releaseMessageScanner = new ReleaseMessageScanner();
      //0. 处理发布消息缓存
      releaseMessageScanner.addMessageListener(releaseMessageServiceWithCache);
      //1. 处理灰度消息规则
      releaseMessageScanner.addMessageListener(grayReleaseRulesHolder);
      //2. 处理服务缓存
      releaseMessageScanner.addMessageListener(configService);
      releaseMessageScanner.addMessageListener(configFileController);
      //3. 通知客户端
      releaseMessageScanner.addMessageListener(notificationControllerV2);
      releaseMessageScanner.addMessageListener(notificationController);
      return releaseMessageScanner;
    }
  }

}
