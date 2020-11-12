package com.ctrip.framework.apollo.portal.spi.ctrip;

import com.ctrip.framework.apollo.portal.spi.SsoHeartbeatHandler;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ctrip 心跳处理器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class CtripSsoHeartbeatHandler implements SsoHeartbeatHandler {

  @Override
  public void doHeartbeat(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 重定向至"ctrip_sso_heartbeat.html页面
      response.sendRedirect("ctrip_sso_heartbeat.html");
    } catch (IOException e) {
    }
  }

}
