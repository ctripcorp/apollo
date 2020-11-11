package com.ctrip.framework.apollo.biz.auth;

import com.ctrip.framework.apollo.common.condition.ConditionalOnMissingProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Web权限配置(只有开启了权限才可以注入).
 */
@ConditionalOnMissingProfile("auth")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    // 关闭打开的csrf保护
    http.csrf().disable();
    // "X-Frame-Options: SAMEORIGIN" 允许iframes在同一个域:
    http.headers().frameOptions().sameOrigin();
  }


  /**
   * 全局配置.
   * <p>
   * 虽然下面的身份验证是无用的，但为了向后兼容性，我们可以不删除它们。因为如果我们删除了它们，老客户端(0.9.0之前)仍然发送身份验证信息，服务器将返回401，这将导致大问题。</p>
   * <p>
   * 一旦我们从阿波罗中移除弹簧安全装置，我们可能会移除以下内容。</p>
   *
   * @param auth 身份验证管理器生成器
   * @throws Exception 如果在添加内存中身份验证时发生错误，抛出
   */
  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication().withUser("user").password("").roles("USER").and()
        .withUser("apollo").password("").roles("USER", "ADMIN");
  }

}
