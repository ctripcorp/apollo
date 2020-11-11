package com.ctrip.framework.apollo.portal.spi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登出处理器
 */
public interface LogoutHandler {

  /**
   * 登出
   *
   * @param request  request请求实体
   * @param response response响应实体
   */
  void logout(HttpServletRequest request, HttpServletResponse response);
}