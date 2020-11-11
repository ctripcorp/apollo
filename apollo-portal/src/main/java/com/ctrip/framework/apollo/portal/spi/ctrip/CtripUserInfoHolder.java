package com.ctrip.framework.apollo.portal.spi.ctrip;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import java.lang.reflect.Method;

/**
 * ctrip内部实现的获取用户信息
 */
public class CtripUserInfoHolder implements UserInfoHolder {

  /**
   * 反射的org.jasig.cas.client.util.AssertionHolder对象，这个对象可以获取用户名
   */
  private Object assertionHolder;
  /**
   * 反射的AssertionHolder.getAssertion()方法
   */
  private Method getAssertion;

  /**
   * 构建属性
   */
  public CtripUserInfoHolder() {
    Class clazz = null;
    try {
      clazz = Class.forName("org.jasig.cas.client.util.AssertionHolder");
      assertionHolder = clazz.newInstance();
      getAssertion = assertionHolder.getClass().getMethod("getAssertion");
    } catch (Exception e) {
      throw new RuntimeException("init AssertionHolder fail", e);
    }
  }

  @Override
  public UserInfo getUser() {
    try {
      // 通过org.jasig.cas.client.util.AssertionHolder来获取用户的登录名
      Object assertion = getAssertion.invoke(assertionHolder);
      Method getPrincipal = assertion.getClass().getMethod("getPrincipal");
      Object principal = getPrincipal.invoke(assertion);
      Method getName = principal.getClass().getMethod("getName");
      String name = (String) getName.invoke(principal);

      //设置用户名称
      UserInfo userInfo = new UserInfo();
      userInfo.setUserId(name);
      return userInfo;
    } catch (Exception e) {
      throw new RuntimeException("get user info from assertion holder error", e);
    }
  }

}
