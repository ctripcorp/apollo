package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Commit;
import com.ctrip.framework.apollo.biz.repository.CommitRepository;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 提交记录 Service层
 */
@Service
public class CommitService {

  private final CommitRepository commitRepository;

  public CommitService(final CommitRepository commitRepository) {
    this.commitRepository = commitRepository;
  }

  /**
   * 保存提交记录
   *
   * @param commit 提交记录实体
   * @return 提交记录信息
   */
   @Transactional(rollbackFor = Exception.class)
  public Commit save(Commit commit) {
    //protection
    commit.setId(0);
    return commitRepository.save(commit);
  }

  /**
   * 查询提交记录
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          分页对象
   * @return 提交记录列表信息
   */
  public List<Commit> find(String appId, String clusterName, String namespaceName, Pageable page) {
    return commitRepository.findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(appId,
        clusterName, namespaceName, page);
  }

  /**
   * 查询提交记录
   *
   * @param appId            应用id
   * @param clusterName      集群名称
   * @param namespaceName    名称空间名称
   * @param lastModifiedTime 最后修改时间
   * @param page             分页对象
   * @return 提交记录列表信息
   */
  public List<Commit> find(String appId, String clusterName, String namespaceName,
      Date lastModifiedTime, Pageable page) {
    return commitRepository
        .findByAppIdAndClusterNameAndNamespaceNameAndDataChangeLastModifiedTimeGreaterThanEqualOrderByIdDesc(
            appId, clusterName, namespaceName, lastModifiedTime, page);
  }

  /**
   * 批量删除
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param operator      操作者
   * @return 影响的行数
   */
   @Transactional(rollbackFor = Exception.class)
  public int batchDelete(String appId, String clusterName, String namespaceName, String operator) {
    return commitRepository.batchDelete(appId, clusterName, namespaceName, operator);
  }

}
