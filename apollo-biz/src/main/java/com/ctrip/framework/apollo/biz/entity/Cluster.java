package com.ctrip.framework.apollo.biz.entity;

import com.ctrip.framework.apollo.common.entity.BaseEntity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Entity
@Table(name = "Cluster")
@SQLDelete(sql = "Update Cluster set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Cluster extends BaseEntity implements Comparable<Cluster> {

  @Column(name = "Name", nullable = false)
  private String name;

  @Column(name = "AppId", nullable = false)
  private String appId;

  @Column(name = "ParentClusterId", nullable = false)
  private long parentClusterId;

  @Column(name = "AssociateClusterId", nullable = false)
  private long associateClusterId;
  
  public String getAppId() {
    return appId;
  }

  public String getName() {
    return name;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getParentClusterId() {
    return parentClusterId;
  }

  public void setParentClusterId(long parentClusterId) {
    this.parentClusterId = parentClusterId;
  }

  public long getAssociateClusterId() {
    return associateClusterId;
  }

  public void setAssociateClusterId(long associateClusterId) {
    this.associateClusterId = associateClusterId;
  }

  public String toString() {
    return toStringHelper().add("name", name).add("appId", appId)
        .add("parentClusterId", parentClusterId).toString();
  }

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
