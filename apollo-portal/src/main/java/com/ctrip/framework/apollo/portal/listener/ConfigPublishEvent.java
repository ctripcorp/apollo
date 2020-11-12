package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.portal.environment.Env;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 配置发布事件
 */
public class ConfigPublishEvent extends ApplicationEvent {

  /**
   * 配置发布信息
   */
  private ConfigPublishInfo configPublishInfo;

  /**
   * 构建配置发布事件
   *
   * @param source 事件源对象
   */
  public ConfigPublishEvent(Object source) {
    super(source);
    configPublishInfo = (ConfigPublishInfo) source;
  }

  /**
   * 获取配置发布事件实例
   *
   * @return 配置发布事件实例
   */
  public static ConfigPublishEvent instance() {
    ConfigPublishInfo info = new ConfigPublishInfo();
    return new ConfigPublishEvent(info);
  }

  /**
   * 获取配置发布信息
   *
   * @return 配置发布信息
   */
  public ConfigPublishInfo getConfigPublishInfo() {
    return configPublishInfo;
  }

  /**
   * 设置应用id
   *
   * @param appId 应用id
   * @return 配置发布事件对象
   */
  public ConfigPublishEvent withAppId(String appId) {
    configPublishInfo.setAppId(appId);
    return this;
  }

  /**
   * 设置集群名称
   *
   * @param clusterName 集群名称
   * @return 配置发布事件对象
   */
  public ConfigPublishEvent withCluster(String clusterName) {
    configPublishInfo.setClusterName(clusterName);
    return this;
  }

  /**
   * 设置名称空间名称
   *
   * @param namespaceName 名称空间名称
   * @return 配置发布事件对象
   */
  public ConfigPublishEvent withNamespace(String namespaceName) {
    configPublishInfo.setNamespaceName(namespaceName);
    return this;
  }

  /**
   * 设置发布id
   *
   * @param releaseId 发布id
   * @return 配置发布事件对象
   */
  public ConfigPublishEvent withReleaseId(long releaseId) {
    configPublishInfo.setReleaseId(releaseId);
    return this;
  }

  /**
   * 设置上一次发布id
   *
   * @param previousReleaseId 上一次发布id
   * @return 配置发布事件对象
   */
  public ConfigPublishEvent withPreviousReleaseId(long previousReleaseId) {
    configPublishInfo.setPreviousReleaseId(previousReleaseId);
    return this;
  }

  /**
   * 设置是否正常发布事件
   *
   * @param isNormalPublishEvent 是否正常发布事件
   * @return 配置发布事件对象
   */
  public ConfigPublishEvent setNormalPublishEvent(boolean isNormalPublishEvent) {
    configPublishInfo.setNormalPublishEvent(isNormalPublishEvent);
    return this;
  }

  /**
   * 设置是否灰度发布事件
   *
   * @param isGrayPublishEvent 是否灰度发布事件
   * @return 配置发布事件对象
   */
  public ConfigPublishEvent setGrayPublishEvent(boolean isGrayPublishEvent) {
    configPublishInfo.setGrayPublishEvent(isGrayPublishEvent);
    return this;
  }

  /**
   * 设置是否回滚事件
   *
   * @param isRollbackEvent 是否回滚事件
   * @return 配置发布事件对象
   */
  public ConfigPublishEvent setRollbackEvent(boolean isRollbackEvent) {
    configPublishInfo.setRollbackEvent(isRollbackEvent);
    return this;
  }

  /**
   * 设置是否全量发布事件
   *
   * @param isMergeEvent 是否全量发布事件
   * @return 配置发布事件对象
   */
  public ConfigPublishEvent setMergeEvent(boolean isMergeEvent) {
    configPublishInfo.setMergeEvent(isMergeEvent);
    return this;
  }

  /**
   * 设置环境
   *
   * @param env 环境
   * @return 配置发布事件对象
   */
  public ConfigPublishEvent setEnv(Env env) {
    configPublishInfo.setEnv(env);
    return this;
  }


  /**
   * 配置发布信息
   */
  public static class ConfigPublishInfo {

    /**
     * 环境
     */
    private String env;
    /**
     * 应用id
     */
    @Getter
    @Setter
    private String appId;
    /**
     * 集群名称
     */
    @Getter
    @Setter
    private String clusterName;
    /**
     * 名称空间名称
     */
    @Getter
    @Setter
    private String namespaceName;
    /**
     * 发布id
     */
    @Getter
    @Setter
    private long releaseId;
    /**
     * 上一次的发布id
     */
    @Getter
    @Setter
    private long previousReleaseId;
    /**
     * 是否回滚事件
     */
    @Getter
    @Setter
    private boolean isRollbackEvent;
    /**
     * 是否全量发布事件
     */
    @Getter
    @Setter
    private boolean isMergeEvent;
    /**
     * 是否正常发布事件
     */
    @Getter
    @Setter
    private boolean isNormalPublishEvent;
    /**
     * 是否灰度发布事件
     */
    @Getter
    @Setter
    private boolean isGrayPublishEvent;

    public Env getEnv() {
      return Env.valueOf(env);
    }

    public void setEnv(Env env) {
      this.env = env.toString();
    }
  }
}
