package com.ctrip.framework.apollo.portal.spi.ldap;

import com.ctrip.framework.apollo.portal.spi.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


public class UserLogoutHandler implements LogoutHandler {

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        //将session销毁
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }
        ThreadLocalUserInfoHolder.clear();
        try {
            response.sendRedirect("/toLogin");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
