package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.Favorite;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface FavoriteRepository extends PagingAndSortingRepository<Favorite, Long> {

  List<Favorite> findByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(String userId, Pageable page);

  List<Favorite> findByAppIdOrderByPositionAscDataChangeCreatedTimeAsc(String appId, Pageable page);

  Favorite findFirstByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(String userId);

  Favorite findByUserIdAndAppId(String userId, String appId);

  @Modifying
  @Query("UPDATE Favorite SET IsDeleted=1,AppId=?2,DataChange_LastModifiedBy = ?3 WHERE AppId=?1")
  Integer batchDeleteByDeleteApp(String oldAppId, String newAppId, String operator);

  Integer countByAppId(String appId);
}
