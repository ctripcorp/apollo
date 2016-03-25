package com.ctrip.apollo.portal.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.portal.entity.App;
import com.ctrip.apollo.portal.repository.AppRepository;

@Service
public class AppService {

  @Autowired
  private AppRepository appRepository;

  public App detail(long appId) {
    return appRepository.findByAppId(appId);
  }

  public Page<App> list(Pageable pageable) {
    return appRepository.findAll(pageable);
  }

  public Iterable<App> list() {
    return appRepository.findAll();
  }

  public App save(App app) {
    app.setCreateTimestamp(new Date());
    return appRepository.save(app);
  }
}
