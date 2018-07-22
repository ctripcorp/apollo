package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.repository.GrayReleaseRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrayReleaseRuleService {

  @Autowired
  private GrayReleaseRuleRepository grayReleaseRuleRepository;

  @Transactional
  public void deleteApp(String appId, String operator) {
    if (grayReleaseRuleRepository.countByAppId(appId) > 0) {
      grayReleaseRuleRepository.batchDeleteByDeleteApp(appId, operator);
    }
  }
}
