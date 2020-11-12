package com.ctrip.framework.apollo.common.dto;

import com.ctrip.framework.apollo.common.utils.InputValidator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 集群Dto
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ClusterDTO extends BaseDTO {

  /**
   * 主键id
   */
  private Long id;
  /**
   * 集群的名称
   */
  @NotBlank(message = "cluster name cannot be blank")
  @Pattern(
      regexp = InputValidator.CLUSTER_NAMESPACE_VALIDATOR,
      message = "Invalid Cluster format: " + InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE
  )
  private String name;
  /**
   * AppId
   */
  @NotBlank(message = "appId cannot be blank")
  private String appId;
  /**
   * 父集群的id
   */
  private Long parentClusterId;


}
