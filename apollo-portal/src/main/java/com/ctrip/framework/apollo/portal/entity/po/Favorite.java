package com.ctrip.framework.apollo.portal.entity.po;

import com.ctrip.framework.apollo.common.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "Favorite")
@SQLDelete(sql = "Update Favorite set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Favorite extends BaseEntity {

  @Column(name = "AppId", nullable = false)
  private String appId;

  @Column(name = "UserId", nullable = false)
  private String userId;

  @Column(name = "Position")
  private long position;

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public long getPosition() {
    return position;
  }

  public void setPosition(long position) {
    this.position = position;
  }
}
