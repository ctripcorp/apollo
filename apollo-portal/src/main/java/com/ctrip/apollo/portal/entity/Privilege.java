package com.ctrip.apollo.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Privilege implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -430087307622435118L;

  @Id
  @GeneratedValue
  private long id;

  @Column
  private String name;

  @Column
  private String privilType;

  @Column
  private long appId;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPrivilType() {
    return privilType;
  }

  public void setPrivilType(String privilType) {
    this.privilType = privilType;
  }

  public long getAppId() {
    return appId;
  }

  public void setAppId(long appId) {
    this.appId = appId;
  }
}
