package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.UserPO;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author lepdou 2017-04-08
 */
public interface UserRepository extends PagingAndSortingRepository<UserPO, Long> {

  List<UserPO> findByUsernameLike(String username);

  UserPO findByUsername(String username);
}
