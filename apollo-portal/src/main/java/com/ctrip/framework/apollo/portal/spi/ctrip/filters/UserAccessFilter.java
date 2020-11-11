package com.ctrip.framework.apollo.portal.spi.ctrip.filters;

import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.base.Strings;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户访问过滤器
 */
public class UserAccessFilter implements Filter {

  private static final String STATIC_RESOURCE_REGEX = ".*\\.(js|html|htm|png|css|woff2)$";

  private UserInfoHolder userInfoHolder;

  public UserAccessFilter(UserInfoHolder userInfoHolder) {
    this.userInfoHolder = userInfoHolder;
  }

  @Override
  public void init(FilterConfig filterConfig) {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    String requestUri = ((HttpServletRequest) request).getRequestURI();

    try {
      // 如果不为开放API或者静态资源
      if (!isOpenAPIRequest(requestUri) && !isStaticResource(requestUri)) {
        UserInfo userInfo = userInfoHolder.getUser();
        // 不为空，记录访问事件
        if (userInfo != null) {
          Tracer.logEvent(TracerEventType.USER_ACCESS, userInfo.getUserId());
        }
      }
    } catch (Throwable e) {
      Tracer.logError("Record user access info error.", e);
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {

  }

  /**
   * 是否为开放API请求
   *
   * @param uri uri字符串
   * @return true, 是开放API请求，否则，false
   */
  private boolean isOpenAPIRequest(String uri) {
    return !Strings.isNullOrEmpty(uri) && uri.startsWith("/openapi");
  }

  /**
   * 是否是静态资源
   *
   * @param uri uri字符串
   * @return true, 是静态资源，否则，false
   */
  private boolean isStaticResource(String uri) {
    return !Strings.isNullOrEmpty(uri) && uri.matches(STATIC_RESOURCE_REGEX);
  }

}
