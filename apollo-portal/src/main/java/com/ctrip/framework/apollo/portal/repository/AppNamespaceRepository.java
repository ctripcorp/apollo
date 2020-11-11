package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 用户名称空间 - Repository
 */
public interface AppNamespaceRepository extends PagingAndSortingRepository<AppNamespace, Long> {

  /**
   * 通过应用id和名称空间名称查询应用名称空间信息
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 符合条件的应用名称空间信息
   */
  AppNamespace findByAppIdAndName(String appId, String namespaceName);

  /**
   * 通过名称空间名称查询应用名称空间信息
   *
   * @param namespaceName 名称空间名称
   * @return 指定名称空间名称的应用名称空间信息
   */
  AppNamespace findByName(String namespaceName);

  /**
   * 通过名称空间名称和公共属性查询应用名称空间信息列表
   *
   * @param namespaceName 名称空间名称
   * @param isPublic      名称空间是否为公共
   * @return 符合条件的应用名称空间信息列表
   */
  List<AppNamespace> findByNameAndIsPublic(String namespaceName, boolean isPublic);

  /**
   * 查询名称空间为公共的应用名称空间信息列表
   *
   * @return 名称空间为公共的应用名称空间信息列表
   */
  List<AppNamespace> findByIsPublicTrue();

  /**
   * 通过应用id查询应用名称空间列表
   *
   * @param appId 应用id
   * @return 指定应用id的应用名称空间信息列表
   */
  List<AppNamespace> findByAppId(String appId);

  /**
   * 通过应用id删除应用名称空间信息
   *
   * @param appId    应用id
   * @param operator 操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("UPDATE AppNamespace SET IsDeleted=1,DataChange_LastModifiedBy=?2 WHERE AppId=?1")
  int batchDeleteByAppId(String appId, String operator);

  /**
   * 通过应用id和名称空间名称删除应用名称空间信息
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param operator      操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("UPDATE AppNamespace SET IsDeleted=1,DataChange_LastModifiedBy = ?3 WHERE AppId=?1 and Name = ?2")
  int delete(String appId, String namespaceName, String operator);
}
