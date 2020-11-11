package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.common.entity.App;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 应用 Repository
 */
public interface AppRepository extends PagingAndSortingRepository<App, Long> {

  /**
   * 通过应用id查询应用信息
   *
   * @param appId 应用id
   * @return 指定应用id的应用信息
   */
  App findByAppId(String appId);

  /**
   * 通过所有者名称查询应用信息分页列表
   *
   * @param ownerName 所有者名称
   * @param page      分页对象
   * @return 符合条件的应用信息列表
   */
  List<App> findByOwnerName(String ownerName, Pageable page);

  /**
   * 通过应用id列表查询应用列表信息
   *
   * @param appIds 应用id列表
   * @return 符合条件的应用信息列表
   */
  List<App> findByAppIdIn(Set<String> appIds);

  /**
   * 通过应用id列表查询应用信息列表
   *
   * @param appIds   应用id列表
   * @param pageable 分页对象
   * @return 符合条件的应用信息列表
   */
  List<App> findByAppIdIn(Set<String> appIds, Pageable pageable);

  /**
   * 通过应用id或者应用名称包含指定应用名称的应用信息分页列表
   *
   * @param appId    应用id
   * @param name     应用名称
   * @param pageable 分页对象
   * @return 符合条件的应用信息分页列表
   */
  Page<App> findByAppIdContainingOrNameContaining(String appId, String name, Pageable pageable);

  /**
   * 通过应用id删除应用信息
   *
   * @param appId    应用id
   * @param operator 操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("UPDATE App SET IsDeleted=1,DataChange_LastModifiedBy = ?2 WHERE AppId=?1")
  int deleteApp(String appId, String operator);
}
