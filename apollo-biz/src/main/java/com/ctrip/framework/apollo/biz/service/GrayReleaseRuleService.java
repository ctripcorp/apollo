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
  public void deleteApp(String oldAppId, String newAppId, String operator) {
    if (grayReleaseRuleRepository.countByAppId(oldAppId) > 0) {
      grayReleaseRuleRepository.batchDeleteByDeleteApp(oldAppId, newAppId, operator);
    }
  }
}
