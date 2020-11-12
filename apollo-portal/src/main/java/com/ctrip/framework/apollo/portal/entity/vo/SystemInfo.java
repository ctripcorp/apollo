package com.ctrip.framework.apollo.portal.entity.vo;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统信息
 */
@Getter
public class SystemInfo {

  /**
   * 版本
   */
  @Setter
  private String version;
  /**
   * 环境信息列表
   */
  private List<EnvironmentInfo> environments = Lists.newLinkedList();

  /**
   * 添加环境信息
   *
   * @param environment 环境对象
   */
  public void addEnvironment(EnvironmentInfo environment) {
    this.environments.add(environment);
  }
}
