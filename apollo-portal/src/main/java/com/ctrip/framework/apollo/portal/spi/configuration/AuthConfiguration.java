package com.ctrip.framework.apollo.portal.spi.configuration;

import com.ctrip.framework.apollo.common.condition.ConditionalOnMissingProfile;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.spi.LogoutHandler;
import com.ctrip.framework.apollo.portal.spi.SsoHeartbeatHandler;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.ctrip.framework.apollo.portal.spi.ctrip.CtripLogoutHandler;
import com.ctrip.framework.apollo.portal.spi.ctrip.CtripSsoHeartbeatHandler;
import com.ctrip.framework.apollo.portal.spi.ctrip.CtripUserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.ctrip.CtripUserService;
import com.ctrip.framework.apollo.portal.spi.defaultimpl.DefaultLogoutHandler;
import com.ctrip.framework.apollo.portal.spi.defaultimpl.DefaultSsoHeartbeatHandler;
import com.ctrip.framework.apollo.portal.spi.defaultimpl.DefaultUserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.defaultimpl.DefaultUserService;
import com.ctrip.framework.apollo.portal.spi.ldap.ApolloLdapAuthenticationProvider;
import com.ctrip.framework.apollo.portal.spi.ldap.FilterLdapByGroupUserSearch;
import com.ctrip.framework.apollo.portal.spi.ldap.LdapUserService;
import com.ctrip.framework.apollo.portal.spi.springsecurity.SpringSecurityUserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.springsecurity.SpringSecurityUserService;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.EventListener;
import java.util.Map;
import javax.servlet.Filter;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * 认证配置
 */
@Configuration
public class AuthConfiguration {

  private static final String[] BY_PASS_URLS = {"/prometheus/**", "/metrics/**", "/openapi/**",
      "/vendor/**", "/styles/**", "/scripts/**", "/views/**", "/img/**", "/i18n/**", "/prefix-path",
      "/health"};

  /**
   * Ctrip认证配置， spring.profiles.active = ctrip
   */
  @Configuration
  @Profile("ctrip")
  static class CtripAuthAutoConfiguration {

    private final PortalConfig portalConfig;

    public CtripAuthAutoConfiguration(final PortalConfig portalConfig) {
      this.portalConfig = portalConfig;
    }

    @Bean
    public ServletListenerRegistrationBean redisAppSettingListner() {
      ServletListenerRegistrationBean redisAppSettingListener = new ServletListenerRegistrationBean();
      redisAppSettingListener
          .setListener(listener("org.jasig.cas.client.credis.CRedisAppSettingListner"));
      return redisAppSettingListener;
    }

    @Bean
    public ServletListenerRegistrationBean singleSignOutHttpSessionListener() {
      ServletListenerRegistrationBean singleSignOutHttpSessionListener = new ServletListenerRegistrationBean();
      singleSignOutHttpSessionListener
          .setListener(listener("org.jasig.cas.client.session.SingleSignOutHttpSessionListener"));
      return singleSignOutHttpSessionListener;
    }

    @Bean
    public FilterRegistrationBean casFilter() {
      FilterRegistrationBean singleSignOutFilter = new FilterRegistrationBean();
      singleSignOutFilter.setFilter(filter("org.jasig.cas.client.session.SingleSignOutFilter"));
      singleSignOutFilter.addUrlPatterns("/*");
      singleSignOutFilter.setOrder(1);
      return singleSignOutFilter;
    }

    @Bean
    public FilterRegistrationBean authenticationFilter() {
      FilterRegistrationBean casFilter = new FilterRegistrationBean();

      Map<String, String> filterInitParam = Maps.newHashMap();
      filterInitParam.put("redisClusterName", "casClientPrincipal");
      filterInitParam.put("serverName", portalConfig.portalServerName());
      filterInitParam.put("casServerLoginUrl", portalConfig.casServerLoginUrl());
      //we don't want to use session to store login information, since we will be deployed to a cluster, not a single instance
      filterInitParam.put("useSession", "false");
      filterInitParam.put("/openapi.*", "exclude");

      casFilter.setInitParameters(filterInitParam);
      casFilter
          .setFilter(filter("com.ctrip.framework.apollo.sso.filter.ApolloAuthenticationFilter"));
      casFilter.addUrlPatterns("/*");
      casFilter.setOrder(2);

      return casFilter;
    }

    @Bean
    public FilterRegistrationBean casValidationFilter() {
      FilterRegistrationBean casValidationFilter = new FilterRegistrationBean();
      Map<String, String> filterInitParam = Maps.newHashMap();
      filterInitParam.put("casServerUrlPrefix", portalConfig.casServerUrlPrefix());
      filterInitParam.put("serverName", portalConfig.portalServerName());
      filterInitParam.put("encoding", "UTF-8");
      //we don't want to use session to store login information, since we will be deployed to a cluster, not a single instance
      filterInitParam.put("useSession", "false");
      filterInitParam.put("useRedis", "true");
      filterInitParam.put("redisClusterName", "casClientPrincipal");

      casValidationFilter
          .setFilter(
              filter("org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter"));
      casValidationFilter.setInitParameters(filterInitParam);
      casValidationFilter.addUrlPatterns("/*");
      casValidationFilter.setOrder(3);

      return casValidationFilter;
    }

    @Bean
    public FilterRegistrationBean assertionHolder() {
      FilterRegistrationBean assertionHolderFilter = new FilterRegistrationBean();

      Map<String, String> filterInitParam = Maps.newHashMap();
      filterInitParam.put("/openapi.*", "exclude");

      assertionHolderFilter.setInitParameters(filterInitParam);

      assertionHolderFilter.setFilter(
          filter("com.ctrip.framework.apollo.sso.filter.ApolloAssertionThreadLocalFilter"));
      assertionHolderFilter.addUrlPatterns("/*");
      assertionHolderFilter.setOrder(4);

      return assertionHolderFilter;
    }

    @Bean
    public CtripUserInfoHolder ctripUserInfoHolder() {
      return new CtripUserInfoHolder();
    }

    @Bean
    public CtripLogoutHandler logoutHandler() {
      return new CtripLogoutHandler();
    }

    private Filter filter(String className) {
      Class clazz = null;
      try {
        clazz = Class.forName(className);
        Object obj = clazz.newInstance();
        return (Filter) obj;
      } catch (Exception e) {
        throw new RuntimeException("instance filter fail", e);
      }
    }

    private EventListener listener(String className) {
      Class clazz = null;
      try {
        clazz = Class.forName(className);
        Object obj = clazz.newInstance();
        return (EventListener) obj;
      } catch (Exception e) {
        throw new RuntimeException("instance listener fail", e);
      }
    }

    @Bean
    public UserService ctripUserService(PortalConfig portalConfig) {
      return new CtripUserService(portalConfig);
    }

    @Bean
    public SsoHeartbeatHandler ctripSsoHeartbeatHandler() {
      return new CtripSsoHeartbeatHandler();
    }
  }

  /**
   * Spring Security 认证配置， spring.profiles.active = auth
   */
  @Configuration
  @Profile("auth")
  static class SpringSecurityAuthAutoConfiguration {

    /**
     * SSO心跳处理器
     *
     * @return SSO心跳处理器bean
     */
    @Bean
    @ConditionalOnMissingBean(SsoHeartbeatHandler.class)
    public SsoHeartbeatHandler defaultSsoHeartbeatHandler() {
      return new DefaultSsoHeartbeatHandler();
    }

    /**
     * 用户信息持有器
     *
     * @return 用户信息持有器bean
     */
    @Bean
    @ConditionalOnMissingBean(UserInfoHolder.class)
    public UserInfoHolder springSecurityUserInfoHolder() {
      return new SpringSecurityUserInfoHolder();
    }

    /**
     * 登出处理器
     *
     * @return 登出处理器对象bean
     */
    @Bean
    @ConditionalOnMissingBean(LogoutHandler.class)
    public LogoutHandler logoutHandler() {
      return new DefaultLogoutHandler();
    }

    /**
     * 用户明细管理器
     *
     * @param auth       认证管理构建器
     * @param datasource 数据源
     * @return 用户明细管理器bean
     * @throws Exception 如果在添加JDBC身份验证时发生错误，抛出
     */
    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager(AuthenticationManagerBuilder auth,
        DataSource datasource) throws Exception {
      JdbcUserDetailsManager jdbcUserDetailsManager = auth.jdbcAuthentication()
          .passwordEncoder(new BCryptPasswordEncoder()).dataSource(datasource)
          .usersByUsernameQuery("select Username,Password,Enabled from `Users` where Username = ?")
          .authoritiesByUsernameQuery(
              "select Username,Authority from `Authorities` where Username = ?")
          .getUserDetailsService();

      jdbcUserDetailsManager.setUserExistsSql("select Username from `Users` where Username = ?");
      jdbcUserDetailsManager
          .setCreateUserSql("insert into `Users` (Username, Password, Enabled) values (?,?,?)");
      jdbcUserDetailsManager
          .setUpdateUserSql(
              "update `Users` set Password = ?, Enabled = ? where id = (select u.id from (select id from `Users` where Username = ?) as u)");
      jdbcUserDetailsManager.setDeleteUserSql(
          "delete from `Users` where id = (select u.id from (select id from `Users` where Username = ?) as u)");
      jdbcUserDetailsManager
          .setCreateAuthoritySql("insert into `Authorities` (Username, Authority) values (?,?)");
      jdbcUserDetailsManager
          .setDeleteUserAuthoritiesSql(
              "delete from `Authorities` where id in (select a.id from (select id from `Authorities` where Username = ?) as a)");
      jdbcUserDetailsManager
          .setChangePasswordSql(
              "update `Users` set Password = ? where id = (select u.id from (select id from `Users` where Username = ?) as u)");

      return jdbcUserDetailsManager;
    }

    /**
     * 用户服务
     *
     * @return 用户服务bean
     */
    @Bean
    @ConditionalOnMissingBean(UserService.class)
    public UserService springSecurityUserService() {
      return new SpringSecurityUserService();
    }

  }

  /**
   * Spring Security配置
   */
  @Order(99)
  @Profile("auth")
  @Configuration
  @EnableWebSecurity
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  static class SpringSecurityConfigurer extends WebSecurityConfigurerAdapter {

    public static final String USER_ROLE = "user";

    /**
     * 配置规则
     *
     * @param http httpSecurity对象
     * @throws Exception 出现错误，抛出
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http.csrf().disable();
      http.headers().frameOptions().sameOrigin();
      http.authorizeRequests()
          .antMatchers(BY_PASS_URLS).permitAll()
          .antMatchers("/**").hasAnyRole(USER_ROLE);
      http.formLogin().loginPage("/signin").defaultSuccessUrl("/", true).permitAll()
          .failureUrl("/signin?#/error").and()
          .httpBasic();
      http.logout().logoutUrl("/user/logout").invalidateHttpSession(true).clearAuthentication(true)
          .logoutSuccessUrl("/signin?#/logout");
      http.exceptionHandling()
          .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/signin"));
    }

  }

  /**
   * Spring Ldap认证配置， spring.profiles.active = ldap
   */
  @Configuration
  @Profile("ldap")
  @EnableConfigurationProperties({LdapProperties.class, LdapExtendProperties.class})
  static class SpringSecurityLDAPAuthAutoConfiguration {

    private final LdapProperties properties;
    private final Environment environment;

    public SpringSecurityLDAPAuthAutoConfiguration(final LdapProperties properties,
        final Environment environment) {
      this.properties = properties;
      this.environment = environment;
    }

    /**
     * SSO心跳处理器
     *
     * @return SSO心跳处理器bean
     */
    @Bean
    @ConditionalOnMissingBean(SsoHeartbeatHandler.class)
    public SsoHeartbeatHandler defaultSsoHeartbeatHandler() {
      return new DefaultSsoHeartbeatHandler();
    }

    /**
     * 用户信息持有器
     *
     * @return 用户信息持有器bean
     */
    @Bean
    @ConditionalOnMissingBean(UserInfoHolder.class)
    public UserInfoHolder springSecurityUserInfoHolder() {
      return new SpringSecurityUserInfoHolder();
    }

    /**
     * 登出处理器
     *
     * @return 登出处理器对象bean
     */
    @Bean
    @ConditionalOnMissingBean(LogoutHandler.class)
    public LogoutHandler logoutHandler() {
      return new DefaultLogoutHandler();
    }

    /**
     * 用户服务
     *
     * @return 用户服务bean
     */
    @Bean
    @ConditionalOnMissingBean(UserService.class)
    public UserService springSecurityUserService() {
      return new LdapUserService();
    }

    /**
     * 构建LdapContextSource
     *
     * @return LdapContextSource Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ContextSource ldapContextSource() {
      LdapContextSource source = new LdapContextSource();
      source.setUserDn(this.properties.getUsername());
      source.setPassword(this.properties.getPassword());
      source.setAnonymousReadOnly(this.properties.isAnonymousReadOnly());
      source.setBase(this.properties.getBase());
      source.setUrls(this.properties.determineUrls(this.environment));
      source.setBaseEnvironmentProperties(
          Collections.unmodifiableMap(this.properties.getBaseEnvironment()));
      return source;
    }

    /**
     * 构建LADP模板
     *
     * @param contextSource LdapContextSource对象
     * @return LADP模板bean
     */
    @Bean
    @ConditionalOnMissingBean(LdapOperations.class)
    public LdapTemplate ldapTemplate(ContextSource contextSource) {
      LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
      ldapTemplate.setIgnorePartialResultException(true);
      return ldapTemplate;
    }
  }

  /**
   * Spring Security Ladp配置
   */
  @Order(99)
  @Profile("ldap")
  @Configuration
  @EnableWebSecurity
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  static class SpringSecurityLDAPConfigurer extends WebSecurityConfigurerAdapter {

    private final LdapProperties ldapProperties;
    private final LdapContextSource ldapContextSource;

    private final LdapExtendProperties ldapExtendProperties;

    public SpringSecurityLDAPConfigurer(final LdapProperties ldapProperties,
        final LdapContextSource ldapContextSource,
        final LdapExtendProperties ldapExtendProperties) {
      this.ldapProperties = ldapProperties;
      this.ldapContextSource = ldapContextSource;
      this.ldapExtendProperties = ldapExtendProperties;
    }

    /**
     * Ldap用户搜索过滤器
     *
     * @return Ldap用户搜索过滤器Bean
     */
    @Bean
    public FilterBasedLdapUserSearch userSearch() {
      if (ldapExtendProperties.getGroup() == null || StringUtils
          .isBlank(ldapExtendProperties.getGroup().getGroupSearch())) {
        FilterBasedLdapUserSearch filterBasedLdapUserSearch = new FilterBasedLdapUserSearch("",
            ldapProperties.getSearchFilter(), ldapContextSource);
        filterBasedLdapUserSearch.setSearchSubtree(true);
        return filterBasedLdapUserSearch;
      }

      FilterLdapByGroupUserSearch filterLdapByGroupUserSearch = new FilterLdapByGroupUserSearch(
          ldapProperties.getBase(), ldapProperties.getSearchFilter(),
          ldapExtendProperties.getGroup().getGroupBase(),
          ldapContextSource, ldapExtendProperties.getGroup().getGroupSearch(),
          ldapExtendProperties.getMapping().getRdnKey(),
          ldapExtendProperties.getGroup().getGroupMembership(),
          ldapExtendProperties.getMapping().getLoginId());
      filterLdapByGroupUserSearch.setSearchSubtree(true);
      return filterLdapByGroupUserSearch;
    }

    /**
     * Ladp认证供应器
     *
     * @return Ladp认证供应器bean
     */
    @Bean
    public LdapAuthenticationProvider ldapAuthProvider() {
      BindAuthenticator bindAuthenticator = new BindAuthenticator(ldapContextSource);
      bindAuthenticator.setUserSearch(userSearch());
      DefaultLdapAuthoritiesPopulator defaultAuthAutoConfiguration = new DefaultLdapAuthoritiesPopulator(
          ldapContextSource, null);
      defaultAuthAutoConfiguration.setIgnorePartialResultException(true);
      defaultAuthAutoConfiguration.setSearchSubtree(true);
      // Rewrite the logic of LdapAuthenticationProvider with ApolloLdapAuthenticationProvider,
      // use userId in LDAP system instead of userId input by user.
      return new ApolloLdapAuthenticationProvider(
          bindAuthenticator, defaultAuthAutoConfiguration, ldapExtendProperties);
    }

    /**
     * 配置规则
     *
     * @param http httpSecurity对象
     * @throws Exception 出现错误，抛出
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http.csrf().disable();
      http.headers().frameOptions().sameOrigin();
      http.authorizeRequests()
          .antMatchers(BY_PASS_URLS).permitAll()
          .antMatchers("/**").authenticated();
      http.formLogin().loginPage("/signin").defaultSuccessUrl("/", true).permitAll()
          .failureUrl("/signin?#/error").and()
          .httpBasic();
      http.logout().logoutUrl("/user/logout").invalidateHttpSession(true).clearAuthentication(true)
          .logoutSuccessUrl("/signin?#/logout");
      http.exceptionHandling()
          .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/signin"));
    }

    /**
     * 配置认证供应器
     *
     * @param auth 认证管理构建器
     * @throws Exception 出现错误，抛出
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.authenticationProvider(ldapAuthProvider());
    }
  }

  /**
   * 默认的认证配置
   */
  @Configuration
  @ConditionalOnMissingProfile({"ctrip", "auth", "ldap"})
  static class DefaultAuthAutoConfiguration {

    /**
     * SSO心跳处理器
     *
     * @return SSO心跳处理器bean
     */
    @Bean
    @ConditionalOnMissingBean(SsoHeartbeatHandler.class)
    public SsoHeartbeatHandler defaultSsoHeartbeatHandler() {
      return new DefaultSsoHeartbeatHandler();
    }

    /**
     * 用户信息持有器
     *
     * @return 用户信息持有器bean
     */
    @Bean
    @ConditionalOnMissingBean(UserInfoHolder.class)
    public DefaultUserInfoHolder defaultUserInfoHolder() {
      return new DefaultUserInfoHolder();
    }

    /**
     * 登出处理器
     *
     * @return 登出处理器对象bean
     */
    @Bean
    @ConditionalOnMissingBean(LogoutHandler.class)
    public DefaultLogoutHandler logoutHandler() {
      return new DefaultLogoutHandler();
    }

    /**
     * 用户服务
     *
     * @return 用户服务bean
     */
    @Bean
    @ConditionalOnMissingBean(UserService.class)
    public UserService defaultUserService() {
      return new DefaultUserService();
    }
  }

  /**
   * 默认的Web安全配置
   */
  @ConditionalOnMissingProfile({"auth", "ldap"})
  @Configuration
  @EnableWebSecurity
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  static class DefaultWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http.csrf().disable();
      http.headers().frameOptions().sameOrigin();
    }
  }
}
