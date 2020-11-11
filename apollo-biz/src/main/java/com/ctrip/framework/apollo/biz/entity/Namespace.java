package com.ctrip.framework.apollo.biz.entity;

import com.ctrip.framework.apollo.common.entity.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 名称空间
 *
 * @author smilesnake
 * @author Jason Song(song_s@ctrip.com)
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "Namespace")
@SQLDelete(sql = "Update Namespace set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Namespace extends BaseEntity {

  /**
   * AppId
   */
  @Column(name = "appId", nullable = false)
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

  public Namespace(String appId, String clusterName, String namespaceName) {
    this.appId = appId;
    this.clusterName = clusterName;
    this.namespaceName = namespaceName;
  }
}
