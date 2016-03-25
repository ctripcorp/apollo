package com.ctrip.apollo.portal.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.ctrip.apollo.portal.entity.Privilege;

public interface PrivilegeRepository extends PagingAndSortingRepository<Privilege, Long> {

  List<Privilege> findByAppId(long appId);

  List<Privilege> findByAppIdAndPrivilType(long appId, String privilType);

  Privilege findByAppIdAndNameAndPrivilType(long appId, String name, String privilType);
}
