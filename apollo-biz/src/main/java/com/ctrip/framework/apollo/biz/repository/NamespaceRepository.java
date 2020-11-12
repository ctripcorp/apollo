package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.Namespace;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 名称空间 Repository层
 */
public interface NamespaceRepository extends PagingAndSortingRepository<Namespace, Long> {

  /**
   * 通过应用id和集群名称以id升序查询名称空间列表信息
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @return 符合条件的名称空间列表信息
   */
  List<Namespace> findByAppIdAndClusterNameOrderByIdAsc(String appId, String clusterName);

  /**
   * 通过用id、集群名称、 名称空间名称查询名称空间信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 符合条件的名称空间信息
   */
  Namespace findByAppIdAndClusterNameAndNamespaceName(String appId, String clusterName,
      String namespaceName);

  /**
   * 通过应用id、集群名称删除名称空间
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @param operator    操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("update Namespace set isdeleted=1,DataChange_LastModifiedBy = ?3 where appId=?1 and clusterName=?2")
  int batchDelete(String appId, String clusterName, String operator);

  /**
   * 通过应用id、名称空间名称以id升序查询名称空间列表信息
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 符合条件的名称空间列表信息
   */
  List<Namespace> findByAppIdAndNamespaceNameOrderByIdAsc(String appId, String namespaceName);

  /**
   * 通过名称空间名称查询名称空间列表信息
   *
   * @param namespaceName 名称空间名称
   * @param page          分页对象
   * @return 符合条件的名称空间列表信息
   */
  List<Namespace> findByNamespaceName(String namespaceName, Pageable page);

  /**
   * 统计查询指定名称空间名称并且应用Id不为指定应用Id的数量
   *
   * @param namespaceName 名称空间名称
   * @param appId         应用id
   * @return 符合条件的名称空间信息数量
   */
  int countByNamespaceNameAndAppIdNot(String namespaceName, String appId);

}
