package com.ctrip.apollo.portal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.portal.entity.App;
import com.ctrip.apollo.portal.exception.NotFoundException;
import com.ctrip.apollo.portal.service.AppService;

@RestController
@RequestMapping("/apps")
public class AppController {

  @Autowired
  private AppService appService;

  @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json"})
  public App create(@RequestBody App app) {
    return appService.save(app);
  }

  @RequestMapping("/{appid}")
  public App detail(@PathVariable String appid) {
    App app = appService.detail(appid);
    if (app == null) {
      throw new NotFoundException();
    }
    return app;
  }

  @RequestMapping("")
  public List<App> list(Pageable pageable) {
    Page<App> page = appService.list(pageable);
    if (pageable.getPageNumber() > page.getTotalPages()) {
      throw new NotFoundException();
    }
    return page.getContent();
  }
}
