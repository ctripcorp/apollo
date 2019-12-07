package com.ctrip.framework.apollo.portal.spi.defaultimpl;

import com.ctrip.framework.apollo.portal.spi.SsoHeartbeatHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultSsoHeartbeatHandler implements SsoHeartbeatHandler {

  @Override
  public void doHeartbeat(HttpServletRequest request, HttpServletResponse response) {
    try {
        response.sendRedirect("default_sso_heartbeat.html");
    } catch (IOException e) {
    }
  }

}
