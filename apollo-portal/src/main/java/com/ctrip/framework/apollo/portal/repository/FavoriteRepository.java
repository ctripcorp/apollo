package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.Favorite;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 应用收藏  Repository
 */
public interface FavoriteRepository extends PagingAndSortingRepository<Favorite, Long> {

  /**
   * 通过用户id查询以收藏顺序升序和创建时间升序的应用收藏信息分页列表
   *
   * @param userId 用户id
   * @param page   分页对象
   * @return 符合条件的应用收藏信息分页列表
   */
  List<Favorite> findByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(String userId,
      Pageable page);

  /**
   * 通过应用id查询以收藏顺序升序和创建时间升序的应用收藏信息分页列表
   *
   * @param appId 应用id
   * @param page  分页对象
   * @return 符合条件的应用收藏信息分页列表
   */
  List<Favorite> findByAppIdOrderByPositionAscDataChangeCreatedTimeAsc(String appId, Pageable page);

  /**
   * 通过应用id询以收藏顺序升序和创建时间升序的第一条应用收藏信息
   *
   * @param userId 用户id
   * @return 符合条件的应用收藏信息
   */
  Favorite findFirstByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(String userId);

  /**
   * 通过用户id和应用id查询应用收藏信息
   *
   * @param userId 用户id
   * @param appId  应用id
   * @return 符合条件的应用收藏信息
   */
  Favorite findByUserIdAndAppId(String userId, String appId);

  /**
   * 通过应用id删除应用收藏信息
   *
   * @param appId    应用id
   * @param operator 操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("UPDATE Favorite SET IsDeleted=1,DataChange_LastModifiedBy = ?2 WHERE AppId=?1")
  int batchDeleteByAppId(String appId, String operator);
}
