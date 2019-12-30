package com.ctrip.framework.apollo.biz.entity;

import com.ctrip.framework.apollo.common.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "AccessKey")
@SQLDelete(sql = "Update AccessKey set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class AccessKey extends BaseEntity {

  @Column(name = "appId", nullable = false)
  private String appId;

  @Column(name = "Secret", nullable = false)
  private String secret;

  @Column(name = "isEnabled", columnDefinition = "Bit default '0'")
  private boolean enabled;

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return toStringHelper().add("appId", appId).add("secret", secret)
        .add("enabled", enabled).toString();
  }
}
