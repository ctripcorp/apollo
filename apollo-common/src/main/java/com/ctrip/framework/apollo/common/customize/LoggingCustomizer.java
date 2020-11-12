package com.ctrip.framework.apollo.common.customize;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.foundation.Foundation;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * clogging-agent 配置.仅在ctrip使用
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public abstract class LoggingCustomizer implements InitializingBean {

  /**
   * cLogging追加器类全路径
   */
  private static final String cLoggingAppenderClass =
      "com.ctrip.framework.clogging.agent.appender.CLoggingAppender";
  private static boolean cLoggingAppenderPresent =
      ClassUtils.isPresent(cLoggingAppenderClass, LoggingCustomizer.class.getClassLoader());

  @Override
  public void afterPropertiesSet() {
    if (!cLoggingAppenderPresent) {
      return;
    }

    try {
      tryConfigCLogging();
    } catch (Throwable ex) {
      log.error("Config CLogging failed", ex);
      Tracer.logError(ex);
    }

  }

  /**
   * 初始化配置
   *
   * @throws Exception
   */
  private void tryConfigCLogging() throws Exception {
    //应用id
    String appId = Foundation.app().getAppId();
    if (Strings.isNullOrEmpty(appId)) {
      log.warn("App id is null or empty!");
      return;
    }

    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    Class clazz = Class.forName(cLoggingAppenderClass);
    //追加器
    Appender cLoggingAppender = (Appender) clazz.newInstance();

    // 通过反射构建追加器
    ReflectionUtils.findMethod(clazz, "setAppId", String.class).invoke(cLoggingAppender, appId);
    ReflectionUtils.findMethod(clazz, "setServerIp", String.class)
        .invoke(cLoggingAppender, cloggingUrl());
    ReflectionUtils.findMethod(clazz, "setServerPort", int.class)
        .invoke(cLoggingAppender, Integer.parseInt(cloggingPort()));

    cLoggingAppender.setName("CentralLogging");
    cLoggingAppender.setContext(loggerContext);
    cLoggingAppender.start();

    ch.qos.logback.classic.Logger logger =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root");
    logger.addAppender(cLoggingAppender);

  }

  /**
   * clogging 服务的Url
   *
   * @return clogging 服务的Url
   */
  protected abstract String cloggingUrl();

  /**
   * clogging 服务的端口
   *
   * @return clogging 服务的端口
   */
  protected abstract String cloggingPort();


}
