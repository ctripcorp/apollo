package com.ctrip.framework.apollo.configservice.util;

import com.ctrip.framework.apollo.configservice.service.AccessKeyServiceWithCache;
import com.ctrip.framework.apollo.core.signature.Signature;
import com.google.common.base.Strings;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author nisiyong
 */
@Component
public class AccessKeyUtil {

  private static final String URL_SEPARATOR = "/";
  private static final String URL_CONFIGS_PREFIX = "/configs/";
  private static final String URL_CONFIGFILES_JSON_PREFIX = "/configfiles/json/";
  private static final String URL_CONFIGFILES_PREFIX = "/configfiles/";
  private static final String URL_NOTIFICATIONS_PREFIX = "/notifications/v2";

  private final AccessKeyServiceWithCache accessKeyServiceWithCache;

  public AccessKeyUtil(AccessKeyServiceWithCache accessKeyServiceWithCache) {
    this.accessKeyServiceWithCache = accessKeyServiceWithCache;
  }

  public List<String> findAvailableSecret(String appId) {
    return accessKeyServiceWithCache.getAvailableSecrets(appId);
  }

  public String extractAppIdFromRequest(HttpServletRequest request) {
    String appId = null;
    String servletPath = request.getServletPath();

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

  public String buildSignature(String path, String query, String timestampString, String secret) {
    String pathWithQuery = path;
    if (!Strings.isNullOrEmpty(query)) {
      pathWithQuery += "?" + query;
    }

    return Signature.signature(timestampString, pathWithQuery, secret);
  }
}
