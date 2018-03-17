package com.ctrip.framework.apollo.demo.spring.common.bean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingUserBean {

  private String userName;

  private String gender;

  private int age;

  private List<String> mobiles;

  private Map<String, Object> favors;

  private BigDecimal money;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public List<String> getMobiles() {
    return mobiles;
  }

  public void setMobiles(List<String> mobiles) {
    this.mobiles = mobiles;
  }

  public Map<String, Object> getFavors() {
    return favors;
  }

  public void setFavors(Map<String, Object> favors) {
    this.favors = favors;
  }

  public BigDecimal getMoney() {
    return money;
  }

  public void setMoney(BigDecimal money) {
    this.money = money;
  }

}
