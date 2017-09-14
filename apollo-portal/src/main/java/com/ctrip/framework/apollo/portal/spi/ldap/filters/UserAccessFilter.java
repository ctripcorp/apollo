package com.ctrip.framework.apollo.portal.spi.ldap.filters;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.ldap.ThreadLocalUserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.ldap.XxdUserInfoHolder;
import com.google.common.base.Strings;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class UserAccessFilter implements Filter {

    private static final String STATIC_RESOURCE_REGEX = ".*\\.(js|png|css|woff2|map)$";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String requestUri = ((HttpServletRequest) request).getRequestURI();
        HttpSession session = ((HttpServletRequest) request).getSession();
        UserInfo currentUser = (UserInfo) session.getAttribute("currentUser");

        if (requestUri.equals("/doLogin")
                || requestUri.equals("/login.html")
                || requestUri.equals("/toLogin")
                || isStaticResource(requestUri)
                || currentUser != null) {

            ThreadLocalUserInfoHolder.clear();
            ThreadLocalUserInfoHolder.setUser(currentUser);
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).sendRedirect("/toLogin");
        }

    }

    @Override
    public void destroy() {

    }

    private boolean isStaticResource(String uri) {
        return !Strings.isNullOrEmpty(uri) && uri.matches(STATIC_RESOURCE_REGEX);
    }

}
