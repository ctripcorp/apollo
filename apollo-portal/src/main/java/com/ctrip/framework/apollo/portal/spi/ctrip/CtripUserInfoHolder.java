package com.ctrip.framework.apollo.portal.spi.ctrip;

import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;

import java.lang.reflect.Method;

/**
 * ctrip内部实现的获取用户信息
 */
public class CtripUserInfoHolder implements UserInfoHolder {

  private Object assertionHolder;

  private Method getAssertion;


  public CtripUserInfoHolder() {
    Class<?> clazz = null;
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

      Object assertion = getAssertion.invoke(assertionHolder);
      Method getPrincipal = assertion.getClass().getMethod("getPrincipal");
      Object principal = getPrincipal.invoke(assertion);
      Method getName = principal.getClass().getMethod("getName");
      String name = (String) getName.invoke(principal);

      UserInfo userInfo = new UserInfo();
      userInfo.setUserId(name);

      return userInfo;
    } catch (Exception e) {
      throw new RuntimeException("get user info from assertion holder error", e);
    }
  }

}
