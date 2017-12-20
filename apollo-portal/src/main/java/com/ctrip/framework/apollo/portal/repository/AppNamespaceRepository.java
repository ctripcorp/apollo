package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.common.entity.AppNamespace;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface AppNamespaceRepository extends PagingAndSortingRepository<AppNamespace, Long> {

  AppNamespace findByAppIdAndName(String appId, String namespaceName);

  AppNamespace findByName(String namespaceName);

  AppNamespace findByNameAndIsPublic(String namespaceName, boolean isPublic);

  List<AppNamespace> findByIsPublicTrue();

  AppNamespace findByAppId(String appId);



}
