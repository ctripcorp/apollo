package com.ctrip.framework.apollo.portal.component;


import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.environment.PortalMetaDomainService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 界面（门户设置）
 */
@Slf4j
@Component
public class PortalSettings {

  /**
   * 健康检查间隔.
   */
  private static final long HEALTH_CHECK_INTERVAL = TimeUnit.SECONDS.toMillis(10);
  /**
   * 应用上下文对象
   */
  private final ApplicationContext applicationContext;
  /**
   * 界面（门户）配置
   */
  private final PortalConfig portalConfig;
  private final PortalMetaDomainService portalMetaDomainService;
  /**
   * 所有环境的列表，系统默认支持的环境
   */
  @Getter
  private List<Env> allEnvs = new ArrayList<>();

  /**
   * 标记环境状态<环境，true,UP,false，DOWN>
   */
  private Map<Env, Boolean> envStatusMark = new ConcurrentHashMap<>();

  public PortalSettings(final ApplicationContext applicationContext,
      final PortalConfig portalConfig, final PortalMetaDomainService portalMetaDomainService) {
    this.applicationContext = applicationContext;
    this.portalConfig = portalConfig;
    this.portalMetaDomainService = portalMetaDomainService;
  }

  /**
   * 初始化
   */
  @PostConstruct
  private void postConstruct() {
    //构建所有环境的列表
    allEnvs = portalConfig.portalSupportedEnvs();
    // 构建环境状态标记
    for (Env env : allEnvs) {
      envStatusMark.put(env, true);
    }

    // 10秒检查一次
    ScheduledExecutorService
        healthCheckService =
        Executors.newScheduledThreadPool(1, ApolloThreadFactory.create("EnvHealthChecker", true));

    healthCheckService
        .scheduleWithFixedDelay(new HealthCheckTask(applicationContext), 1000,
            HEALTH_CHECK_INTERVAL,
            TimeUnit.MILLISECONDS);

  }

  /**
   * 获取所有活跃的环境
   *
   * @return 活跃的环境列表
   */
  public List<Env> getActiveEnvs() {
    List<Env> activeEnvs = new LinkedList<>();
    for (Env env : allEnvs) {
      if (envStatusMark.get(env)) {
        activeEnvs.add(env);
      }
    }
    return activeEnvs;
  }

  /**
   * 环境是否活跃
   *
   * @param env 指定环境
   * @return true, UP. false，DOWN
   */
  public boolean isEnvActive(Env env) {
    Boolean mark = envStatusMark.get(env);
    return mark != null && mark;
  }

  /**
   * 健康检查任务
   */
  private class HealthCheckTask implements Runnable {

    /**
     * 环境down机阈值
     */
    private static final int ENV_DOWN_THRESHOLD = 2;
    /**
     * 检查检查环境失败次数（环境，次数）
     */
    private Map<Env, Integer> healthCheckFailedCounter = new HashMap<>();
    /**
     * 检查检查api
     */
    private AdminServiceAPI.HealthAPI healthAPI;

    /**
     * 初始化
     *
     * @param context 应用上下文
     */
    public HealthCheckTask(ApplicationContext context) {
      healthAPI = context.getBean(AdminServiceAPI.HealthAPI.class);
      for (Env env : allEnvs) {
        healthCheckFailedCounter.put(env, 0);
      }
    }

    @Override
    public void run() {

      for (Env env : allEnvs) {
        try {
          //如果为UP，但是标记的Down,就直接修改标记状态，重置失败次数
          if (isUp(env)) {
            //revive
            if (!envStatusMark.get(env)) {
              envStatusMark.put(env, true);
              healthCheckFailedCounter.put(env, 0);
              log.info("Env revived because env health check success. env: {}", env);
            }
          } else {
            // 处理环境down状态的情况
            log.error(
                "Env health check failed, maybe because of admin server down. env: {}, meta server address: {}",
                env,
                portalMetaDomainService.getDomain(env));
            handleEnvDown(env);
          }

        } catch (Exception e) {
          //处理环境down状态检查失败的情况
          log.error("Env health check failed, maybe because of meta server down "
                  + "or configure wrong meta server address. env: {}, meta server address: {}", env,
              portalMetaDomainService.getDomain(env), e);
          handleEnvDown(env);
        }
      }

    }

    /**
     * 判断指定环境是状态是否为UP
     *
     * @param env 指定环境
     * @return true，表示指定环境为UP，否则，false
     */
    private boolean isUp(Env env) {
      Health health = healthAPI.health(env);
      return "UP".equals(health.getStatus().getCode());
    }

    /**
     * 处理环境down状态的情况
     *
     * @param env
     */
    private void handleEnvDown(Env env) {
      //次数加1
      int failedTimes = healthCheckFailedCounter.get(env);
      healthCheckFailedCounter.put(env, ++failedTimes);
      //如果标记的是down，打印日志
      if (!envStatusMark.get(env)) {
        log.error("Env is down. env: {}, failed times: {}, meta server address: {}", env,
            failedTimes, portalMetaDomainService.getDomain(env));
      } else {
        //如果标记的为UP,但是失败次数超出了阈值，修改指定环境标记为false
        if (failedTimes >= ENV_DOWN_THRESHOLD) {
          envStatusMark.put(env, false);
          log.error("Env is down because health check failed for {} times, "
                  + "which equals to down threshold. env: {}, meta server address: {}",
              ENV_DOWN_THRESHOLD, env,
              portalMetaDomainService.getDomain(env));
        } else {
          //如果标记的为UP,但是失败次数没有超出阈值，仅打印日志
          log.error(
              "Env health check failed for {} times which less than down threshold. down threshold:{}, env: {}, meta server address: {}",
              failedTimes, ENV_DOWN_THRESHOLD, env, portalMetaDomainService.getDomain(env));
        }
      }

    }

  }
}
