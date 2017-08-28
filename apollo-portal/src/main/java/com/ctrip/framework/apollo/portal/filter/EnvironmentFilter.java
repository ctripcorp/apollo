package com.ctrip.framework.apollo.portal.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;

/**
 * 环境权限认证过滤filter
 *
 * @author bowen
 */
public class EnvironmentFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        Object object = session.getAttribute("SPRING_SECURITY_CONTEXT");
        if (object != null && object instanceof SecurityContextImpl) {
            User u = (User) ((SecurityContextImpl) object).getAuthentication().getPrincipal();
            String url = request.getRequestURI();
            String env = getEnv(url);
            if (env != null) {
                System.out.println(u.getUsername() + "   " + url + "   " + env);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String getEnv(String url) {
        if (url == null) {
            return null;
        }
        String[] keys = url.split("/");
        for (int i = 0; i < keys.length; i++) {
            if ("envs".equals(keys[i])) {
                return keys[i + 1];
            }
        }
        return null;
    }

    @Override
    public void destroy() {

    }
}