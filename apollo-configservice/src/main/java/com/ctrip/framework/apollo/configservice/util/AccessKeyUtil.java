package com.ctrip.framework.apollo.configservice.util;

import com.ctrip.framework.apollo.configservice.service.AccessKeyServiceWithCache;
import com.ctrip.framework.apollo.core.signature.Signature;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 访问密钥工具类
 *
 * @author nisiyong
 */
@Component
public class AccessKeyUtil {

  /**
   * 请求分隔字符
   */
  private static final String URL_SEPARATOR = "/";
  /**
   * 配置URL的前缀
   */
  private static final String URL_CONFIGS_PREFIX = "/configs/";
  /**
   * Json配置文件Url前缀
   */
  private static final String URL_CONFIGFILES_JSON_PREFIX = "/configfiles/json/";
  /**
   * 配置文件url前缀
   */
  private static final String URL_CONFIGFILES_PREFIX = "/configfiles/";
  /**
   * 通知URL前缀
   */
  private static final String URL_NOTIFICATIONS_PREFIX = "/notifications/v2";

  /**
   * 访问密钥缓存Service
   */
  private final AccessKeyServiceWithCache accessKeyServiceWithCache;

  public AccessKeyUtil(AccessKeyServiceWithCache accessKeyServiceWithCache) {
    this.accessKeyServiceWithCache = accessKeyServiceWithCache;
  }

  /**
   * 查询可用的密钥
   *
   * @param appId 应用id
   * @return 密钥列表
   */
  public List<String> findAvailableSecret(String appId) {
    return accessKeyServiceWithCache.getAvailableSecrets(appId);
  }

  /**
   * 从请求中提取应用id
   *
   * @param request 请求对象
   * @return 应用id
   */
  public String extractAppIdFromRequest(HttpServletRequest request) {
    String appId = null;
    // 服务路径
    String servletPath = request.getServletPath();

    // 匹配指定前缀
    if (StringUtils.startsWith(servletPath, URL_CONFIGS_PREFIX)) {
      appId = StringUtils.substringBetween(servletPath, URL_CONFIGS_PREFIX, URL_SEPARATOR);
    } else if (StringUtils.startsWith(servletPath, URL_CONFIGFILES_JSON_PREFIX)) {
      appId = StringUtils.substringBetween(servletPath, URL_CONFIGFILES_JSON_PREFIX, URL_SEPARATOR);
    } else if (StringUtils.startsWith(servletPath, URL_CONFIGFILES_PREFIX)) {
      appId = StringUtils.substringBetween(servletPath, URL_CONFIGFILES_PREFIX, URL_SEPARATOR);
    } else if (StringUtils.startsWith(servletPath, URL_NOTIFICATIONS_PREFIX)) {
      appId = request.getParameter("appId");
    }
    return appId;
  }

  /**
   * 构建签名
   *
   * @param path            路径
   * @param query           查询
   * @param timestampString 时间字符串
   * @param secret          密钥
   * @return 签名
   */
  public String buildSignature(String path, String query, String timestampString, String secret) {
    String pathWithQuery = path;
    // 如果有查询参数,添加
    if (!StringUtils.isBlank(query)) {
      pathWithQuery += "?" + query;
    }

    return Signature.signature(timestampString, pathWithQuery, secret);
  }
}
