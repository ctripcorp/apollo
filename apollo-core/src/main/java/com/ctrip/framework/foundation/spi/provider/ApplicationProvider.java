package com.ctrip.framework.foundation.spi.provider;

import java.io.InputStream;

/**
 * 应用供应器
 * <p>应用程序相关属性的供应器
 */
public interface ApplicationProvider extends Provider {

  /**
   * 获取应用的AppId
   *
   * @return 应用的AppId
   */
  String getAppId();

  /**
   * 获取应用的访问密钥
   *
   * @return 应用的访问密钥
   */
  String getAccessKeySecret();

  /**
   * 应用的AppId是否设置
   *
   * @return true, 应用的AppId已经设置，否则，false
   */
  boolean isAppIdSet();

  /**
   * 使用指定的输入流初始化应用供应器
   *
   * @param in 指定的输入流
   */
  void initialize(InputStream in);
}
