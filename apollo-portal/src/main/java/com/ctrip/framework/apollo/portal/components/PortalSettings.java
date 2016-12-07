package com.ctrip.framework.apollo.portal.components;


import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.service.ServerConfigService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

@Component
public class PortalSettings {

  private static final Logger logger = LoggerFactory.getLogger(PortalSettings.class);
  private static final int HEALTH_CHECK_INTERVAL = 10 * 1000;
  private static final String DEFAULT_SUPPORT_ENV_LIST = "FAT,UAT,PRO";


  @Autowired
  ApplicationContext applicationContext;

  @Autowired
  private ServerConfigService serverConfigService;

  private List<Env> allEnvs = new ArrayList<>();

  //mark env up or down
  private Map<Env, Boolean> envStatusMark = new ConcurrentHashMap<>();

  @PostConstruct
  private void postConstruct() {

    String serverConfig = serverConfigService.getValue("apollo.portal.envs", DEFAULT_SUPPORT_ENV_LIST);
    String[] configedEnvs = serverConfig.split(",");
    List<String> allStrEnvs = Arrays.asList(configedEnvs);
    for (String e : allStrEnvs) {
      allEnvs.add(Env.valueOf(e.toUpperCase()));
    }

    for (Env env : allEnvs) {
      envStatusMark.put(env, true);
    }

    ScheduledExecutorService
        healthCheckService =
        Executors.newScheduledThreadPool(1, ApolloThreadFactory.create("EnvHealthChecker", false));

    healthCheckService
        .scheduleWithFixedDelay(new HealthCheckTask(applicationContext), 1000, HEALTH_CHECK_INTERVAL,
                                TimeUnit.MILLISECONDS);

  }

  public List<Env> getAllEnvs() {
    return allEnvs;
  }

  public List<Env> getActiveEnvs() {
    List<Env> activeEnvs = new LinkedList<>();
    for (Env env : allEnvs) {
      if (envStatusMark.get(env)) {
        activeEnvs.add(env);
      }
    }
    return activeEnvs;
  }

  private class HealthCheckTask implements Runnable {

    private static final int ENV_DOWN_THRESHOLD = 2;

    private Map<Env, Integer> healthCheckFailedCounter = new HashMap<>();

    private AdminServiceAPI.HealthAPI healthAPI;

    public HealthCheckTask(ApplicationContext context) {
      healthAPI = context.getBean(AdminServiceAPI.HealthAPI.class);
      for (Env env : allEnvs) {
        healthCheckFailedCounter.put(env, 0);
      }
    }

    public void run() {

      for (Env env : allEnvs) {
        try {
          if (isUp(env)) {
            //revive
            if (!envStatusMark.get(env)) {
              envStatusMark.put(env, true);
              healthCheckFailedCounter.put(env, 0);
              logger.info("Env revived because env health check success. env: {}", env);
            }
          } else {
            logger.warn("Env health check failed, maybe because of admin server down. env: {}", env);
            handleEnvDown(env);
          }

        } catch (Exception e) {
          logger.warn("Env health check failed, maybe because of meta server down "
                      + "or config error meta server address. env: {}", env);
          handleEnvDown(env);
        }
      }

    }

    private boolean isUp(Env env) {
      Health health = healthAPI.health(env);
      return "UP".equals(health.getStatus().getCode());
    }

    private void handleEnvDown(Env env) {
      int failedTimes = healthCheckFailedCounter.get(env);
      healthCheckFailedCounter.put(env, ++failedTimes);

      if (!envStatusMark.get(env)) {
        logger.error("Env is down. env: {}, failed times: {}", env, failedTimes);
      } else {
        if (failedTimes >= ENV_DOWN_THRESHOLD) {
          envStatusMark.put(env, false);
          logger.error("Env is down because health check failed for {} times, "
                       + "which equals to down threshold. env: {}", ENV_DOWN_THRESHOLD, env);
        } else {
          logger.warn("Env health check failed for {} times which less than down threshold. down threshold:{}, env: {}",
                      failedTimes, ENV_DOWN_THRESHOLD, env);
        }
      }

    }

  }
}
