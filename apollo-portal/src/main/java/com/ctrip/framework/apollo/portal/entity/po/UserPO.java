package com.ctrip.framework.apollo.portal.entity.po;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 * 用户表
 *
 * @author lepdou 2017-04-08
 */
@Data
@Entity
@Table(name = "Users")
public class UserPO {

  /**
   * 自增Id
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "Id")
  private long id;
  /**
   * 用户名
   */
  @Column(name = "Username", nullable = false)
  private String username;
  /**
   * 密码
   */
  @Column(name = "Password", nullable = false)
  private String password;
  /**
   * 邮箱地址
   */
  @Column(name = "Email", nullable = false)
  private String email;
  /**
   * 是否有效
   */
  @Column(name = "Enabled", nullable = false)
  private int enabled;

  /**
   * 转换为用户信息
   *
   * @return 用户信息实体
   */
  public UserInfo toUserInfo() {
    UserInfo userInfo = new UserInfo();
    userInfo.setName(this.getUsername());
    userInfo.setUserId(this.getUsername());
    userInfo.setEmail(this.getEmail());
    return userInfo;
  }
}
