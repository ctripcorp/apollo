package com.ctrip.framework.apollo.common.entity;


import com.ctrip.framework.apollo.common.utils.InputValidator;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 应用名称空间表
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "AppNamespace")
@SQLDelete(sql = "Update AppNamespace set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class AppNamespace extends BaseEntity {

  /**
   * 名称空间名称，注意，需要全局唯一
   */
  @NotBlank(message = "AppNamespace Name cannot be blank")
  @Pattern(
      regexp = InputValidator.CLUSTER_NAMESPACE_VALIDATOR,
      message = "Invalid Namespace format: " + InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE
          + " & " + InputValidator.INVALID_NAMESPACE_NAMESPACE_MESSAGE
  )
  @Column(name = "Name", nullable = false)
  private String name;
  /**
   * 应用Id
   */
  @NotBlank(message = "AppId cannot be blank")
  @Column(name = "AppId", nullable = false)
  private String appId;
  /**
   * 名称空间的格式（后缀）类型
   */
  @Column(name = "Format", nullable = false)
  private String format;
  /**
   * 名称空间是否为公共
   */
  @Column(name = "IsPublic", columnDefinition = "Bit default '0'")
  private boolean isPublic = false;
  /**
   * 备注
   */
  @Column(name = "Comment")
  private String comment;

  /**
   * 将格式类型转化为枚举
   *
   * @return 配置的格式类型枚举
   */
  public ConfigFileFormat formatAsEnum() {
    return ConfigFileFormat.fromString(this.format);
  }
}
