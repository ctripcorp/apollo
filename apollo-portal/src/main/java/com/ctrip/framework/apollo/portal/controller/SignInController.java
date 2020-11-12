package com.ctrip.framework.apollo.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 登录
 *
 * @author lepdou 2017-08-30
 */
@Controller
public class SignInController {

  /**
   * 登录页面
   *
   * @param error  失败 Url
   * @param logout 登出Url
   * @return 登录页面地址
   */
  @GetMapping("/signin")
  public String login(@RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "logout", required = false) String logout) {
    return "login.html";
  }

}
