package com.ctrip.framework.apollo.common.enrich;

import com.ctrip.framework.apollo.common.dto.BaseDTO;
import com.ctrip.framework.apollo.common.enrich.OperatorInfoEnriched;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class CreatedByOperatorInfoEnriched implements OperatorInfoEnriched {

  private final BaseDTO dto;

  public CreatedByOperatorInfoEnriched(BaseDTO dto) {
    this.dto = dto;
  }

  @Override
  public String getOperatorId() {
    return this.dto.getDataChangeCreatedBy();
  }

  @Override
  public void setOperatorDisplayName(String operatorDisplayName) {
    this.dto.setDataChangeCreatedByDisplayName(operatorDisplayName);
  }
}
