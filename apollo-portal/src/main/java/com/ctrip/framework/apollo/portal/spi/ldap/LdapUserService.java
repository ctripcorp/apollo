package com.ctrip.framework.apollo.portal.spi.ldap;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.ctrip.framework.apollo.portal.spi.configuration.LdapExtendProperties;
import com.ctrip.framework.apollo.portal.spi.configuration.LdapProperties;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.naming.directory.Attribute;
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
 * Ldap用户spi服务
 * <p>
 * 支持OpenLdap、ApacheDS、ActiveDirectory使用{@linkldaptemplate}作为底层实现
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
   * 指定LDAP树中应开始搜索的根DN。
   */
  @Value("${spring.ldap.base}")
  private String base;

  /**
   * ldap用户objectClass配置
   */
  @Value("${ldap.mapping.objectClass}")
  private String objectClassAttrName;

  /**
   * ldap用户惟一 id，用来作为登录的id
   */
  @Value("${ldap.mapping.loginId}")
  private String loginIdAttrName;

  /**
   * ldap用户名，用来作为显示名
   */
  @Value("${ldap.mapping.userDisplayName}")
  private String userDisplayNameAttrName;

  /**
   * ldap 邮箱属性
   */
  @Value("${ldap.mapping.email}")
  private String emailAttrName;

  /**
   * ldap的rdnKey
   */
  @Value("${ldap.mapping.rdnKey:}")
  private String rdnKey;

  /**
   * memberOf,需要快速的查询某一个用户是属于哪一个或多个组的（member of）。
   * <p>memberOf 正是提供了这样的一个功能：如果某个组中通过 member 属性新增了一个用户，OpenLDAP 便会自动在该用户上创建一个 memberOf属性，其值为该组的dn
   */
  @Value("#{'${ldap.filter.memberOf:}'.split('\\|')}")
  private String[] memberOf;

  /**
   * 组指定LDAP树中应开始搜索的根
   */
  @Value("${ldap.group.groupBase:}")
  private String groupBase;

  /**
   * 组搜索筛选器,例如：(&(cn=apollo-admins)(&(member=*)))
   */
  @Value("${ldap.group.groupSearch:}")
  private String groupSearch;

  /**
   * 组成员身份，例如member或memberUid
   */
  @Value("${ldap.group.groupMembership:}")
  private String groupMembershipAttrName;


  @Autowired
  private LdapTemplate ldapTemplate;
  /**
   * 成员属性名称
   */
  private static final String MEMBER_OF_ATTR_NAME = "memberOf";
  /**
   * 成员Uid属性名称
   */
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
    ContainerCriteria criteria = query().searchScope(SearchScope.SUBTREE)
        .where("objectClass").is(objectClassAttrName);
    if (memberOf.length > 0 && StringUtils.isNotBlank(memberOf[0])) {
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
   * @param member  ldap EntryDN
   * @param userIds 用户ID列表
   * @return 返回用户信息
   */
  private UserInfo lookupUser(String member, List<String> userIds) {
    return ldapTemplate.lookup(member, (AttributesMapper<UserInfo>) attributes -> {
      UserInfo tmp = new UserInfo();
      Attribute emailAttribute = attributes.get(emailAttrName);
      if (emailAttribute != null && emailAttribute.get() != null) {
        tmp.setEmail(emailAttribute.get().toString());
      }
      Attribute loginIdAttribute = attributes.get(loginIdAttrName);
      if (loginIdAttribute != null && loginIdAttribute.get() != null) {
        tmp.setUserId(loginIdAttribute.get().toString());
      }
      Attribute userDisplayNameAttribute = attributes.get(userDisplayNameAttrName);
      if (userDisplayNameAttribute != null && userDisplayNameAttribute.get() != null) {
        tmp.setName(userDisplayNameAttribute.get().toString());
      }

      if (userIds != null) {
        if (userIds.stream().anyMatch(c -> c.equals(tmp.getUserId()))) {
          return tmp;
        }
        return null;
      }
      return tmp;

    });
  }

  /**
   * 通过用户id查询用户信息
   *
   * @param userId 用户id
   * @return 指定用户id的用户信息
   */
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
   * @param groupBase   组指定LDAP树中应开始搜索的根
   * @param groupSearch 组搜索筛选器
   * @param keyword     用户查询的关键词
   * @param userIds     用户id列表
   * @return 用户信息列表
   */
  private List<UserInfo> searchUserInfoByGroup(String groupBase, String groupSearch,
      String keyword, List<String> userIds) {

    return ldapTemplate.searchForObject(groupBase, groupSearch, ctx -> {
          List<UserInfo> userInfos = new ArrayList<>();

          if (!MEMBER_UID_ATTR_NAME.equals(groupMembershipAttrName)) {
            String[] members = ((DirContextAdapter) ctx)
                .getStringAttributes(groupMembershipAttrName);
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
          }

          Set<String> memberUids = Sets.newHashSet(((DirContextAdapter) ctx)
              .getStringAttributes(groupMembershipAttrName));
          if (!CollectionUtils.isEmpty(userIds)) {
            memberUids = Sets.intersection(memberUids, Sets.newHashSet(userIds));
          }
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
    }
    ContainerCriteria criteria = ldapQueryCriteria();
    if (!Strings.isNullOrEmpty(keyword)) {
      criteria.and(query().where(loginIdAttrName).like(keyword + "*").or(userDisplayNameAttrName)
          .like(keyword + "*"));
    }
    users = ldapTemplate.search(criteria, ldapUserInfoMapper);
    return users;
  }

  @Override
  public UserInfo findByUserId(String userId) {
    if (StringUtils.isNotBlank(groupSearch)) {
      List<UserInfo> lists = searchUserInfoByGroup(groupBase, groupSearch, null,
          Collections.singletonList(userId));
      if (lists != null && !lists.isEmpty() && lists.get(0) != null) {
        return lists.get(0);
      }
      return null;
    }
    return ldapTemplate
        .searchForObject(ldapQueryCriteria().and(loginIdAttrName).is(userId), ldapUserInfoMapper);

  }

  @Override
  public List<UserInfo> findByUserIds(List<String> userIds) {
    if (CollectionUtils.isEmpty(userIds)) {
      return Collections.emptyList();
    }
    if (StringUtils.isNotBlank(groupSearch)) {
      List<UserInfo> userListByGroup = searchUserInfoByGroup(groupBase, groupSearch, null,
          userIds);
      return userListByGroup;
    }
    ContainerCriteria criteria = query().where(loginIdAttrName).is(userIds.get(0));
    userIds.stream().skip(1).forEach(userId -> criteria.or(loginIdAttrName).is(userId));
    return ldapTemplate.search(ldapQueryCriteria().and(criteria), ldapUserInfoMapper);
  }
}
