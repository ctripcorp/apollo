package com.ctrip.framework.apollo.portal.spi.defaultimpl;

import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.po.Permission;
import com.ctrip.framework.apollo.portal.entity.po.Role;
import com.ctrip.framework.apollo.portal.entity.po.RolePermission;
import com.ctrip.framework.apollo.portal.entity.po.UserRole;
import com.ctrip.framework.apollo.portal.repository.PermissionRepository;
import com.ctrip.framework.apollo.portal.repository.RolePermissionRepository;
import com.ctrip.framework.apollo.portal.repository.RoleRepository;
import com.ctrip.framework.apollo.portal.repository.UserRoleRepository;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;

import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by timothy on 2017/4/26.
 */
public class DefaultRolePermissionService implements RolePermissionService {
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private RolePermissionRepository rolePermissionRepository;
	@Autowired
	private UserRoleRepository userRoleRepository;
	@Autowired
	private PermissionRepository permissionRepository;
	@Autowired
	private PortalConfig portalConfig;


	/**
	 * Create role with permissions, note that role name should be unique
	 */
	@Transactional
	public Role createRoleWithPermissions(Role role, Set<Long> permissionIds) {
		Role current = findRoleByRoleName(role.getRoleName());
		Preconditions.checkState(current == null, "Role %s already exists!", role.getRoleName());

		Role createdRole = roleRepository.save(role);

		if (!CollectionUtils.isEmpty(permissionIds)) {
			Iterable<RolePermission> rolePermissions = FluentIterable.from(permissionIds).transform(
					permissionId -> {
						RolePermission rolePermission = new RolePermission();
						rolePermission.setRoleId(createdRole.getId());
						rolePermission.setPermissionId(permissionId);
						rolePermission.setDataChangeCreatedBy(createdRole.getDataChangeCreatedBy());
						rolePermission.setDataChangeLastModifiedBy(createdRole.getDataChangeLastModifiedBy());
						return rolePermission;
					});
			rolePermissionRepository.save(rolePermissions);
		}

		return createdRole;
	}

	/**
	 * Assign role to users
	 *
	 * @return the users assigned roles
	 */
	@Transactional
	public Set<String> assignRoleToUsers(String roleName, Set<String> userIds,
										 String operatorUserId) {
		Role role = findRoleByRoleName(roleName);
		Preconditions.checkState(role != null, "Role %s doesn't exist!", roleName);

		List<UserRole> existedUserRoles =
				userRoleRepository.findByUserIdInAndRoleId(userIds, role.getId());
		Set<String> existedUserIds =
				FluentIterable.from(existedUserRoles).transform(userRole -> userRole.getUserId()).toSet();

		Set<String> toAssignUserIds = Sets.difference(userIds, existedUserIds);

		Iterable<UserRole> toCreate = FluentIterable.from(toAssignUserIds).transform(userId -> {
			UserRole userRole = new UserRole();
			userRole.setRoleId(role.getId());
			userRole.setUserId(userId);
			userRole.setDataChangeCreatedBy(operatorUserId);
			userRole.setDataChangeLastModifiedBy(operatorUserId);
			return userRole;
		});

		userRoleRepository.save(toCreate);
		return toAssignUserIds;
	}

	/**
	 * Remove role from users
	 */
	@Transactional
	public void removeRoleFromUsers(String roleName, Set<String> userIds, String operatorUserId) {
		Role role = findRoleByRoleName(roleName);
		Preconditions.checkState(role != null, "Role %s doesn't exist!", roleName);

		List<UserRole> existedUserRoles =
				userRoleRepository.findByUserIdInAndRoleId(userIds, role.getId());

		for (UserRole userRole : existedUserRoles) {
			userRole.setDeleted(true);
			userRole.setDataChangeLastModifiedTime(new Date());
			userRole.setDataChangeLastModifiedBy(operatorUserId);
		}

		userRoleRepository.save(existedUserRoles);
	}

	/**
	 * Query users with role
	 */
	public Set<UserInfo> queryUsersWithRole(String roleName) {
		Role role = findRoleByRoleName(roleName);

		if (role == null) {
			return Collections.emptySet();
		}

		List<UserRole> userRoles = userRoleRepository.findByRoleId(role.getId());

		Set<UserInfo> users = FluentIterable.from(userRoles).transform(userRole -> {
			UserInfo userInfo = new UserInfo();
			userInfo.setUserId(userRole.getUserId());
			return userInfo;
		}).toSet();

		return users;
	}

	/**
	 * Find role by role name, note that roleName should be unique
	 */
	public Role findRoleByRoleName(String roleName) {
		return roleRepository.findTopByRoleName(roleName);
	}

	/**
	 * Check whether user has the permission
	 */
	public boolean userHasPermission(String userId, String permissionType, String targetId) {
		Permission permission =
				permissionRepository.findTopByPermissionTypeAndTargetId(permissionType, targetId);
		if (permission == null) {
			return false;
		}

		if (isSuperAdmin(userId)) {
			return true;
		}

		List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
		if (CollectionUtils.isEmpty(userRoles)) {
			return false;
		}

		Set<Long> roleIds =
				FluentIterable.from(userRoles).transform(userRole -> userRole.getRoleId()).toSet();
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

	public boolean isSuperAdmin(String userId) {
		return portalConfig.superAdmins().contains(userId);
	}

	/**
	 * Create permission, note that permissionType + targetId should be unique
	 */
	@Transactional
	public Permission createPermission(Permission permission) {
		String permissionType = permission.getPermissionType();
		String targetId = permission.getTargetId();
		Permission current =
				permissionRepository.findTopByPermissionTypeAndTargetId(permissionType, targetId);
		Preconditions.checkState(current == null,
				"Permission with permissionType %s targetId %s already exists!", permissionType, targetId);

		return permissionRepository.save(permission);
	}

	/**
	 * Create permissions, note that permissionType + targetId should be unique
	 */
	@Transactional
	public Set<Permission> createPermissions(Set<Permission> permissions) {
		Multimap<String, String> targetIdPermissionTypes = HashMultimap.create();
		for (Permission permission : permissions) {
			targetIdPermissionTypes.put(permission.getTargetId(), permission.getPermissionType());
		}

		for (String targetId : targetIdPermissionTypes.keySet()) {
			Collection<String> permissionTypes = targetIdPermissionTypes.get(targetId);
			List<Permission> current =
					permissionRepository.findByPermissionTypeInAndTargetId(permissionTypes, targetId);
			Preconditions.checkState(CollectionUtils.isEmpty(current),
					"Permission with permissionType %s targetId %s already exists!", permissionTypes,
					targetId);
		}

		Iterable<Permission> results = permissionRepository.save(permissions);
		return FluentIterable.from(results).toSet();
	}

	/**
	 * get appIds that userId has permissions
	 */
	@Transactional
	public Set<String> getPermissionMaterApps(String userId) {
		//get roleIds by UserId
		List<Long> roleIds = userRoleRepository.findAllByUserIdAndIsDeletedFalse(userId)
				.stream().map(UserRole::getRoleId).collect(Collectors.toList());
		//get appIds by roleIds
		Set<String> appIds = roleRepository.findAllByIdInAndRoleNameStartingWith(roleIds, RoleType.MASTER)
				.stream().map(item -> item.getRoleName().split("[+]")[1])
				.distinct()
				.collect(Collectors.toSet());
		return appIds;
	}

}
