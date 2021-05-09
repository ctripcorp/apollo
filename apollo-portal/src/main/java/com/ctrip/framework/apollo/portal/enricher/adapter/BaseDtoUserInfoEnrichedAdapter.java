package com.ctrip.framework.apollo.portal.enricher.adapter;

import com.ctrip.framework.apollo.common.dto.BaseDTO;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class BaseDtoUserInfoEnrichedAdapter implements UserInfoEnrichedAdapter {

  private final BaseDTO dto;

  public BaseDtoUserInfoEnrichedAdapter(BaseDTO dto) {
    this.dto = dto;
  }

  @Override
  public final String getFirstUserId() {
    return this.dto.getDataChangeCreatedBy();
  }

  @Override
  public final void setFirstUserDisplayName(String userDisplayName) {
    this.dto.setDataChangeCreatedByDisplayName(userDisplayName);
  }

  @Override
  public final String getSecondUserId() {
    return this.dto.getDataChangeLastModifiedBy();
  }

  @Override
  public final void setSecondUserDisplayName(String userDisplayName) {
    this.dto.setDataChangeLastModifiedByDisplayName(userDisplayName);
  }
}
