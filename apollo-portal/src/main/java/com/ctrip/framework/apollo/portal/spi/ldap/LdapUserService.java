package com.ctrip.framework.apollo.portal.spi.ldap;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.ctrip.framework.apollo.portal.spi.configuration.LdapExtendProperties;
import com.ctrip.framework.apollo.portal.spi.configuration.LdapProperties;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.CollectionUtils;

/**
 * Ldap user spi service
 *
 * Support OpenLdap,ApacheDS,ActiveDirectory use {@link LdapTemplate} as underlying implementation
 *
 * @author xm.lin xm.lin@anxincloud.com
 * @author idefav
 * @Description ldap user service
 * @date 18-8-9 下午4:42
 */
public class LdapUserService implements UserService {

  @Autowired
  private LdapProperties ldapProperties;

  @Autowired
  private LdapExtendProperties ldapExtendProperties;

  /**
   * ldap search base
   */
  @Value("${spring.ldap.base}")
  private String base;

  /**
   * user objectClass
   */
  @Value("${ldap.mapping.objectClass}")
  private String objectClassAttrName;

  /**
   * user LoginId
   */
  @Value("${ldap.mapping.loginId}")
  private String loginIdAttrName;

  /**
   * user displayName
   */
  @Value("${ldap.mapping.userDisplayName}")
  private String userDisplayNameAttrName;

  /**
   * email
   */
  @Value("${ldap.mapping.email}")
  private String emailAttrName;

  /**
   * rdn
   */
  @Value("${ldap.mapping.rdnKey:}")
  private String rdnKey;

  /**
   * memberOf
   */
  @Value("#{'${ldap.filter.memberOf:}'.split('\\|')}")
  private String[] memberOf;

  /**
   * group search base
   */
  @Value("${ldap.group.groupBase:}")
  private String groupBase;

  /**
   * group filter eg. (&(cn=apollo-admins)(&(member=*)))
   */
  @Value("${ldap.group.groupSearch:}")
  private String groupSearch;

  /**
   * group memberShip eg. member
   */
  @Value("${ldap.group.groupMembership:}")
  private String groupMembershipAttrName;


  @Autowired
  private LdapTemplate ldapTemplate;

  private static final String MEMBER_OF_ATTR_NAME = "memberOf";
  private static final String MEMBER_UID_ATTR_NAME = "memberUid";

  /**
   * 用户信息Mapper
   */
  private ContextMapper<UserInfo> ldapUserInfoMapper = (ctx) -> {
    DirContextAdapter contextAdapter = (DirContextAdapter) ctx;
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId(contextAdapter.getStringAttribute(loginIdAttrName));
    userInfo.setName(contextAdapter.getStringAttribute(userDisplayNameAttrName));
    userInfo.setEmail(contextAdapter.getStringAttribute(emailAttrName));
    return userInfo;
  };

  /**
   * 查询条件
   */
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

  /**
   * 根据entryDN查找用户信息
   *
   * @param member ldap EntryDN
   * @param userIds 用户ID列表
   */
  private UserInfo lookupUser(String member, List<String> userIds) {
    return ldapTemplate.lookup(member, (AttributesMapper<UserInfo>) attributes -> {
      UserInfo tmp = new UserInfo();
      tmp.setEmail(attributes.get(emailAttrName).get().toString());
      tmp.setUserId(attributes.get(loginIdAttrName).get().toString());
      tmp.setName(attributes.get(userDisplayNameAttrName).get().toString());
      if (userIds != null) {
        if (userIds.stream().anyMatch(c -> c.equals(tmp.getUserId()))) {
          return tmp;
        } else {
          return null;
        }
      } else {
        return tmp;
      }

    });
  }

  private UserInfo searchUserById(String userId) {
    return ldapTemplate.searchForObject(query().where(loginIdAttrName).is(userId),
        ctx -> {
          UserInfo userInfo = new UserInfo();
          DirContextAdapter contextAdapter = (DirContextAdapter) ctx;
          userInfo.setEmail(contextAdapter.getStringAttribute(emailAttrName));
          userInfo.setName(contextAdapter.getStringAttribute(userDisplayNameAttrName));
          userInfo.setUserId(contextAdapter.getStringAttribute(loginIdAttrName));
          return userInfo;
        });
  }

  /**
   * 按照group搜索用户
   *
   * @param groupBase group search base
   * @param groupSearch group filter
   * @param keyword user search keywords
   * @param userIds user id list
   */
  private List<UserInfo> searchUserInfoByGroup(String groupBase, String groupSearch,
      String keyword, List<String> userIds) {

    return ldapTemplate
        .searchForObject(groupBase, groupSearch, ctx -> {
          String[] members = ((DirContextAdapter) ctx).getStringAttributes(groupMembershipAttrName);

          if (!MEMBER_UID_ATTR_NAME.equals(groupMembershipAttrName)) {
            List<UserInfo> userInfos = new ArrayList<>();
            for (String item : members) {
              LdapName ldapName = LdapUtils.newLdapName(item);
              LdapName memberRdn = LdapUtils.removeFirst(ldapName, LdapUtils.newLdapName(base));
              if (keyword != null) {
                String rdnValue = LdapUtils.getValue(memberRdn, rdnKey).toString();
                if (rdnValue.toLowerCase().contains(keyword.toLowerCase())) {
                  UserInfo userInfo = lookupUser(memberRdn.toString(), userIds);
                  userInfos.add(userInfo);
                }
              } else {
                UserInfo userInfo = lookupUser(memberRdn.toString(), userIds);
                if (userInfo != null) {
                  userInfos.add(userInfo);
                }
              }

            }
            return userInfos;
          } else {
            List<UserInfo> userInfos = new ArrayList<>();
            String[] memberUids = ((DirContextAdapter) ctx)
                .getStringAttributes(groupMembershipAttrName);
            for (String memberUid : memberUids) {
              UserInfo userInfo = searchUserById(memberUid);
              if (userInfo != null) {
                if (keyword != null) {
                  if (userInfo.getUserId().toLowerCase().contains(keyword.toLowerCase())) {
                    userInfos.add(userInfo);
                  }
                } else {
                  userInfos.add(userInfo);
                }
              }

            }
            return userInfos;
          }


        });
  }

  @Override
  public List<UserInfo> searchUsers(String keyword, int offset, int limit) {
    List<UserInfo> users = new ArrayList<>();
    if (StringUtils.isNotBlank(groupSearch)) {
      List<UserInfo> userListByGroup = searchUserInfoByGroup(groupBase, groupSearch, keyword,
          null);
      users.addAll(userListByGroup);
      return users.stream().collect(collectingAndThen(toCollection(() -> new TreeSet<>((o1, o2) -> {
        if (o1.getUserId().equals(o2.getUserId())) {
          return 0;
        }
        return -1;
      })), ArrayList::new));
    } else {
      ContainerCriteria criteria = ldapQueryCriteria();
      if (!Strings.isNullOrEmpty(keyword)) {
        criteria.and(query().where(loginIdAttrName).like(keyword + "*").or(userDisplayNameAttrName)
            .like(keyword + "*"));
      }
      users = ldapTemplate.search(criteria, ldapUserInfoMapper);
      return users;
    }
  }

  @Override
  public UserInfo findByUserId(String userId) {
    if (StringUtils.isNotBlank(groupSearch)) {
      List<UserInfo> lists = searchUserInfoByGroup(groupBase, groupSearch, null,
          Collections.singletonList(userId));
      if (lists != null && !lists.isEmpty() && lists.get(0) != null
          && lists.get(0) != null) {
        return lists.get(0);
      }
      return null;
    } else {
      return ldapTemplate
          .searchForObject(ldapQueryCriteria().and(loginIdAttrName).is(userId), ldapUserInfoMapper);

    }
  }

  @Override
  public List<UserInfo> findByUserIds(List<String> userIds) {
    if (CollectionUtils.isEmpty(userIds)) {
      return new ArrayList<>();
    } else {
      List<UserInfo> userList = new ArrayList<>();
      if (StringUtils.isNotBlank(groupSearch)) {
        List<UserInfo> userListByGroup = searchUserInfoByGroup(groupBase, groupSearch, null,
            userIds);
        userList.addAll(userListByGroup);
        return userList;
      } else {
        ContainerCriteria criteria = ldapQueryCriteria()
            .and(query().where(loginIdAttrName).is(userIds.get(0)));
        userIds.stream().skip(1).forEach(userId -> criteria.or(loginIdAttrName).is(userId));
        return ldapTemplate.search(criteria, ldapUserInfoMapper);
      }
    }
  }

}
