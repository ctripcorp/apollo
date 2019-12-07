package com.ctrip.framework.apollo.portal.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrefixPathController {

  @Value("${prefixPath:}")
  private String prefixPath;

  @GetMapping("/prefix-path")
  public String getPrefixPath(){
    return prefixPath;
  }

}
