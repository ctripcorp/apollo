package com.ctrip.framework.apollo.portal.entity.model;


import com.ctrip.framework.apollo.common.utils.InputValidator;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class AppModel {

  @NotBlank(message = "name cannot be blank")
  private String name;

  @NotBlank(message = "appId cannot be blank")
  @Pattern(
      regexp = InputValidator.CLUSTER_NAMESPACE_VALIDATOR,
      message = "Invalid AppId format: " + InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE
  )
  private String appId;

  @NotBlank(message = "orgId cannot be blank")
  private String orgId;

  @NotBlank(message = "orgName cannot be blank")
  private String orgName;

  @NotBlank(message = "ownerName cannot be blank")
  private String ownerName;

  private Set<String> admins;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getOrgName() {
    return orgName;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  public Set<String> getAdmins() {
    return admins;
  }

  public void setAdmins(Set<String> admins) {
    this.admins = admins;
  }
}
