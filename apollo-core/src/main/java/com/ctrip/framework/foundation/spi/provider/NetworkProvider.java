package com.ctrip.framework.foundation.spi.provider;

/**
 * 网络供应器.
 * <p>网络相关属性的供应器</p>
 */
public interface NetworkProvider extends Provider {

  /**
   * 获取主机地址，即ip.
   *
   * @return 主机地址，即ip
   */
  String getHostAddress();

  /**
   * 获取主机名.
   *
   * @return 主机名
   */
  String getHostName();
}
