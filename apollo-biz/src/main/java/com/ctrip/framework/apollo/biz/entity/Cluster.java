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
 * 集群.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "Cluster")
@SQLDelete(sql = "Update Cluster set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Cluster extends BaseEntity implements Comparable<Cluster> {

  /**
   * 集群名称
   */
  @Column(name = "Name", nullable = false)
  private String name;
  /**
   *应用Id
   */
  @Column(name = "AppId", nullable = false)
  private String appId;
  /**
   * 父集群的id
   */
  @Column(name = "ParentClusterId", nullable = false)
  private long parentClusterId;

  @Override
  public int compareTo(Cluster o) {
    if (o == null || getId() > o.getId()) {
      return 1;
    }
    if (getId() == o.getId()) {
      return 0;
    }
    return -1;
  }
}
