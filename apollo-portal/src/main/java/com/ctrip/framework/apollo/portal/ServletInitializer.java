package com.ctrip.framework.apollo.portal;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * web应用程序的入口点,如此配置打包后,war包才可在tomcat下使用
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ServletInitializer extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(PortalApplication.class);
  }

}
