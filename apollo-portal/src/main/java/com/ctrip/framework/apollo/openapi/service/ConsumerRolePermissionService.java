package com.ctrip.framework.apollo.openapi.service;

import com.ctrip.framework.apollo.openapi.entity.ConsumerRole;
import com.ctrip.framework.apollo.openapi.repository.ConsumerRoleRepository;
import com.ctrip.framework.apollo.portal.entity.po.Permission;
import com.ctrip.framework.apollo.portal.entity.po.RolePermission;
import com.ctrip.framework.apollo.portal.repository.PermissionRepository;
import com.ctrip.framework.apollo.portal.repository.RolePermissionRepository;
import com.google.common.collect.FluentIterable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ConsumerRolePermissionService {
  private final PermissionRepository permissionRepository;
  private final ConsumerRoleRepository consumerRoleRepository;
  private final RolePermissionRepository rolePermissionRepository;

  public ConsumerRolePermissionService(
      final PermissionRepository permissionRepository,
      final ConsumerRoleRepository consumerRoleRepository,
      final RolePermissionRepository rolePermissionRepository) {
    this.permissionRepository = permissionRepository;
    this.consumerRoleRepository = consumerRoleRepository;
    this.rolePermissionRepository = rolePermissionRepository;
  }

  /**
   * Check whether user has the permission
   */
  public boolean consumerHasPermission(long consumerId, String permissionType, String targetId) {
    Permission permission =
        permissionRepository.findTopByPermissionTypeAndTargetId(permissionType, targetId);
    if (permission == null) {
      return false;
    }

    List<ConsumerRole> consumerRoles = consumerRoleRepository.findByConsumerId(consumerId);
    if (CollectionUtils.isEmpty(consumerRoles)) {
      return false;
    }

    Set<Long> roleIds =
        FluentIterable.from(consumerRoles).transform(consumerRole -> consumerRole.getRoleId())
            .toSet();
    List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIdIn(roleIds);
    if (CollectionUtils.isEmpty(rolePermissions)) {
      return false;
    }

    for (RolePermission rolePermission : rolePermissions) {
      if (rolePermission.getPermissionId() == permission.getId()) {
        return true;
      }
    }

    return false;
  }
}
