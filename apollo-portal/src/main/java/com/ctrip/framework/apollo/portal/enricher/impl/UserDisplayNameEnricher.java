package com.ctrip.framework.apollo.portal.enricher.impl;

import com.ctrip.framework.apollo.common.dto.BaseDTO;
import com.ctrip.framework.apollo.portal.enricher.AdditionalUserInfoEnricher;
import com.ctrip.framework.apollo.portal.enricher.adapter.UserInfoEnrichedAdapter;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
@Component
public class UserDisplayNameEnricher implements AdditionalUserInfoEnricher {

  @Override
  public void enrichAdditionalUserInfo(UserInfoEnrichedAdapter adapter,
      Map<String, UserInfo> userInfoMap) {
    if (StringUtils.hasText(adapter.getFirstUserId())) {
      UserInfo userInfo = userInfoMap.get(adapter.getFirstUserId());
      if (userInfo != null && StringUtils.hasText(userInfo.getName())) {
        adapter.setFirstUserDisplayName(userInfo.getName());
      }
    }
    if (StringUtils.hasText(adapter.getSecondUserId())) {
      UserInfo userInfo = userInfoMap.get(adapter.getSecondUserId());
      if (userInfo != null && StringUtils.hasText(userInfo.getName())) {
        adapter.setSecondUserDisplayName(userInfo.getName());
      }
    }
    if (StringUtils.hasText(adapter.getThirdUserId())) {
      UserInfo userInfo = userInfoMap.get(adapter.getThirdUserId());
      if (userInfo != null && StringUtils.hasText(userInfo.getName())) {
        adapter.setThirdUserDisplayName(userInfo.getName());
      }
    }
  }
}
