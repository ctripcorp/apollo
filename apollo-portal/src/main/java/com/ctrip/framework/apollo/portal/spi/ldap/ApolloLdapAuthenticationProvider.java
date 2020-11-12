

package com.ctrip.framework.apollo.portal.spi.ldap;

import com.ctrip.framework.apollo.portal.spi.configuration.LdapExtendProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.util.Assert;


/**
 * Apollo Ldap身份验证提供器，从LdapAuthenticationProvider继承并重写了身份验证方法，修改了前一个用户输入使用的userId，并更改为在LDAP系统中使用该userId。
 *
 * @author wuzishu
 */
public class ApolloLdapAuthenticationProvider extends LdapAuthenticationProvider {

  /**
   * Ldap扩展属性
   */
  private LdapExtendProperties properties;

  /**
   * 构建Apollo Ldap身份验证提供器
   *
   * @param authenticator        Ldap身份验证
   * @param authoritiesPopulator Ldap权限人士
   */
  public ApolloLdapAuthenticationProvider(LdapAuthenticator authenticator,
      LdapAuthoritiesPopulator authoritiesPopulator) {
    super(authenticator, authoritiesPopulator);
  }

  /**
   * 构建Apollo Ldap身份验证提供器
   *
   * @param authenticator Ldap身份验证
   */
  public ApolloLdapAuthenticationProvider(LdapAuthenticator authenticator) {
    super(authenticator);
  }

  /**
   * 构建Apollo Ldap身份验证提供器
   *
   * @param authenticator        Ldap身份验证
   * @param authoritiesPopulator Ldap权限人士
   * @param properties           Ldap扩展属性
   */
  public ApolloLdapAuthenticationProvider(LdapAuthenticator authenticator,
      LdapAuthoritiesPopulator authoritiesPopulator, LdapExtendProperties properties) {
    super(authenticator, authoritiesPopulator);
    this.properties = properties;
  }

  /**
   * 构建Apollo Ldap身份验证提供器
   *
   * @param authenticator Ldap身份验证
   * @param properties    Ldap扩展属性
   */
  public ApolloLdapAuthenticationProvider(LdapAuthenticator authenticator,
      LdapExtendProperties properties) {
    super(authenticator);
    this.properties = properties;
  }

  /**
   * 认证，使用与AuthenticationManager.authenticate（Authentication）。
   *
   * @param authentication 身份验证对象
   * @return 包含凭据的完全身份验证对象。如果AuthenticationProvider无法支持对传递的身份验证对象进行身份验证，则可能返回null。在这种情况下，将尝试下一个支持当前身份验证类的AuthenticationProvider。
   * @throws AuthenticationException 如果身份验证失败，抛出
   */
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication, this.messages
        .getMessage("LdapAuthenticationProvider.onlySupports",
            "Only UsernamePasswordAuthenticationToken is supported"));
    // 获取用户名和密码
    UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) authentication;
    String username = userToken.getName();
    String password = (String) authentication.getCredentials();
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Processing authentication request for user: " + username);
    }

    // 校验用户名和密码
    if (StringUtils.isBlank(username)) {
      throw new BadCredentialsException(
          this.messages.getMessage("LdapAuthenticationProvider.emptyUsername", "Empty Username"));
    }
    if (StringUtils.isBlank(password)) {
      throw new BadCredentialsException(this.messages
          .getMessage("AbstractLdapAuthenticationProvider.emptyPassword", "Empty Password"));
    }
    Assert.notNull(password, "Null password was supplied in authentication token");

    // 用户数据
    DirContextOperations userData = this.doAuthentication(userToken);
    String loginId = userData.getStringAttribute(properties.getMapping().getLoginId());
    UserDetails user = this.userDetailsContextMapper.mapUserFromContext(userData, loginId,
        this.loadUserAuthorities(userData, loginId, (String) authentication.getCredentials()));
    // 认证成功创建认证对象
    return this.createSuccessfulAuthentication(userToken, user);
  }
}
