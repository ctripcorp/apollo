package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.portal.spi.SsoHeartbeatHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SSO认证心跳 Controller层，由于sso认证信息有一个有限的过期时间，所以我们需要执行sso heartbeat以在不可用时刷新信息
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Controller
@RequestMapping("/sso_heartbeat")
public class SsoHeartbeatController {

  private final SsoHeartbeatHandler handler;

  public SsoHeartbeatController(final SsoHeartbeatHandler handler) {
    this.handler = handler;
  }

  /**
   * 心跳检查
   *
   * @param request  请求对象
   * @param response 响应对象
   */
  @GetMapping
  public void heartbeat(HttpServletRequest request, HttpServletResponse response) {
    handler.doHeartbeat(request, response);
  }
}
