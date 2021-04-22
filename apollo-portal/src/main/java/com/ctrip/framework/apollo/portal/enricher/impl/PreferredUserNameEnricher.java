package com.ctrip.framework.apollo.portal.enricher.impl;

import com.ctrip.framework.apollo.common.dto.BaseDTO;
import com.ctrip.framework.apollo.portal.enricher.AdditionalUserInfoEnricher;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
@Component
public class PreferredUserNameEnricher implements AdditionalUserInfoEnricher {

  @Override
  public void enrichAdditionalUserInfo(BaseDTO dto,
      Map<String, UserInfo> userInfoMap) {
    if (StringUtils.hasText(dto.getDataChangeCreatedBy())) {
      UserInfo userInfo = userInfoMap.get(dto.getDataChangeCreatedBy());
      if (userInfo != null && StringUtils.hasText(userInfo.getName())) {
        dto.setDataChangeCreatedByPreferredUsername(userInfo.getName());
      }
    }
    if (StringUtils.hasText(dto.getDataChangeLastModifiedBy())) {
      UserInfo userInfo = userInfoMap.get(dto.getDataChangeLastModifiedBy());
      if (userInfo != null && StringUtils.hasText(userInfo.getName())) {
        dto.setDataChangeLastModifiedByPreferredUsername(userInfo.getName());
      }
    }
  }
}
