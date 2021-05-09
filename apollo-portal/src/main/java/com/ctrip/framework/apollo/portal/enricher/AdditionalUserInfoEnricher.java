package com.ctrip.framework.apollo.portal.enricher;

import com.ctrip.framework.apollo.common.dto.BaseDTO;
import com.ctrip.framework.apollo.portal.enricher.adapter.UserInfoEnrichedAdapter;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public interface AdditionalUserInfoEnricher {

  /**
   * enrich an additional user info for the dto list
   *
   * @param adapter         enrich adapter
   * @param userInfoMap userInfo map
   */
  void enrichAdditionalUserInfo(UserInfoEnrichedAdapter adapter, Map<String, UserInfo> userInfoMap);
}
