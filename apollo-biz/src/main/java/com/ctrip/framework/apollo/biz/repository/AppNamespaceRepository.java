package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 应用名称空间 Repository层
 */
public interface AppNamespaceRepository extends PagingAndSortingRepository<AppNamespace, Long> {

  /**
   * 通过应用id与名称空间查询应用名称空间
   *
   * @param appId         应用 id
   * @param namespaceName 名称空间名称
   * @return 应用名称空间对象
   */
  AppNamespace findByAppIdAndName(String appId, String namespaceName);

  /**
   * 通过应用id和名称空间列表查询应用名称空间列表
   *
   * @param appId          应用id
   * @param namespaceNames 名称空间名称列表
   * @return 应用名称空间列表
   */
  List<AppNamespace> findByAppIdAndNameIn(String appId, Set<String> namespaceNames);

  /**
   * 查询指定名称空间并且为公共的名称空间
   *
   * @param namespaceName 名称空间名称
   * @return 应用名称空间对象
   */
  AppNamespace findByNameAndIsPublicTrue(String namespaceName);

  /**
   * 通过名称空间列表且名称空间为公共的查询名称空间列表
   *
   * @param namespaceNames 名称空间名称列表
   * @return 应用名称空间列表
   */
  List<AppNamespace> findByNameInAndIsPublicTrue(Set<String> namespaceNames);

  /**
   * 查询指定应用id为是否公共的名称空间对象
   *
   * @param appId    应用id
   * @param isPublic 是否为公共的
   * @return 应用名称空间对象列表
   */
  List<AppNamespace> findByAppIdAndIsPublic(String appId, boolean isPublic);

  /**
   * 指定应用id的名称空间列表
   *
   * @param appId 应用id
   * @return 指定应用id的应用名称空间对象列表
   */
  List<AppNamespace> findByAppId(String appId);

  /**
   * 查询大于指定应用名称空间id的升序的500条名称空间对象信息
   *
   * @param id 应用名称空间id
   * @return 应用名称空间对象列表
   */
  List<AppNamespace> findFirst500ByIdGreaterThanOrderByIdAsc(long id);

  /**
   * 通过应用id删除应用名称空间信息
   *
   * @param appId    应用id
   * @param operator 操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("UPDATE AppNamespace SET IsDeleted=1,DataChange_LastModifiedBy = ?2 WHERE AppId=?1")
  int batchDeleteByAppId(String appId, String operator);

  /**
   * 通过应用id和名称空间名称删除应用名称空间信息
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param operator      应用id
   * @return 影响的行数
   */
  @Modifying
  @Query("UPDATE AppNamespace SET IsDeleted=1,DataChange_LastModifiedBy = ?3 WHERE AppId=?1 and Name = ?2")
  int delete(String appId, String namespaceName, String operator);
}
