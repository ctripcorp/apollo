package com.ctrip.framework.apollo.openapi.filter;

import com.ctrip.framework.apollo.openapi.util.ConsumerAuditUtil;
import com.ctrip.framework.apollo.openapi.util.ConsumerAuthUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConsumerAuthenticationFilter implements Filter {
  private ConsumerAuthUtil consumerAuthUtil;
  private ConsumerAuditUtil consumerAuditUtil;

  public ConsumerAuthenticationFilter(ConsumerAuthUtil consumerAuthUtil, ConsumerAuditUtil consumerAuditUtil) {
    this.consumerAuthUtil = consumerAuthUtil;
    this.consumerAuditUtil = consumerAuditUtil;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    //nothing
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws
      IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) resp;

    String token = request.getHeader("Authorization");

    Long consumerId = consumerAuthUtil.getConsumerId(token);

    if (consumerId == null) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
      return;
    }

    consumerAuthUtil.storeConsumerId(request, consumerId);
    consumerAuditUtil.audit(request, consumerId);

    chain.doFilter(req, resp);
  }

  @Override
  public void destroy() {
    //nothing
  }
}
