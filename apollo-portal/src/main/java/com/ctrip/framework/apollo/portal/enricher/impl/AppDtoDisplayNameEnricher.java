package com.ctrip.framework.apollo.portal.enricher.impl;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.dto.BaseDTO;
import com.ctrip.framework.apollo.portal.enricher.AdditionalUserInfoEnricher;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class AppDtoDisplayNameEnricher implements AdditionalUserInfoEnricher {

  @Override
  public Set<String> extractOperatorId(List<? extends BaseDTO> dtoList) {
    Set<String> operatorIdSet = new HashSet<>();
    for (BaseDTO dto : dtoList) {
      if (!(dto instanceof AppDTO)) {
        continue;
      }
      AppDTO appDto = (AppDTO) dto;
      if (StringUtils.hasText(appDto.getOwnerName())) {
        operatorIdSet.add(appDto.getOwnerName());
      }
    }
    return operatorIdSet;
  }

  @Override
  public void enrichAdditionalUserInfo(BaseDTO dto, Map<String, UserInfo> userInfoMap) {
    if (!(dto instanceof AppDTO)) {
      return;
    }
    AppDTO appDto = (AppDTO) dto;
    UserInfo userInfo = userInfoMap.get(appDto.getOwnerName());
    if (userInfo != null && StringUtils.hasText(userInfo.getName())) {
      appDto.setOwnerDisplayName(userInfo.getName());
    }
  }
}
