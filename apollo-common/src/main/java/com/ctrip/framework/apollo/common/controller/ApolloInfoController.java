package com.ctrip.framework.apollo.common.controller;

import com.ctrip.framework.apollo.Apollo;
import com.ctrip.framework.foundation.Foundation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Apollo信息 Controller
 */
@RestController
@RequestMapping(path = "/apollo")
public class ApolloInfoController {

  /**
   * 获取Apollo的应用信息
   *
   * @return Apollo的应用信息
   */
  @RequestMapping("app")
  public String getApp() {
    return Foundation.app().toString();
  }

  /**
   * 获取Apollo的网络信息
   *
   * @return Apollo的网络信息
   */
  @RequestMapping("net")
  public String getNet() {
    return Foundation.net().toString();
  }

  /**
   * 获取Apollo的服务器信息
   *
   * @return Apollo的服务器信息
   */
  @RequestMapping("server")
  public String getServer() {
    return Foundation.server().toString();
  }

  /**
   * 获取Apollo的系统版本
   *
   * @return Apollo的系统版本
   */
  @RequestMapping("version")
  public String getVersion() {
    return Apollo.VERSION;
  }
}
