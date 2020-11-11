package com.ctrip.framework.apollo.portal.controller;

import javax.servlet.ServletContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前缀路径Controller
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
public class PrefixPathController {

  private final ServletContext servletContext;

  /**
   * 配置的前缀路径的路径
   */
  @Deprecated
  @Value("${prefix.path:}")
  private String prefixPath;

  public PrefixPathController(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  /**
   * 前缀路径
   *
   * @return 如果配置了前缀路径的路径，就走配置的前缀路径，没有走contextPath
   */
  @GetMapping("/prefix-path")
  public String getPrefixPath() {
    if (StringUtils.isBlank(prefixPath)) {
      return servletContext.getContextPath();
    }
    return prefixPath;
  }

}
