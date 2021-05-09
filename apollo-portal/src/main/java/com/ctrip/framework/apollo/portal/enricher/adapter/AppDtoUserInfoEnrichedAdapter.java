package com.ctrip.framework.apollo.portal.enricher.adapter;

import com.ctrip.framework.apollo.common.dto.AppDTO;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class AppDtoUserInfoEnrichedAdapter implements UserInfoEnrichedAdapter {

  private final AppDTO dto;

  public AppDtoUserInfoEnrichedAdapter(AppDTO dto) {
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

  @Override
  public final String getThirdUserId() {
    return this.dto.getOwnerName();
  }

  @Override
  public final void setThirdUserDisplayName(String userDisplayName) {
    this.dto.setOwnerDisplayName(userDisplayName);
  }
}
