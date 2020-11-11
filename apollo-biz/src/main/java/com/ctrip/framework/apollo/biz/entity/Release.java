package com.ctrip.framework.apollo.biz.entity;

import com.ctrip.framework.apollo.common.entity.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 发布信息.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "Release")
@SQLDelete(sql = "Update Release set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Release extends BaseEntity {

  /**
   * 发布的Key
   */
  @Column(name = "ReleaseKey", nullable = false)
  private String releaseKey;
  /**
   * 发布名字
   */
  @Column(name = "Name", nullable = false)
  private String name;
  /**
   * AppID
   */
  @Column(name = "AppId", nullable = false)
  private String appId;
  /**
   * 集群名称
   */
  @Column(name = "ClusterName", nullable = false)
  private String clusterName;
  /**
   * 名称空间名称
   */
  @Column(name = "NamespaceName", nullable = false)
  private String namespaceName;
  /**
   * 发布的配置项
   */
  @Column(name = "Configurations", nullable = false)
  @Lob
  private String configurations;
  /**
   * 发布说明
   */
  @Column(name = "Comment", nullable = false)
  private String comment;
  /**
   * 是否废弃
   */
  @Column(name = "IsAbandoned", columnDefinition = "Bit default '0'")
  private boolean isAbandoned;
}
