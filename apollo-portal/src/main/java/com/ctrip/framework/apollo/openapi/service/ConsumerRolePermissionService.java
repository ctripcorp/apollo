package com.ctrip.framework.apollo.openapi.service;

import com.ctrip.framework.apollo.openapi.entity.ConsumerRole;
import com.ctrip.framework.apollo.openapi.repository.ConsumerRoleRepository;
import com.ctrip.framework.apollo.portal.entity.po.Permission;
import com.ctrip.framework.apollo.portal.entity.po.RolePermission;
import com.ctrip.framework.apollo.portal.repository.PermissionRepository;
import com.ctrip.framework.apollo.portal.repository.RolePermissionRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 消费者角色权限校验
 *
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
   * 检查用户是否有权限
   */
  public boolean consumerHasPermission(long consumerId, String permissionType, String targetId) {
    // 获得 Permission 对象
    Permission permission =
        permissionRepository.findTopByPermissionTypeAndTargetId(permissionType, targetId);
    // 若 Permission 不存在，返回 false
    if (permission == null) {
      return false;
    }

    // 获得 ConsumerRole 数组
    List<ConsumerRole> consumerRoles = consumerRoleRepository.findByConsumerId(consumerId);
    // 若数组为空，返回 false
    if (CollectionUtils.isEmpty(consumerRoles)) {
      return false;
    }

    // 获得 RolePermission 数组
    Set<Long> roleIds =
        consumerRoles.stream().map(ConsumerRole::getRoleId).collect(Collectors.toSet());
    List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIdIn(roleIds);
    // 若数组为空，返回 false
    if (CollectionUtils.isEmpty(rolePermissions)) {
      return false;
    }

    // 判断是否有对应的 RolePermission 。若有，则返回 true 【有权限】c
    for (RolePermission rolePermission : rolePermissions) {
      if (rolePermission.getPermissionId() == permission.getId()) {
        return true;
      }
    }

    return false;
  }
}
