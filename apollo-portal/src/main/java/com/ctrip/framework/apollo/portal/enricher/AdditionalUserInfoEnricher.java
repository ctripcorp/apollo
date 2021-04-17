package com.ctrip.framework.apollo.portal.enricher;

import com.ctrip.framework.apollo.common.dto.BaseDTO;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import java.util.List;
import java.util.Map;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public interface AdditionalUserInfoEnricher {

  /**
   * enrich an additional user info for the dto list
   *
   * @param dtoList     dto with operator id
   * @param userInfoMap userInfo map
   */
  void enrichAdditionalUserInfo(List<? extends BaseDTO> dtoList, Map<String, UserInfo> userInfoMap);
}
