package com.ctrip.framework.apollo.portal.component;


import com.ctrip.framework.apollo.core.MetaDomainConsts;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class PortalSettings {

  private static final Logger logger = LoggerFactory.getLogger(PortalSettings.class);
  private static final int HEALTH_CHECK_INTERVAL = 10 * 1000;

  private final ApplicationContext applicationContext;
  private final PortalConfig portalConfig;

  private List<String> allEnvs = new ArrayList<>();

  //mark env up or down
  private Map<String, Boolean> envStatusMark = new ConcurrentHashMap<>();

  public PortalSettings(final ApplicationContext applicationContext, final PortalConfig portalConfig) {
    this.applicationContext = applicationContext;
    this.portalConfig = portalConfig;
  }

  @PostConstruct
  private void postConstruct() {

    allEnvs = portalConfig.portalSupportedEnvs();

    for (String env : allEnvs) {
      envStatusMark.put(env, true);
    }

    ScheduledExecutorService
        healthCheckService =
        Executors.newScheduledThreadPool(1, ApolloThreadFactory.create("EnvHealthChecker", true));

    healthCheckService
        .scheduleWithFixedDelay(new HealthCheckTask(applicationContext), 1000, HEALTH_CHECK_INTERVAL,
                                TimeUnit.MILLISECONDS);

  }

  public List<String> getAllEnvs() {
    return allEnvs;
  }

  public List<String> getActiveEnvs() {
    List<String> activeEnvs = new LinkedList<>();
    for (String env : allEnvs) {
      if (envStatusMark.get(env)) {
        activeEnvs.add(env);
      }
    }
    return activeEnvs;
  }

  public boolean isEnvActive(String env) {
    Boolean mark = envStatusMark.get(env);
    return mark == null ? false : mark;
  }

  private class HealthCheckTask implements Runnable {

    private static final int ENV_DOWN_THRESHOLD = 2;

    private Map<String, Integer> healthCheckFailedCounter = new HashMap<>();

    private AdminServiceAPI.HealthAPI healthAPI;

    public HealthCheckTask(ApplicationContext context) {
      healthAPI = context.getBean(AdminServiceAPI.HealthAPI.class);
      for (String env : allEnvs) {
        healthCheckFailedCounter.put(env, 0);
      }
    }

    public void run() {

      for (String env : allEnvs) {
        try {
          if (isUp(env)) {
            //revive
            if (!envStatusMark.get(env)) {
              envStatusMark.put(env, true);
              healthCheckFailedCounter.put(env, 0);
              logger.info("Env revived because env health check success. env: {}", env);
            }
          } else {
            logger.error("Env health check failed, maybe because of admin server down. env: {}, meta server address: {}", env,
                        MetaDomainConsts.getDomain(env));
            handleEnvDown(env);
          }

        } catch (Exception e) {
          logger.error("Env health check failed, maybe because of meta server down "
                       + "or configure wrong meta server address. env: {}, meta server address: {}", env,
                       MetaDomainConsts.getDomain(env), e);
          handleEnvDown(env);
        }
      }

    }

    private boolean isUp(String env) {
      Health health = healthAPI.health(env);
      return "UP".equals(health.getStatus().getCode());
    }

    private void handleEnvDown(String env) {
      int failedTimes = healthCheckFailedCounter.get(env);
      healthCheckFailedCounter.put(env, ++failedTimes);

      if (!envStatusMark.get(env)) {
        logger.error("Env is down. env: {}, failed times: {}, meta server address: {}", env, failedTimes,
                     MetaDomainConsts.getDomain(env));
      } else {
        if (failedTimes >= ENV_DOWN_THRESHOLD) {
          envStatusMark.put(env, false);
          logger.error("Env is down because health check failed for {} times, "
                       + "which equals to down threshold. env: {}, meta server address: {}", ENV_DOWN_THRESHOLD, env,
                       MetaDomainConsts.getDomain(env));
        } else {
          logger.error(
              "Env health check failed for {} times which less than down threshold. down threshold:{}, env: {}, meta server address: {}",
              failedTimes, ENV_DOWN_THRESHOLD, env, MetaDomainConsts.getDomain(env));
        }
      }

    }

  }
}
