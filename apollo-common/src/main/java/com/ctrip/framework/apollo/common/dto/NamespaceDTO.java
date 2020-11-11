package com.ctrip.framework.apollo.common.dto;

import com.ctrip.framework.apollo.common.utils.InputValidator;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 名称空间 Dto
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class NamespaceDTO extends BaseDTO {

  /**
   * 主键id
   */
  private Long id;
  /**
   * AppId
   */
  private String appId;
  /**
   * 集群的名称
   */
  private String clusterName;
  /**
   * 命名空间的名称
   */
  @Pattern(
      regexp = InputValidator.CLUSTER_NAMESPACE_VALIDATOR,
      message = "Invalid Namespace format: " + InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE
  )
  private String namespaceName;
}