package com.ctrip.framework.apollo.portal.spi.ldap;

import com.ctrip.framework.apollo.portal.entity.bo.LdapUserInfo;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author xm.lin xm.lin@anxincloud.com
 * @Description
 * @date 18-8-9 下午4:42
 */
public class LadpUserSerive implements UserService {

    @Autowired
    private LdapTemplate ldapTemplate;

    @Override
    public List<UserInfo> searchUsers(String keyword, int offset, int limit) {
        List<LdapUserInfo> all = null;
        if (Strings.isNullOrEmpty(keyword)) {
            all = ldapTemplate.findAll(LdapUserInfo.class);
        } else {
            ContainerCriteria criteria = LdapQueryBuilder
                    .query().searchScope(SearchScope.SUBTREE)
                    .where("cn").like(keyword + "*")
                    .or("sn").like(keyword + "*");
            all = ldapTemplate.find(criteria, LdapUserInfo.class);
        }
        List<UserInfo> userInfoList = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(all)) {
            all.stream().map(p -> {
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(p.getUsername());
                userInfo.setName(p.getRealName());
                userInfo.setEmail(p.getMail());
                return userInfo;
            }).forEach(userInfoList::add);
        }
        return userInfoList;
    }

    @Override
    public UserInfo findByUserId(String userId) {
        ContainerCriteria criteria = LdapQueryBuilder.query().where("cn").is(userId);
        LdapUserInfo ldapUser = ldapTemplate.findOne(criteria, LdapUserInfo.class);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(ldapUser.getUsername());
        userInfo.setName(ldapUser.getRealName());
        userInfo.setEmail(ldapUser.getMail());
        return userInfo;
    }

    @Override
    public List<UserInfo> findByUserIds(List<String> userIds) {
        return null;
    }
}
