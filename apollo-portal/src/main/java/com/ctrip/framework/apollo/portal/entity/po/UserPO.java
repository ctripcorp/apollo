package com.ctrip.framework.apollo.portal.entity.po;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author lepdou 2017-04-08
 */
@Entity
@Table(name = "users")
public class UserPO {

  @Id
  @GeneratedValue
  @Column(name = "Id")
  private long id;
  @Column(name = "username", nullable = false)
  private String username;
  @Column(name = "password", nullable = false)
  private String password;
  @Column(name = "enabled", nullable = false)
  private int enabled;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getEnabled() {
    return enabled;
  }

  public void setEnabled(int enabled) {
    this.enabled = enabled;
  }

  public UserInfo toUserInfo() {
    UserInfo userInfo = new UserInfo();
    userInfo.setName(this.getUsername());
    userInfo.setUserId(this.getUsername());
    userInfo.setEmail("apollo@acme.com");
    return userInfo;
  }
}
