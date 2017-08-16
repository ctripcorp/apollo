package com.ctrip.framework.apollo.portal.spi.springsecurity;

import com.google.common.collect.Lists;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.configuration.SystemUserConfigurer;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.po.UserPO;
import com.ctrip.framework.apollo.portal.repository.UserRepository;
import com.ctrip.framework.apollo.portal.spi.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

/**
 * @author lepdou 2017-03-10
 */
public class SpringSecurityUserService implements UserService {

  private PasswordEncoder encoder = new BCryptPasswordEncoder();
  private List<GrantedAuthority> authorities;

  @Autowired
  private JdbcUserDetailsManager userDetailsManager;
  @Autowired
  private UserRepository userRepository;

  @PostConstruct
  public void init() {
    authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_" + SystemUserConfigurer.USER_ROLE));
  }

  public void createOrUpdate(User user) {
    String username = user.getUsername();

    User userDetails = new User(username, encoder.encode(user.getPassword()), authorities);

    if (userDetailsManager.userExists(username)) {
      userDetailsManager.updateUser(userDetails);
    } else {
      userDetailsManager.createUser(userDetails);
    }

  }

  @Override
  public List<UserInfo> searchUsers(String keyword, int offset, int limit) {
    if (StringUtils.isEmpty(keyword)) {
      return Collections.emptyList();
    }

    List<UserPO> userPOs = userRepository.findByUsernameLike("%" + keyword + "%");

    List<UserInfo> result = Lists.newArrayList();
    if (CollectionUtils.isEmpty(userPOs)) {
      return result;
    }

    result.addAll(userPOs.stream().map(UserPO::toUserInfo).collect(Collectors.toList()));

    return result;
  }

  @Override
  public UserInfo findByUserId(String userId) {
    UserPO userPO = userRepository.findByUsername(userId);
    return userPO == null ? null : userPO.toUserInfo();
  }

  @Override
  public List<UserInfo> findByUserIds(List<String> userIds) {
    return null;
  }


}
