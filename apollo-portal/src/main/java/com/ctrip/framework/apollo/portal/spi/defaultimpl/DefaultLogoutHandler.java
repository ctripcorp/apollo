package com.ctrip.framework.apollo.portal.spi.defaultimpl;

import com.ctrip.framework.apollo.portal.spi.LogoutHandler;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 默认登出处理器
 */
public class DefaultLogoutHandler implements LogoutHandler {

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    //直接重定向到主页，然后再跳转到登录页
    try {
      response.sendRedirect("/");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
