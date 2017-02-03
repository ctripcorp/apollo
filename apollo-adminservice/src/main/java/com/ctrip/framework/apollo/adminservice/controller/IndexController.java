package com.ctrip.framework.apollo.adminservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/")
public class IndexController {

  @RequestMapping(path = "", method = RequestMethod.GET)
  public String index() {
    return "apollo-adminservice";
  }
}
