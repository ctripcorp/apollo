package com.ctrip.framework.apollo.core.enums;

import com.google.common.base.Preconditions;

/**
 * 环境枚举.
 * <p>
 * 以下是所有预定义环境的简要说明：
 * <ul>
 *   <li>LOCAL: 本地开发环境，假设您在没有网络接入的海上工作</li>
 *   <li>DEV: 开发环境</li>
 *   <li>FWS: 功能Web服务测试环境</li>
 *   <li>FAT: 功能验收测试环境 </li>
 *   <li>UAT: 用户验收测试环境</li>
 *   <li>LPT: 负载和性能测试环境</li>
 *   <li>PRO: 生产环境</li>
 *   <li>TOOLS:工具集环境，生产环境中的一个特殊区域，允许访问测试环境，例如Apollo门户应该部署在工具环境中</li>
 * </ul>
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public enum Env {
  /**
   * 本地开发环境( Local Development environment)
   */
  LOCAL,
  /**
   * 开发环境(Development environmentc)
   */
  DEV,
  /**
   * 功能Web服务测试环境( Feature Web Service Test environmentc)
   */
  FWS,
  /**
   * 功能验收测试环境(Feature Acceptance Test environment)
   */
  FAT,
  /**
   * 用户验收测试环境(User Acceptance Test environment)
   */
  UAT,
  /**
   * 负载和性能测试环境(Load and Performance Test environment)
   */
  LPT,
  /**
   * 生产环境(Production environment)
   */
  PRO,
  /**
   * 工具集环境
   */
  TOOLS,
  /**
   * 未知环境.
   */
  UNKNOWN;

  /**
   * 将环境字符串转枚举.
   *
   * @param env 环境字符串
   * @return 环境枚举
   */
  public static Env fromString(String env) {
    Env environment = EnvUtils.transformEnv(env);
    Preconditions.checkArgument(environment != UNKNOWN, String.format("Env %s is invalid", env));
    return environment;
  }
}
