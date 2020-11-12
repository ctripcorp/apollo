package com.ctrip.framework.foundation.spi.provider;

import java.io.IOException;
import java.io.InputStream;

/**
 * 服务器供应器
 * <p>服务器相关属性的供应器
 */
public interface ServerProvider extends Provider {

  /**
   * 获取当前环境，如果没有设置返回{@code null}
   *
   * @return 当前环境，如果没有设置返回{@code null}
   */
  String getEnvType();

  /**
   * 当前环境是否已经设置
   *
   * @return true, 当前环境已经设置, 否则，false
   */
  boolean isEnvTypeSet();

  /**
   * 获取当前数据中心，如果没有设置返回null
   *
   * @return 当前数据中心，如果没有设置返回{@code null}
   */
  String getDataCenter();

  /**
   * 数据中心是否设置
   *
   * @return true, 数据中心已经设置，否则，false
   */
  boolean isDataCenterSet();

  /**
   * 使用指定的输入流初始化服务器提供程序
   *
   * @param in 指定的输入流
   * @throws IOException 读写数据出现异常，抛出.
   */
  void initialize(InputStream in) throws IOException;
}
