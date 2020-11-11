package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.constants.ReleaseOperation;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.portal.component.ConfigReleaseWebhookNotifier;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.component.emailbuilder.GrayPublishEmailBuilder;
import com.ctrip.framework.apollo.portal.component.emailbuilder.MergeEmailBuilder;
import com.ctrip.framework.apollo.portal.component.emailbuilder.NormalPublishEmailBuilder;
import com.ctrip.framework.apollo.portal.component.emailbuilder.RollbackEmailBuilder;
import com.ctrip.framework.apollo.portal.entity.bo.Email;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.ReleaseHistoryService;
import com.ctrip.framework.apollo.portal.spi.EmailService;
import com.ctrip.framework.apollo.portal.spi.MQService;
import com.ctrip.framework.apollo.tracer.Tracer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 配置发布监听器
 */
@Component
public class ConfigPublishListener {

  private final ReleaseHistoryService releaseHistoryService;
  private final EmailService emailService;
  private final NormalPublishEmailBuilder normalPublishEmailBuilder;
  private final GrayPublishEmailBuilder grayPublishEmailBuilder;
  private final RollbackEmailBuilder rollbackEmailBuilder;
  private final MergeEmailBuilder mergeEmailBuilder;
  private final PortalConfig portalConfig;
  private final MQService mqService;
  private final ConfigReleaseWebhookNotifier configReleaseWebhookNotifier;

  private ExecutorService executorService;

  public ConfigPublishListener(
      final ReleaseHistoryService releaseHistoryService,
      final EmailService emailService,
      final NormalPublishEmailBuilder normalPublishEmailBuilder,
      final GrayPublishEmailBuilder grayPublishEmailBuilder,
      final RollbackEmailBuilder rollbackEmailBuilder,
      final MergeEmailBuilder mergeEmailBuilder,
      final PortalConfig portalConfig,
      final MQService mqService,
      final ConfigReleaseWebhookNotifier configReleaseWebhookNotifier) {
    this.releaseHistoryService = releaseHistoryService;
    this.emailService = emailService;
    this.normalPublishEmailBuilder = normalPublishEmailBuilder;
    this.grayPublishEmailBuilder = grayPublishEmailBuilder;
    this.rollbackEmailBuilder = rollbackEmailBuilder;
    this.mergeEmailBuilder = mergeEmailBuilder;
    this.portalConfig = portalConfig;
    this.mqService = mqService;
    this.configReleaseWebhookNotifier = configReleaseWebhookNotifier;
  }

  @PostConstruct
  public void init() {
    executorService = Executors
        .newSingleThreadExecutor(ApolloThreadFactory.create("ConfigPublishNotify", true));
  }

  /**
   * 监听配置发布事件
   *
   * @param event
   */
  @EventListener
  public void onConfigPublish(ConfigPublishEvent event) {
    executorService.submit(new ConfigPublishNotifyTask(event.getConfigPublishInfo()));
  }

  /**
   * 配置发布通知任务
   */
  private class ConfigPublishNotifyTask implements Runnable {

    /**
     * 配置发布事件配置发布信息
     */
    private ConfigPublishEvent.ConfigPublishInfo publishInfo;

    ConfigPublishNotifyTask(ConfigPublishEvent.ConfigPublishInfo publishInfo) {
      this.publishInfo = publishInfo;
    }

    @Override
    public void run() {
      ReleaseHistoryBO releaseHistory = getReleaseHistory();
      // 获得不到 ReleaseHistoryBO 对象，返回
      if (releaseHistory == null) {
        Tracer.logError("Load release history failed", null);
        return;
      }

      // 发送WebHook
      this.sendPublishWebHook(releaseHistory);
      // 发送邮件
      sendPublishEmail(releaseHistory);
      // 发送 MQ 消息
      sendPublishMsg(releaseHistory);
    }

    /**
     * 调用 Admin Service ，获得对应的 发布历史信息 业务对象
     *
     * @return 发布历史信息 业务对象
     */
    private ReleaseHistoryBO getReleaseHistory() {
      Env env = publishInfo.getEnv();

      // 获得发布操作类型
      int operation = publishInfo.isMergeEvent() ? ReleaseOperation.GRAY_RELEASE_MERGE_TO_MASTER :
          publishInfo.isRollbackEvent() ? ReleaseOperation.ROLLBACK :
              publishInfo.isNormalPublishEvent() ? ReleaseOperation.NORMAL_RELEASE :
                  publishInfo.isGrayPublishEvent() ? ReleaseOperation.GRAY_RELEASE : -1;

      // 操作类型未知，直接返回
      if (operation == -1) {
        return null;
      }
      // 若是回滚操作，获得前一个版本
      if (publishInfo.isRollbackEvent()) {
        return releaseHistoryService.findLatestByPreviousReleaseIdAndOperation(env,
            publishInfo.getPreviousReleaseId(), operation);
      }
      // 若是非回滚操作，获得最新版本
      return releaseHistoryService
          .findLatestByReleaseIdAndOperation(env, publishInfo.getReleaseId(), operation);

    }

    /**
     * 发送webhook
     *
     * @param releaseHistory 发布历史信息业务对象
     */
    private void sendPublishWebHook(ReleaseHistoryBO releaseHistory) {
      Env env = publishInfo.getEnv();

      String[] webHookUrls = portalConfig.webHookUrls();
      // // 判断当前 Env 是否支持发送webhook
      if (!portalConfig.webHookSupportedEnvs().contains(env) || webHookUrls == null) {
        return;
      }
      // 发送通知
      configReleaseWebhookNotifier.notify(webHookUrls, env, releaseHistory);
    }

    /**
     * 发送邮件
     *
     * @param releaseHistory 发布历史信息 业务对象
     */
    private void sendPublishEmail(ReleaseHistoryBO releaseHistory) {
      Env env = publishInfo.getEnv();
      // 判断当前 Env 是否支持发邮件
      if (!portalConfig.emailSupportedEnvs().contains(env)) {
        return;
      }

      int realOperation = releaseHistory.getOperation();
      // 创建 Email 对象
      Email email = null;
      try {
        email = buildEmail(env, releaseHistory, realOperation);
      } catch (Throwable e) {
        Tracer.logError("build email failed.", e);
      }
      // 发送邮件
      if (email != null) {
        emailService.send(email);
      }
    }

    /**
     * 发送 MQ 消息
     *
     * @param releaseHistory 发布历史信息 业务对象
     */
    private void sendPublishMsg(ReleaseHistoryBO releaseHistory) {
      mqService.sendPublishMsg(publishInfo.getEnv(), releaseHistory);
    }

    /**
     * 创建Email对象
     *
     * @param env            环境
     * @param releaseHistory 发布历史信息 业务对象
     * @param operation      操作类型值
     * @return 邮件对象
     */
    private Email buildEmail(Env env, ReleaseHistoryBO releaseHistory, int operation) {
      switch (operation) {
        case ReleaseOperation.GRAY_RELEASE: {
          return grayPublishEmailBuilder.build(env, releaseHistory);
        }
        case ReleaseOperation.NORMAL_RELEASE: {
          return normalPublishEmailBuilder.build(env, releaseHistory);
        }
        case ReleaseOperation.ROLLBACK: {
          return rollbackEmailBuilder.build(env, releaseHistory);
        }
        case ReleaseOperation.GRAY_RELEASE_MERGE_TO_MASTER: {
          return mergeEmailBuilder.build(env, releaseHistory);
        }
        default:
          return null;
      }
    }
  }

}
