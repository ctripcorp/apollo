package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.openapi.entity.ConsumerRole;
import com.ctrip.framework.apollo.openapi.repository.ConsumerRoleRepository;
import com.ctrip.framework.apollo.portal.PortalApplication;
import com.ctrip.framework.apollo.portal.entity.po.Favorite;
import com.ctrip.framework.apollo.portal.entity.po.Permission;
import com.ctrip.framework.apollo.portal.entity.po.RolePermission;
import com.ctrip.framework.apollo.portal.entity.po.UserRole;
import com.ctrip.framework.apollo.portal.repository.AppNamespaceRepository;
import com.ctrip.framework.apollo.portal.repository.AppRepository;
import com.ctrip.framework.apollo.portal.repository.FavoriteRepository;
import com.ctrip.framework.apollo.portal.repository.PermissionRepository;
import com.ctrip.framework.apollo.portal.repository.RolePermissionRepository;
import com.ctrip.framework.apollo.portal.repository.RoleRepository;
import com.ctrip.framework.apollo.portal.repository.UserRoleRepository;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {PortalApplication.class})
public class AppServiceTest {
  @Autowired
  private AppService appService;
  @Autowired
  private AppNamespaceRepository appNamespaceRepository;
  @Autowired
  private FavoriteRepository favoriteRepository;
  @Autowired
  private PermissionRepository permissionRepository;
  @Autowired
  private RolePermissionRepository rolePermissionRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private UserRoleRepository userRoleRepository;
  @Autowired
  private ConsumerRoleRepository consumerRoleRepository;
  @Autowired
  private AppRepository appRepository;

  private static final String APP_ID = String.valueOf(RandomUtils.nextInt(10000));



  @Before
  public void setup() {
    App app = App.builder().name("app").appId(APP_ID).ownerName("apollo").build();
    app = appService.createAppInLocal(app);

    Favorite favorite = new Favorite();
    favorite.setAppId(APP_ID);
    favoriteRepository.save(favorite);

  }

  @Test
  @Transactional
  public void deleteByAppId() {

    App app = appRepository.findByAppId(APP_ID);
    Assert.isTrue(Objects.nonNull(app));


    AppNamespace appNamespace = appNamespaceRepository.findByAppId(APP_ID);
    Assert.isTrue(Objects.nonNull(appNamespace));

    Favorite favorite = favoriteRepository.findByAppId(APP_ID);
    Assert.isTrue(Objects.nonNull(favorite));


    Iterable<Permission> permission = permissionRepository.findAll();
    Assert.isTrue(permission.iterator().hasNext());


    Iterable<RolePermission> rolePermission = rolePermissionRepository.findAll();
    Assert.isTrue(rolePermission.iterator().hasNext());


    Iterable<UserRole> userRole = userRoleRepository.findAll();
    Assert.isTrue(userRole.iterator().hasNext());


    appService.deleteByAppId(APP_ID);

    app = appRepository.findByAppId(APP_ID);
    Assert.isTrue(Objects.isNull(app));


    appNamespace = appNamespaceRepository.findByAppId(APP_ID);
    Assert.isTrue(Objects.isNull(appNamespace));

    favorite = favoriteRepository.findByAppId(APP_ID);
    Assert.isTrue(Objects.isNull(favorite));


    permission = permissionRepository.findAll();
    Assert.isTrue(!permission.iterator().hasNext());


    rolePermission = rolePermissionRepository.findAll();
    Assert.isTrue(!rolePermission.iterator().hasNext());


    userRole = userRoleRepository.findAll();
    Assert.isTrue(!userRole.iterator().hasNext());


  }
}
