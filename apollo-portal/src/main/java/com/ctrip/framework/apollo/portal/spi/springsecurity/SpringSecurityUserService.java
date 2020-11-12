package com.ctrip.framework.apollo.portal.spi.springsecurity;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.po.UserPO;
import com.ctrip.framework.apollo.portal.repository.UserRepository;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * SpringSecurity用户服务
 *
 * @author lepdou 2017-03-10
 */
public class SpringSecurityUserService implements UserService {

  /**
   * 密码编码器
   */
  private PasswordEncoder encoder = new BCryptPasswordEncoder();
  /**
   * 权限列表
   */
  private List<GrantedAuthority> authorities;

  @Autowired
  private JdbcUserDetailsManager userDetailsManager;
  @Autowired
  private UserRepository userRepository;

  @PostConstruct
  public void init() {
    authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_user"));
  }

  /**
   * 创建或更新用户信息
   *
   * @param user 用户
   */
  @Transactional(rollbackFor = Exception.class)
  public void createOrUpdate(UserPO user) {
    String username = user.getUsername();

    User userDetails = new User(username, encoder.encode(user.getPassword()), authorities);

    // 创建或更新
    if (userDetailsManager.userExists(username)) {
      userDetailsManager.updateUser(userDetails);
    } else {
      userDetailsManager.createUser(userDetails);
    }

    //更新用户邮箱信息
    UserPO managedUser = userRepository.findByUsername(username);
    managedUser.setEmail(user.getEmail());

    userRepository.save(managedUser);
  }

  @Override
  public List<UserInfo> searchUsers(String keyword, int offset, int limit) {
    List<UserPO> users;
    //  查询用户
    if (StringUtils.isNotBlank(keyword)) {
      users = userRepository.findFirst20ByEnabled(1);
    } else {
      users = userRepository.findByUsernameLikeAndEnabled("%" + keyword + "%", 1);
    }

    List<UserInfo> result = Lists.newArrayList();
    if (CollectionUtils.isEmpty(users)) {
      return result;
    }

    // 添加用户信息
    result.addAll(users.stream().map(UserPO::toUserInfo).collect(Collectors.toList()));
    return result;
  }

  @Override
  public UserInfo findByUserId(String userId) {
    // 用户信息
    UserPO userPO = userRepository.findByUsername(userId);
    return userPO == null ? null : userPO.toUserInfo();
  }

  @Override
  public List<UserInfo> findByUserIds(List<String> userIds) {
    // 用户信息列表
    List<UserPO> users = userRepository.findByUsernameIn(userIds);

    if (CollectionUtils.isEmpty(users)) {
      return Collections.emptyList();
    }

    // 添加用户信息
    List<UserInfo> result = Lists.newArrayList();
    result.addAll(users.stream().map(UserPO::toUserInfo).collect(Collectors.toList()));
    return result;
  }


}
