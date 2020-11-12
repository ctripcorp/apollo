package com.ctrip.framework.apollo.adminservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 主页
 */
@RestController
@RequestMapping(path = "/")
public class IndexController {

  /**
   * 主页
   *
   * @return 主页标识字符串
   */
  @GetMapping
  public String index() {
    return "apollo-adminservice";
  }
}
