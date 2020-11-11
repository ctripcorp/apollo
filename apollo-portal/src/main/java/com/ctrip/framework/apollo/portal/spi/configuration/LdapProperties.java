package com.ctrip.framework.apollo.portal.spi.configuration;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Ldap属性实体
 *
 * @author xm.lin xm.lin@anxincloud.com
 * @Description
 * @date 18-8-9 下午4:36
 */
@Data
@ConfigurationProperties(prefix = "spring.ldap")
public class LdapProperties {

  /**
   * 默认的端口
   */
  private static final int DEFAULT_PORT = 389;

  /**
   * 服务器的LDAP URl数组
   */
  private String[] urls;

  /**
   * 所有操作都应该源自的基后缀(Base suffix from which all operations should originate)
   */
  private String base;

  /**
   * 服务登录的用户名
   */
  private String username;

  /**
   * 服务登录的密码
   */
  private String password;

  /**
   * 只读操作是否应使用匿名环境(Whether read-only operations should use an anonymous environment).
   */
  private boolean anonymousReadOnly;

  /**
   * 用户过滤器，登录的时候用这个过滤器来搜索用户
   */
  private String searchFilter;

  /**
   * 基本环境 LDAP规范设置（specification settings）.
   */
  private final Map<String, String> baseEnvironment = new HashMap<>();

  /**
   * 确定URL
   *
   * @param environment 环境对象
   * @return 返回指定环境的Url列表
   */
  public String[] determineUrls(Environment environment) {
    if (ObjectUtils.isEmpty(this.urls)) {
      return new String[]{"ldap://localhost:" + determinePort(environment)};
    }
    return this.urls;
  }

  /**
   * 确定端口
   *
   * @param environment 环境对象
   * @return 返回指定环境的端口
   */
  private int determinePort(Environment environment) {
    Assert.notNull(environment, "Environment must not be null");
    String localPort = environment.getProperty("local.ldap.port");
    if (localPort != null) {
      return Integer.parseInt(localPort);
    }
    return DEFAULT_PORT;
  }
}
