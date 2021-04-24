package com.ctrip.framework.apollo.openapi.v1.controller;

import com.ctrip.framework.apollo.openapi.dto.OpenAppDTO;
import com.ctrip.framework.apollo.openapi.service.AuthorizationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Let open api know self's permission.
 *
 * @author wxq
 * @since 1.9.0
 */
@RestController("openapiAuthorizationController")
@RequestMapping("/openapi/v1")
public class AuthorizationController {

  @Autowired
  private AuthorizationService authorizationService;

  /**
   * @return which apps can be operated by open api
   */
  @GetMapping("/authorized/apps")
  public List<OpenAppDTO> getAuthorizedApps(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
    return this.authorizationService.getAuthorizedApps(token);
  }
}
