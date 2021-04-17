package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.BaseDTO;
import java.util.List;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public interface AdditionalUserInfoEnrichService {

  /**
   * enrich the additional user info for the dto list
   *
   * @param dtoList dto with operator id
   */
  void enrichAdditionalUserInfo(List<? extends BaseDTO> dtoList);
}
