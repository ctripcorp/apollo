

package com.ctrip.framework.apollo.portal.spi.ldap;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import javax.naming.Name;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

/**
 * Ldap组用户搜索过滤器.
 *
 * @author wuzishu
 */
@Slf4j
public class FilterLdapByGroupUserSearch extends FilterBasedLdapUserSearch {

  /**
   * 成员UID属性名称
   */
  private static final String MEMBER_UID_ATTR_NAME = "memberUid";
  /**
   * 指定LDAP树中应开始搜索的根DN。
   */
  private String searchBase;
  /**
   * 组指定LDAP树中应开始搜索的根
   */
  private String groupBase;
  /**
   * 组搜索筛选器,例如：(&(cn=apollo-admins)(&(member=*)))
   */
  private String groupSearch;
  /**
   * ldap的rdnKey
   */
  private String rdnKey;
  /**
   * 组成员身份，例如member或memberUid
   */
  private String groupMembershipAttrName;
  /**
   * ldap用户惟一 id，用来作为登录的id
   */
  private String loginIdAttrName;

  /**
   * 搜索控件
   */
  private final SearchControls searchControls = new SearchControls();

  /**
   * Ldap路径上下文源基本类
   */
  private BaseLdapPathContextSource contextSource;

  /**
   * 构建Ldap组用户搜索过滤器
   *
   * @param searchBase              指定LDAP树中应开始搜索的根DN
   * @param searchFilter            搜索过滤器
   * @param groupBase               组指定LDAP树中应开始搜索的根
   * @param contextSource           Ldap路径上下文源基本类
   * @param groupSearch             组搜索筛选器
   * @param rdnKey                  ldap的rdnKey
   * @param groupMembershipAttrName 组成员身份
   * @param loginIdAttrName         登录id属性名称
   */
  public FilterLdapByGroupUserSearch(String searchBase, String searchFilter, String groupBase,
      BaseLdapPathContextSource contextSource, String groupSearch, String rdnKey,
      String groupMembershipAttrName, String loginIdAttrName) {
    super(searchBase, searchFilter, contextSource);
    this.searchBase = searchBase;
    this.groupBase = groupBase;
    this.groupSearch = groupSearch;
    this.contextSource = contextSource;
    this.rdnKey = rdnKey;
    this.groupMembershipAttrName = groupMembershipAttrName;
    this.loginIdAttrName = loginIdAttrName;
  }

  /**
   * 通过应用id搜索名称对象
   *
   * @param userId 用户id
   * @return 指定应用id搜索名称对象
   */
  private Name searchUserById(String userId) {
    SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(this.contextSource);
    template.setSearchControls(searchControls);
    return template.searchForObject(query().where(this.loginIdAttrName).is(userId),
        ctx -> ((DirContextAdapter) ctx).getDn());
  }


  @Override
  public DirContextOperations searchForUser(String username) {
    if (log.isDebugEnabled()) {
      log.debug("Searching for user '" + username + "', with user search " + this);
    }
    // Ldap模板
    SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(this.contextSource);
    // 搜索控件
    template.setSearchControls(searchControls);
    return template.searchForObject(groupBase, groupSearch, ctx -> {

      if (!MEMBER_UID_ATTR_NAME.equals(groupMembershipAttrName)) {
        String[] members = ((DirContextAdapter) ctx)
            .getStringAttributes(groupMembershipAttrName);
        for (String item : members) {
          LdapName memberDn = LdapUtils.newLdapName(item);
          LdapName memberRdn = LdapUtils.removeFirst(memberDn, LdapUtils.newLdapName(searchBase));
          String rdnValue = LdapUtils.getValue(memberRdn, rdnKey).toString();
          // 用户名称匹配直接返回
          if (rdnValue.equalsIgnoreCase(username)) {
            return new DirContextAdapter(memberRdn.toString());
          }
        }
        throw new UsernameNotFoundException("User " + username + " not found in directory.");
      }
      // 成员的UID数组列表
      String[] memberUids = ((DirContextAdapter) ctx).getStringAttributes(groupMembershipAttrName);
      for (String memberUid : memberUids) {
        // 成员id与用户名称匹配后直接返回目录上下文适配器
        if (memberUid.equalsIgnoreCase(username)) {
          Name name = searchUserById(memberUid);
          LdapName ldapName = LdapUtils.newLdapName(name);
          LdapName ldapRdn = LdapUtils
              .removeFirst(ldapName, LdapUtils.newLdapName(searchBase));
          return new DirContextAdapter(ldapRdn);
        }
      }
      throw new UsernameNotFoundException("User " + username + " not found in directory.");
    });
  }
}
