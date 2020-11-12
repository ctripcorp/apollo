package com.ctrip.framework.apollo.openapi.filter;

import com.ctrip.framework.apollo.openapi.util.ConsumerAuditUtil;
import com.ctrip.framework.apollo.openapi.util.ConsumerAuthUtil;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

/**
 * OpenAPI 认证( Authentication )过滤器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConsumerAuthenticationFilter implements Filter {

  private ConsumerAuthUtil consumerAuthUtil;
  private ConsumerAuditUtil consumerAuditUtil;

  public ConsumerAuthenticationFilter(ConsumerAuthUtil consumerAuthUtil,
      ConsumerAuditUtil consumerAuditUtil) {
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

    // 从请求 Header 中，获得 token
    String token = request.getHeader(HttpHeaders.AUTHORIZATION);

    // 获得 Consumer 编号
    Long consumerId = consumerAuthUtil.getConsumerId(token);
    // 若不存在，返回错误状态码 401
    if (consumerId == null) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
      return;
    }
    // 存储 Consumer 编号到请求中
    consumerAuthUtil.storeConsumerId(request, consumerId);
    // 记录 ConsumerAudit 记录
    consumerAuditUtil.audit(request, consumerId);

    chain.doFilter(req, resp);
  }

  @Override
  public void destroy() {
    //nothing
  }
}
