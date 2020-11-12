package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.Release;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * 发布信息 Repository层
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ReleaseRepository extends PagingAndSortingRepository<Release, Long> {

  /**
   * 通过应用id、集群名称、名称空间的名称按id逆序查询没有废弃的第一条发布信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间的名称
   * @return 符合条件的发布信息
   */
  Release findFirstByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(
      @Param("appId") String appId, @Param("clusterName") String clusterName,
      @Param("namespaceName") String namespaceName);

  /**
   * 通过主键id查询没有废弃的发布信息
   *
   * @param id 主键id
   * @return 符合条件的发布信息
   */
  Release findByIdAndIsAbandonedFalse(long id);

  /**
   * 通过应用id、集群名称、名称空间的名称按id降序查询发布信息列表
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间的名称
   * @param page          分页对象
   * @return 符合条件的发布信息分页列表
   */
  List<Release> findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(String appId,
      String clusterName, String namespaceName, Pageable page);

  /**
   * 通过应用id、集群名称、名称空间名称按id降序查询没有废弃的发布信息列表
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          分页对象
   * @return 符合条件的发布信息列表
   */
  List<Release> findByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(
      String appId, String clusterName, String namespaceName, Pageable page);

  /**
   * 通过通过应用id、集群名称、名称空间名称查询id在指定范围内没有废弃的发布信息列表
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间的名称
   * @param fromId        开始id
   * @param toId          结束id
   * @return 符合条件的发布信息列表
   */
  List<Release> findByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseAndIdBetweenOrderByIdDesc(
      String appId, String clusterName, String namespaceName, long fromId, long toId);

  /**
   * 通过指定发布key集合查询发布信息列表
   *
   * @param releaseKey 发布的Key
   * @return 符合条件的发布信息列表
   */
  List<Release> findByReleaseKeyIn(Set<String> releaseKey);

  /**
   * 通过主键id集合查询发布信息列表
   *
   * @param releaseIds 主键id列表
   * @return 合条件的发布信息列表
   */
  List<Release> findByIdIn(Set<Long> releaseIds);

  /**
   * 通过应用id、集群名称、名称空间的名称删除发布信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间的名称
   * @param operator      操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("update Release set isdeleted=1,DataChange_LastModifiedBy = ?4 where appId=?1 and clusterName=?2 and namespaceName = ?3")
  int batchDelete(String appId, String clusterName, String namespaceName, String operator);

  /**
   * 通过应用id、集群名称、名称空间名称通过id升序查询发布信息列表
   * <p>对于发布历史转换程序，需要在转换完成后删除它</p>
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间的名称
   * @return 合条件的发布信息列表
   */
  List<Release> findByAppIdAndClusterNameAndNamespaceNameOrderByIdAsc(String appId,
      String clusterName, String namespaceName);
}
