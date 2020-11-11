package com.ctrip.framework.apollo.common.entity;

import com.ctrip.framework.apollo.common.utils.InputValidator;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 应用表
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "App")
@SQLDelete(sql = "Update App set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class App extends BaseEntity {

  public App(String appId, String appName, String ownerName, String orgId, String orgName) {
    this.appId = appId;
    this.name = appName;
    this.ownerName = ownerName;
    this.orgId = orgId;
    this.orgName = orgName;
  }

  /**
   * 应用名称
   */
  @NotBlank(message = "Name cannot be blank")
  @Column(name = "Name", nullable = false)
  private String name;
  /**
   * 应用Id
   */
  @NotBlank(message = "AppId cannot be blank")
  @Pattern(
      regexp = InputValidator.CLUSTER_NAMESPACE_VALIDATOR,
      message = InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE
  )
  @Column(name = "AppId", nullable = false)
  private String appId;
  /**
   * 部门Id
   */
  @Column(name = "OrgId", nullable = false)
  private String orgId;
  /**
   * 部门名字
   */
  @Column(name = "OrgName", nullable = false)
  private String orgName;
  /**
   * 所有者的名称
   */
  @NotBlank(message = "OwnerName cannot be blank")
  @Column(name = "OwnerName", nullable = false)
  private String ownerName;
  /**
   * 所有者的邮箱
   */
  @NotBlank(message = "OwnerEmail cannot be blank")
  @Column(name = "OwnerEmail", nullable = false)
  private String ownerEmail;
}