package com.ctrip.apollo.portal.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class App implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 7348554309210401557L;

  @Id
  private long appId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String owner;

  @Column
  private String ownerPhone;

  @Column
  private String ownerMail;

  @Column
  private Date createTimestamp;

  @Column
  private Date lastUpdatedTimestamp;


  public long getAppId() {
    return appId;
  }

  public void setAppId(long appId) {
    this.appId = appId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getOwnerPhone() {
    return ownerPhone;
  }

  public void setOwnerPhone(String ownerPhone) {
    this.ownerPhone = ownerPhone;
  }

  public String getOwnerMail() {
    return ownerMail;
  }

  public void setOwnerMail(String ownerMail) {
    this.ownerMail = ownerMail;
  }

  public Date getCreateTimestamp() {
    return createTimestamp;
  }

  public void setCreateTimestamp(Date createTimestamp) {
    this.createTimestamp = createTimestamp;
  }

  public Date getLastUpdatedTimestamp() {
    return lastUpdatedTimestamp;
  }

  public void setLastUpdatedTimestamp(Date lastUpdatedTimestamp) {
    this.lastUpdatedTimestamp = lastUpdatedTimestamp;
  }
}
