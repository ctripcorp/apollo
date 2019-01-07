package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.portal.spi.SsoHeartbeatHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Since sso auth information has a limited expiry time, so we need to do sso heartbeat to keep the
 * information refreshed when unavailable
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Controller
@RequestMapping("/sso_heartbeat")
public class SsoHeartbeatController {
  @Autowired
  private SsoHeartbeatHandler handler;

  @GetMapping
  public void heartbeat(HttpServletRequest request, HttpServletResponse response) {
    handler.doHeartbeat(request, response);
  }
}
