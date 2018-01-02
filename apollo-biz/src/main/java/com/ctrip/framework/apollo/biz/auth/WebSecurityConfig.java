package com.ctrip.framework.apollo.biz.auth;

import com.ctrip.framework.apollo.common.condition.ConditionalOnMissingProfile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@ConditionalOnMissingProfile("auth")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.csrf().disable();
    http.headers().frameOptions().sameOrigin();
  }

  /**
   * Although the authentication below is useless, we may not remove them for backward compatibility.
   * Because if we remove them and the old clients(before 0.9.0) still send the authentication
   * information, the server will return 401, which should cause big problems.
   *
   * We may remove the following once we remove spring security from Apollo.
   */
  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication().withUser("user").password("").roles("USER").and()
        .withUser("apollo").password("").roles("USER", "ADMIN");
  }

}
