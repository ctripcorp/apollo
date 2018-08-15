package com.ctrip.framework.apollo.portal.spi.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import java.util.Collections;

/**
 * @author xm.lin xm.lin@anxincloud.com
 * @Description
 * @date 18-8-9 下午4:37
 */
@Configuration
@ConditionalOnClass(ContextSource.class)
@EnableConfigurationProperties(LdapProperties.class)
public class LdapAutoConfiguration {

  @Autowired
  private LdapProperties properties;
  @Autowired
  private Environment environment;

  @Bean
  @ConditionalOnMissingBean
  public ContextSource ldapContextSource() {
    LdapContextSource source = new LdapContextSource();
    source.setUserDn(this.properties.getUsername());
    source.setPassword(this.properties.getPassword());
    source.setAnonymousReadOnly(this.properties.getAnonymousReadOnly());
    source.setBase(this.properties.getBase());
    source.setUrls(this.properties.determineUrls(this.environment));
    source.setBaseEnvironmentProperties(
        Collections.unmodifiableMap(this.properties.getBaseEnvironment()));
    return source;
  }

  @Bean
  @ConditionalOnMissingBean(LdapOperations.class)
  public LdapTemplate ldapTemplate(ContextSource contextSource) {
    return new LdapTemplate(contextSource);
  }

}
