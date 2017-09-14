package com.ctrip.framework.apollo.portal.spi.ldap;


import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;

public class ThreadLocalUserInfoHolder implements UserInfoHolder {


    private static final ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();


    public static void setUser(UserInfo userInfo){
        threadLocal.set(userInfo);
    }

    public static void clear(){
        threadLocal.remove();
    }

    public ThreadLocalUserInfoHolder() {
    }

    @Override
    public UserInfo getUser() {
        return ThreadLocalUserInfoHolder.threadLocal.get();
    }

}
