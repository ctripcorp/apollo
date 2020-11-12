package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.ReleaseHistory;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 发布历史  Repository
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ReleaseHistoryRepository extends PagingAndSortingRepository<ReleaseHistory, Long> {

  /**
   * 通过应用id、集群名称、名称空间名称以id降序查询发布历史分页信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param pageable      分页对象
   * @return 符合条件的发布历史分页信息
   */
  Page<ReleaseHistory> findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(String appId, String
      clusterName, String namespaceName, Pageable pageable);

  /**
   * 通过关联的ReleaseId、发布类型以id降序查询发布历史分页信息
   *
   * @param releaseId 关联的ReleaseId
   * @param operation 发布类型
   * @param pageable  分页对象
   * @return 符合条件的发布历史分页信息
   */
  Page<ReleaseHistory> findByReleaseIdAndOperationOrderByIdDesc(long releaseId, int operation,
      Pageable pageable);

  /**
   * 通过前一次发布的ReleaseId、发布类型以id降序查询发布历史分页信息
   *
   * @param previousReleaseId 前一次发布的ReleaseId
   * @param operation         发布类型
   * @param pageable          分页对象
   * @return 符合条件的发布历史分页信息
   */
  Page<ReleaseHistory> findByPreviousReleaseIdAndOperationOrderByIdDesc(long previousReleaseId,
      int operation, Pageable pageable);

  /**
   * 通过关联的ReleaseId、发布类型集合以id降序查询发布历史分页信息
   *
   * @param releaseId  关联的ReleaseId
   * @param operations 发布类型集合
   * @param pageable   分页对象
   * @return 符合条件的发布历史分页信息
   */
  Page<ReleaseHistory> findByReleaseIdAndOperationInOrderByIdDesc(long releaseId,
      Set<Integer> operations, Pageable pageable);

  /**
   * 通过应用id、集群名称、名称空间名称删除发布历史信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param operator      操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("update ReleaseHistory set isdeleted=1,DataChange_LastModifiedBy = ?4 where appId=?1 and clusterName=?2 and namespaceName = ?3")
  int batchDelete(String appId, String clusterName, String namespaceName, String operator);

}
