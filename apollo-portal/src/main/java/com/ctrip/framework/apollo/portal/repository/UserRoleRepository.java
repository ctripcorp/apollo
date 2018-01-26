package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.UserRole;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface UserRoleRepository extends PagingAndSortingRepository<UserRole, Long> {
	/**
	 * find user roles by userId
	 */
	List<UserRole> findByUserId(String userId);

	/**
	 * find user roles by roleId
	 */
	List<UserRole> findByRoleId(long roleId);

	/**
	 * find user roles by userIds and roleId
	 */
	List<UserRole> findByUserIdInAndRoleId(Collection<String> userId, long roleId);

	/**
	 * find not deleted user roles by userId
	 */
	List<UserRole> findAllByUserIdAndIsDeletedFalse(String userId);
}
