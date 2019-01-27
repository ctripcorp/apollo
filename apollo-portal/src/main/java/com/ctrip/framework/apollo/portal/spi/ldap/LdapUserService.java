package com.ctrip.framework.apollo.portal.spi.ldap;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.SearchScope;
import org.springframework.util.CollectionUtils;

/**
 * @author xm.lin xm.lin@anxincloud.com
 * @Description
 * @date 18-8-9 下午4:42
 */
public class LdapUserService implements UserService {

  @Value("${ldap.mapping.objectClass}")
  private String objectClassAttrName;
  @Value("${ldap.mapping.loginId}")
  private String loginIdAttrName;
  @Value("${ldap.mapping.userDisplayName}")
  private String userDisplayNameAttrName;
  @Value("${ldap.mapping.email}")
  private String emailAttrName;
  @Value("#{'${ldap.filter.memberOf:}'.split('\\|')}")
  private String[] memberOf;

  @Autowired
  private LdapTemplate ldapTemplate;

  private static final String MEMBER_OF_ATTR_NAME = "memberOf";

  private ContextMapper<UserInfo> ldapUserInfoMapper = (ctx) -> {
    DirContextAdapter contextAdapter = (DirContextAdapter) ctx;
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId(contextAdapter.getStringAttribute(loginIdAttrName));
    userInfo.setName(contextAdapter.getStringAttribute(userDisplayNameAttrName));
    userInfo.setEmail(contextAdapter.getStringAttribute(emailAttrName));
    return userInfo;
  };

  private ContainerCriteria ldapQueryCriteria() {
    ContainerCriteria criteria = query()
        .searchScope(SearchScope.SUBTREE)
        .where("objectClass").is(objectClassAttrName);
    if (memberOf.length > 0 && !StringUtils.isEmpty(memberOf[0])) {
      ContainerCriteria memberOfFilters = query().where(MEMBER_OF_ATTR_NAME).is(memberOf[0]);
      Arrays.stream(memberOf).skip(1)
          .forEach(filter -> memberOfFilters.or(MEMBER_OF_ATTR_NAME).is(filter));
      criteria.and(memberOfFilters);
    }
    return criteria;
  }


  @Override
  public List<UserInfo> searchUsers(String keyword, int offset, int limit) {
    ContainerCriteria criteria = ldapQueryCriteria();
    if (!Strings.isNullOrEmpty(keyword)) {
      criteria.and(query().where(loginIdAttrName).like(keyword + "*").or(userDisplayNameAttrName)
          .like(keyword + "*"));
    }
    return ldapTemplate.search(criteria, ldapUserInfoMapper);
  }

  @Override
  public UserInfo findByUserId(String userId) {
    return ldapTemplate
        .searchForObject(ldapQueryCriteria().and(loginIdAttrName).is(userId), ldapUserInfoMapper);
  }

  @Override
  public List<UserInfo> findByUserIds(List<String> userIds) {
    if (CollectionUtils.isEmpty(userIds)) {
      return null;
    } else {
      ContainerCriteria criteria = query().where(loginIdAttrName).is(userIds.get(0));
      userIds.stream().skip(1).forEach(userId -> criteria.or(loginIdAttrName).is(userId));
      return ldapTemplate.search(ldapQueryCriteria().and(criteria), ldapUserInfoMapper);
    }
  }

}
