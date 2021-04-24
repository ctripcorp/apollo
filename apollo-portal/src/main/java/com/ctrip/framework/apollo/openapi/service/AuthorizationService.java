package com.ctrip.framework.apollo.openapi.service;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.openapi.dto.OpenAppDTO;
import com.ctrip.framework.apollo.openapi.entity.ConsumerRole;
import com.ctrip.framework.apollo.openapi.repository.ConsumerRoleRepository;
import com.ctrip.framework.apollo.openapi.util.OpenApiBeanUtils;
import com.ctrip.framework.apollo.portal.entity.po.Role;
import com.ctrip.framework.apollo.portal.repository.RoleRepository;
import com.ctrip.framework.apollo.portal.service.AppService;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wxq
 */
@Service
public class AuthorizationService {

  private final ConsumerService consumerService;

  private final AppService appService;

  @Autowired
  private ConsumerRoleRepository consumerRoleRepository;

  @Autowired
  private RoleRepository roleRepository;

  public AuthorizationService(
      ConsumerService consumerService,
      AppService appService) {
    this.consumerService = consumerService;
    this.appService = appService;
  }

  private Set<String> getAppIdsByRoleIds(List<Long> roleIds) {
    Iterable<Role> roleIterable = this.roleRepository.findAllById(roleIds);

    List<String> roleNames = new ArrayList<>();
    for (Role role : roleIterable) {
      if (!role.isDeleted()) {
        roleNames.add(role.getRoleName());
      }
    }

    Set<String> appIds = roleNames.stream().map(RoleUtils::extractAppIdFromRoleName)
        .collect(Collectors.toSet());

    return appIds;
  }

  public List<OpenAppDTO> getAuthorizedApps(String token) {
    long consumerId = this.consumerService.getConsumerIdByToken(token);
    List<ConsumerRole> consumerRoles = this.consumerRoleRepository.findByConsumerId(consumerId);
    List<Long> roleIds = consumerRoles.stream().map(ConsumerRole::getRoleId)
        .collect(Collectors.toList());

    Set<String> appIds = this.getAppIdsByRoleIds(roleIds);

    List<App> apps = this.appService.findByAppIds(appIds);
    List<OpenAppDTO> openAppDTOS = OpenApiBeanUtils.transformFromApps(apps);
    return openAppDTOS;
  }
}
