package com.ctrip.framework.apollo.portal.spi.ldap;


import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserService;

import java.util.Collections;
import java.util.List;

public class LdapUserService implements UserService {

    @Override
    public List<UserInfo> searchUsers(String keyword, int offset, int limit) {
        try {
            return LDAPUtil.findUsers(keyword, offset, limit);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public UserInfo findByUserId(String userId) {
        try {
            return LDAPUtil.findUser(userId);
        } catch (Exception e) {
            return new UserInfo();
        }
    }

    @Override
    public List<UserInfo> findByUserIds(List<String> userIds) {
        try {
            return LDAPUtil.findUsers(userIds);
        } catch (Exception e) {
            return Collections.EMPTY_LIST;
        }
    }
}
