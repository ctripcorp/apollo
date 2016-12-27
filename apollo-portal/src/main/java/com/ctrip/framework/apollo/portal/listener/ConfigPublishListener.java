package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.constants.ReleaseOperation;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.components.config.PortalConfig;
import com.ctrip.framework.apollo.portal.components.emailbuilder.GrayPublishEmailBuilder;
import com.ctrip.framework.apollo.portal.components.emailbuilder.MergeEmailBuilder;
import com.ctrip.framework.apollo.portal.components.emailbuilder.NormalPublishEmailBuilder;
import com.ctrip.framework.apollo.portal.components.emailbuilder.RollbackEmailBuilder;
import com.ctrip.framework.apollo.portal.entity.bo.Email;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.service.ReleaseHistoryService;
import com.ctrip.framework.apollo.portal.spi.EmailService;
import com.ctrip.framework.apollo.tracer.Tracer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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


  @EventListener
  public void onConfigPublish(ConfigPublishEvent event) {
    Env env = event.getConfigPublishInfo().getEnv();
    if (!portalConfig.emailSupportedEnvs().contains(env)) {
      return;
    }

    ReleaseHistoryBO releaseHistory = getReleaseHistory(event);

    if (releaseHistory == null) {
      Tracer.logError("Will not send email, because load release history error", null);
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

  private ReleaseHistoryBO getReleaseHistory(ConfigPublishEvent event) {
    ConfigPublishEvent.ConfigPublishInfo info = event.getConfigPublishInfo();
    Env env = info.getEnv();

    int operation = info.isMergeEvent() ? ReleaseOperation.GRAY_RELEASE_MERGE_TO_MASTER :
                    info.isRollbackEvent() ? ReleaseOperation.ROLLBACK :
                    info.isNormalPublishEvent() ? ReleaseOperation.NORMAL_RELEASE :
                    info.isGrayPublishEvent() ? ReleaseOperation.GRAY_RELEASE : -1;

    if (operation == -1) {
      return null;
    }

    if (info.isRollbackEvent()) {
      return releaseHistoryService
          .findLatestByPreviousReleaseIdAndOperation(env, info.getPreviousReleaseId(), operation);
    } else {
      return releaseHistoryService.findLatestByReleaseIdAndOperation(env, info.getReleaseId(), operation);
    }

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
