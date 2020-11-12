package com.ctrip.framework.apollo.configservice.filter;

import com.ctrip.framework.apollo.configservice.util.AccessKeyUtil;
import com.ctrip.framework.apollo.core.signature.Signature;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.google.common.net.HttpHeaders;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 客户端认证过滤器
 *
 * @author nisiyong
 */
@Slf4j
public class ClientAuthenticationFilter implements Filter {


  private static final Long TIMESTAMP_INTERVAL = 60 * 1000L;
  /**
   * 访问密钥工具类
   */
  private final AccessKeyUtil accessKeyUtil;

  public ClientAuthenticationFilter(AccessKeyUtil accessKeyUtil) {
    this.accessKeyUtil = accessKeyUtil;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    //nothing
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) resp;

    // 从请求中提取的应用id
    String appId = accessKeyUtil.extractAppIdFromRequest(request);
    if (StringUtils.isBlank(appId)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "InvalidAppId");
      return;
    }

    // 查询可用的密钥
    List<String> availableSecrets = accessKeyUtil.findAvailableSecret(appId);
    if (CollectionUtils.isNotEmpty(availableSecrets)) {
      // http请求中header时间戳
      String timestamp = request.getHeader(Signature.HTTP_HEADER_TIMESTAMP);
      // header中的认证字符串
      String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

      // 检查时间戳，1分钟内有效
      if (!checkTimestamp(timestamp)) {
        log.warn("Invalid timestamp. appId={},timestamp={}", appId, timestamp);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "RequestTimeTooSkewed");
        return;
      }

        // 检查签名
      String uri = request.getRequestURI();
      String query = request.getQueryString();
      if (!checkAuthorization(authorization, availableSecrets, timestamp, uri, query)) {
        log.warn("Invalid authorization. appId={},authorization={}", appId, authorization);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return;
      }
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    //nothing
  }

  /**
   * 检查时间戳范围（检查时间戳，1分钟内有效）
   *
   * @param timestamp 时间戳
   * @return true, 有效，否则，false
   */
  private boolean checkTimestamp(String timestamp) {
    // 请求时间（毫秒）
    long requestTimeMillis = 0L;
    try {
      requestTimeMillis = Long.parseLong(timestamp);
    } catch (NumberFormatException e) {
      // nothing to do
    }

    long x = System.currentTimeMillis() - requestTimeMillis;
    // 时间差为±TIMESTAMP_INTERVAL，
    return x >= -TIMESTAMP_INTERVAL && x <= TIMESTAMP_INTERVAL;
  }

  /**
   * 检查认证
   *
   * @param authorization    认证字符串
   * @param availableSecrets 可用的密钥
   * @param timestamp        时间戳
   * @param path             路径
   * @param query            查询参数
   * @return true, 认证有效，否则，false
   */
  private boolean checkAuthorization(String authorization, List<String> availableSecrets,
      String timestamp, String path, String query) {

    String signature = null;
    if (authorization != null) {
      String[] split = authorization.split(":");
      if (split.length > 1) {
        signature = split[1];
      }
    }

    // 循环遍历，检查签名
    for (String secret : availableSecrets) {
      // 构建签名
      String availableSignature = accessKeyUtil.buildSignature(path, query, timestamp, secret);
      // 签名相等说明可用
      if (Objects.equals(signature, availableSignature)) {
        return true;
      }
    }
    return false;
  }
}
