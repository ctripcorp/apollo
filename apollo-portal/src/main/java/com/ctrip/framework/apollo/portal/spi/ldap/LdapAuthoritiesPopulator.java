package com.ctrip.framework.apollo.portal.spi.ldap;

import com.ctrip.framework.apollo.portal.entity.po.UserPO;
import com.ctrip.framework.apollo.portal.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;


public class LdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {

  private UserRepository userRepository;

  public LdapAuthoritiesPopulator(ContextSource contextSource, UserRepository repository) {
    this(contextSource, "", repository);
  }

  public LdapAuthoritiesPopulator(ContextSource contextSource, String groupSearchBase, UserRepository repository) {
    super(contextSource, groupSearchBase);
    this.userRepository = repository;
  }

  protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username) {
    Set<GrantedAuthority> authorities = super.getAdditionalRoles(user, username);

    UserPO userPO = userRepository.findByUsername(username);

    // 根据用户信息从DB取得用户角色列表
    if (userPO != null) {
      if (authorities == null) {
        authorities = new HashSet<>();
      }
      authorities.add((GrantedAuthority) () -> "ROLE_user");
    }

    return authorities;
  }

}
