package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.constants.ReleaseOperation;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.component.emailbuilder.GrayPublishEmailBuilder;
import com.ctrip.framework.apollo.portal.component.emailbuilder.MergeEmailBuilder;
import com.ctrip.framework.apollo.portal.component.emailbuilder.NormalPublishEmailBuilder;
import com.ctrip.framework.apollo.portal.component.emailbuilder.RollbackEmailBuilder;
import com.ctrip.framework.apollo.portal.entity.bo.Email;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.service.ReleaseHistoryService;
import com.ctrip.framework.apollo.portal.spi.EmailService;
import com.ctrip.framework.apollo.portal.spi.MQService;
import com.ctrip.framework.apollo.tracer.Tracer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

@Component
public class ConfigPublishListener {

  @Autowired
  private ReleaseHistoryService releaseHistoryService;
  @Autowired
  private EmailService emailService;
  @Autowired
  private NormalPublishEmailBuilder normalPublishEmailBuilder;
  @Autowired
  private GrayPublishEmailBuilder grayPublishEmailBuilder;
  @Autowired
  private RollbackEmailBuilder rollbackEmailBuilder;
  @Autowired
  private MergeEmailBuilder mergeEmailBuilder;
  @Autowired
  private PortalConfig portalConfig;
  @Autowired
  private MQService mqService;

  private ExecutorService executorService;

  @PostConstruct
  public void init() {
    executorService = Executors.newSingleThreadExecutor(ApolloThreadFactory.create("ConfigPublishNotify", true));
  }

  @EventListener
  public void onConfigPublish(ConfigPublishEvent event) {
    executorService.submit(new ConfigPublishNotifyTask(event.getConfigPublishInfo()));
  }


  private class ConfigPublishNotifyTask implements Runnable {

    private ConfigPublishEvent.ConfigPublishInfo publishInfo;

    ConfigPublishNotifyTask(ConfigPublishEvent.ConfigPublishInfo publishInfo) {
      this.publishInfo = publishInfo;
    }

    @Override
    public void run() {
      ReleaseHistoryBO releaseHistory = getReleaseHistory();
      if (releaseHistory == null) {
        Tracer.logError("Load release history failed", null);
        return;
      }

      sendPublishEmail(releaseHistory);

      sendPublishMsg(releaseHistory);
    }

    private ReleaseHistoryBO getReleaseHistory() {
      Env env = publishInfo.getEnv();

      int operation = publishInfo.isMergeEvent() ? ReleaseOperation.GRAY_RELEASE_MERGE_TO_MASTER :
                      publishInfo.isRollbackEvent() ? ReleaseOperation.ROLLBACK :
                      publishInfo.isNormalPublishEvent() ? ReleaseOperation.NORMAL_RELEASE :
                      publishInfo.isGrayPublishEvent() ? ReleaseOperation.GRAY_RELEASE : -1;

      if (operation == -1) {
        return null;
      }

      if (publishInfo.isRollbackEvent()) {
        return releaseHistoryService
            .findLatestByPreviousReleaseIdAndOperation(env, publishInfo.getPreviousReleaseId(), operation);
      } else {
        return releaseHistoryService.findLatestByReleaseIdAndOperation(env, publishInfo.getReleaseId(), operation);
      }

    }

    private void sendPublishEmail(ReleaseHistoryBO releaseHistory) {
      Env env = publishInfo.getEnv();

      if (!portalConfig.emailSupportedEnvs().contains(env)) {
        return;
      }

      int realOperation = releaseHistory.getOperation();

      Email email = null;
      try {
        email = buildEmail(env, releaseHistory, realOperation);
      } catch (Throwable e) {
        Tracer.logError("build email failed.", e);
      }

      if (email != null) {
        emailService.send(email);
      }
    }

    private void sendPublishMsg(ReleaseHistoryBO releaseHistory) {
      mqService.sendPublishMsg(publishInfo.getEnv(), releaseHistory);
    }

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
