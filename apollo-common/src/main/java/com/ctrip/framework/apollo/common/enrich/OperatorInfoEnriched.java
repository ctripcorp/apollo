package com.ctrip.framework.apollo.common.enrich;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public interface OperatorInfoEnriched {

  /**
   * get operator id from the object
   *
   * @return operator id
   */
  String getOperatorId();

  /**
   * set the operator display name for the object
   *
   * @param operatorDisplayName operator display name
   */
  void setOperatorDisplayName(String operatorDisplayName);
}
