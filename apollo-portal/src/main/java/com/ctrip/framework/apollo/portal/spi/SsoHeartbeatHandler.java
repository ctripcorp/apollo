package com.ctrip.framework.apollo.portal.spi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SSO心跳处理器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface SsoHeartbeatHandler {

  /**
   * 心跳检查
   *
   * @param request  请求对象
   * @param response 响应对象
   */
  void doHeartbeat(HttpServletRequest request, HttpServletResponse response);
}
