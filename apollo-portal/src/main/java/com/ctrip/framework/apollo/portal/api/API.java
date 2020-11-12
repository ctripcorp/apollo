package com.ctrip.framework.apollo.portal.api;


import com.ctrip.framework.apollo.portal.component.RetryableRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * API  抽象类
 */
public abstract class API {

  @Autowired
  protected RetryableRestTemplate restTemplate;

}
