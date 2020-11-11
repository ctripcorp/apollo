package com.ctrip.framework.apollo.portal.entity.model;


import com.ctrip.framework.apollo.common.utils.InputValidator;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;

/**
 * 应用 Model
 */
@Data
public class AppModel {

  /**
   * 应用名
   */
  @NotBlank(message = "name cannot be blank")
  private String name;
  /**
   * 应用Id
   */
  @NotBlank(message = "appId cannot be blank")
  @Pattern(regexp = InputValidator.CLUSTER_NAMESPACE_VALIDATOR,
      message = "Invalid AppId format: " + InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE
  )
  private String appId;
  /**
   * 部门Id
   */
  @NotBlank(message = "orgId cannot be blank")
  private String orgId;
  /**
   * 部门名字
   */
  @NotBlank(message = "orgName cannot be blank")
  private String orgName;
  /**
   * 所有者的名称
   */
  @NotBlank(message = "ownerName cannot be blank")
  private String ownerName;
  /**
   * 管理员
   */
  private Set<String> admins;


}
