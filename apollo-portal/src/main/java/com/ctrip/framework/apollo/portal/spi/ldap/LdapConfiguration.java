package com.ctrip.framework.apollo.portal.spi.ldap;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * LDAP 的自动配置类, 完成连接 及LdapTemplate生成
 */
@Profile("ldap")
@Configuration
public class LdapConfiguration {

  @Value("${spring.ldap.urls}")
  private String urls;

  @Value("${spring.ldap.base}")
  private String base;

  @Value("${spring.ldap.username}")
  private String username;

  @Value("${spring.ldap.password}")
  private String password;

  @Value("${spring.ldap.pooled}")
  private int pooled;

  private LdapTemplate ldapTemplate;

  @Bean
  public LdapTemplate ldapTemplate() {
    if (null == ldapTemplate) {
      LdapContextSource contextSource = new LdapContextSource();

      contextSource.setUrl(urls);
      contextSource.setBase(base);
      contextSource.setUserDn(username);
      contextSource.setPassword(password);
      contextSource.setPooled(pooled > 0);

      // 解决乱码问题
      Map<String, Object> config = new HashMap<>();
      config.put("java.naming.ldap.attributes.binary", "objectGUID");
      contextSource.setBaseEnvironmentProperties(config);

      ldapTemplate = new LdapTemplate(contextSource);
      ldapTemplate.setDefaultCountLimit(20);
      ldapTemplate.setIgnorePartialResultException(true);
    }

    return ldapTemplate;
  }

  @Bean
  public LdapContextSource contextSource() {
    LdapContextSource contextSource = new LdapContextSource();

    contextSource.setUrl(urls);
    contextSource.setBase(base);
    contextSource.setUserDn(username);
    contextSource.setPassword(password);
    contextSource.setPooled(pooled > 0);

    // 解决乱码问题
    Map<String, Object> config = new HashMap<>();
    config.put("java.naming.ldap.attributes.binary", "objectGUID");
    contextSource.setBaseEnvironmentProperties(config);

    return contextSource;
  }

}
