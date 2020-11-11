package com.ctrip.framework.apollo.biz.entity;

import com.ctrip.framework.apollo.common.entity.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 访问密钥
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "AccessKey")
@SQLDelete(sql = "Update AccessKey set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class AccessKey extends BaseEntity {

  /**
   * AppId
   */
  @Column(name = "appId", nullable = false)
  private String appId;
  /**
   * 密钥
   */
  @Column(name = "Secret", nullable = false)
  private String secret;
  /**
   * 是否启用（1:启用 0:禁用）
   */
  @Column(name = "isEnabled", columnDefinition = "Bit default '0'")
  private boolean enabled;
}
