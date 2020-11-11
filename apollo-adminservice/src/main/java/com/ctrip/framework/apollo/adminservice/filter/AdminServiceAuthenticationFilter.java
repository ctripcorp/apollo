package com.ctrip.framework.apollo.adminservice.filter;

import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

/**
 * 系统服务权限过滤器
 */
@Slf4j
public class AdminServiceAuthenticationFilter implements Filter {

  /**
   * 访问令牌分隔器
   */
  private static final Splitter ACCESS_TOKEN_SPLITTER = Splitter.on(",").omitEmptyStrings()
      .trimResults();

  /**
   * 业务配置
   */
  private final BizConfig bizConfig;
  /**
   * 最后一次访问令牌列表的字符串表示（逗号分割）
   */
  private volatile String lastAccessTokens;
  /**
   * 访问token列表信息
   */
  private volatile List<String> accessTokenList;

  public AdminServiceAuthenticationFilter(BizConfig bizConfig) {
    this.bizConfig = bizConfig;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  /**
   * 过滤，当开启系统服务访问权限控制的时，过滤访问token不存在的请求
   *
   * @param req   请求对象
   * @param resp  响应对象
   * @param chain 过滤链
   * @throws IOException      如果在处理请求期间发生I/O错误，抛出
   * @throws ServletException 如果处理因其他原因失败，抛出
   */
  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
      throws IOException, ServletException {
    // 开启系统服务访问权限控制的情况
    if (bizConfig.isAdminServiceAccessControlEnabled()) {
      HttpServletRequest request = (HttpServletRequest) req;
      HttpServletResponse response = (HttpServletResponse) resp;

      String token = request.getHeader(HttpHeaders.AUTHORIZATION);

      // 过滤访问token不存在的请求
      if (!checkAccessToken(token)) {
        log.warn("Invalid access token: {} for uri: {}", token, request.getRequestURI());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return;
      }
    }

    chain.doFilter(req, resp);
  }

  /**
   * 检查指定的token是否存在
   *
   * @param token 访问token
   * @return true, 指定的token存在，否则，false
   */
  private boolean checkAccessToken(String token) {
    // 允许访问apollo-admin服务的访问token列表
    String accessTokens = bizConfig.getAdminServiceAccessTokens();

    // 如果用户忘记配置访问令牌，则默认为pass
    if (Strings.isNullOrEmpty(accessTokens)) {
      return true;
    }

    // 不需要检查
    if (Strings.isNullOrEmpty(token)) {
      return false;
    }

    // 更新缓存
    if (!accessTokens.equals(lastAccessTokens)) {
      synchronized (this) {
        accessTokenList = ACCESS_TOKEN_SPLITTER.splitToList(accessTokens);
        lastAccessTokens = accessTokens;
      }
    }
    // 指定token是否存在
    return accessTokenList.contains(token);
  }

  @Override
  public void destroy() {

  }
}
