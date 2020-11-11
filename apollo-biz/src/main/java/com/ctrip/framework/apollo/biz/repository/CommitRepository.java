package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.Commit;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 提交记录 Repository层
 */
public interface CommitRepository extends PagingAndSortingRepository<Commit, Long> {

  /**
   * 通过应用id、集群名称、名称空间名称，分页对象查询提交记录信息列表（降序）
   *
   * @param appId         应用id
   * @param clusterName   集群的名称
   * @param namespaceName 名称空间的名称
   * @param pageable      分页对象
   * @return 符合条件的提交记录信息列表
   */
  List<Commit> findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(String appId,
      String clusterName,
      String namespaceName, Pageable pageable);

  /**
   * 通过应用id、集群名称、名称空间名称，最后修改时间、分页对象查询提交记录信息
   *
   * @param appId                      应用id
   * @param clusterName                集群的名称
   * @param namespaceName              名称空间的名称
   * @param dataChangeLastModifiedTime 最后修改时间
   * @param pageable                   分页对象
   * @return 符合条件的提交记录信息列表
   */
  List<Commit> findByAppIdAndClusterNameAndNamespaceNameAndDataChangeLastModifiedTimeGreaterThanEqualOrderByIdDesc(
      String appId, String clusterName, String namespaceName, Date dataChangeLastModifiedTime,
      Pageable pageable);

  /**
   * 通过应用id、集群名称、名称空间名称删除提交记录信息
   *
   * @param appId         应用id
   * @param clusterName   集群的名称
   * @param namespaceName 名称空间的名称
   * @param operator      操作人
   * @return 影响的行数
   */
  @Modifying
  @Query("update Commit set isdeleted=1,DataChange_LastModifiedBy = ?4 where appId=?1 and clusterName=?2 and namespaceName = ?3")
  int batchDelete(String appId, String clusterName, String namespaceName, String operator);

}
