package com.ctrip.framework.apollo.portal.enricher.impl;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.dto.BaseDTO;
import com.ctrip.framework.apollo.portal.enricher.AdditionalUserInfoEnricher;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ReleaseHistoryBoDisplayNameEnricher implements AdditionalUserInfoEnricher {

  @Override
  public Set<String> extractOperatorId(List<? extends BaseDTO> dtoList) {

    Set<String> operatorIdSet = new HashSet<>();
    for (BaseDTO dto : dtoList) {
      if (!(dto instanceof ReleaseHistoryBO)) {
        continue;
      }
      ReleaseHistoryBO releaseHistory = (ReleaseHistoryBO) dto;
      if (StringUtils.hasText(releaseHistory.getOperator())) {
        operatorIdSet.add(releaseHistory.getOperator());
      }
    }
    return operatorIdSet;
  }

  @Override
  public void enrichAdditionalUserInfo(BaseDTO dto, Map<String, UserInfo> userInfoMap) {

  }
}
