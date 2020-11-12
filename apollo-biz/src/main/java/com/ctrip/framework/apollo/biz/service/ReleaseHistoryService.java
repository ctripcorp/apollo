package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.ReleaseHistory;
import com.ctrip.framework.apollo.biz.repository.ReleaseHistoryRepository;
import com.google.gson.Gson;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发布历史记录 Service层
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ReleaseHistoryService {

  private static final Gson GSON = new Gson();

  private final ReleaseHistoryRepository releaseHistoryRepository;
  private final AuditService auditService;

  public ReleaseHistoryService(
      final ReleaseHistoryRepository releaseHistoryRepository,
      final AuditService auditService) {
    this.releaseHistoryRepository = releaseHistoryRepository;
    this.auditService = auditService;
  }

  /**
   * 通过名称空间查找发布历史记录
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param pageable      分页对象
   * @return 发布历史记录分页信息
   */
  public Page<ReleaseHistory> findReleaseHistoriesByNamespace(String appId, String clusterName,
      String namespaceName, Pageable
      pageable) {
    return releaseHistoryRepository
        .findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(appId, clusterName,
            namespaceName, pageable);
  }

  /**
   * 根据发布id和操作查询发布历史信息
   *
   * @param releaseId 发布id
   * @param operation 发布操作
   * @param page      分页对象
   * @return 发布历史记录分页信息
   */
  public Page<ReleaseHistory> findByReleaseIdAndOperation(long releaseId, int operation,
      Pageable page) {
    return releaseHistoryRepository
        .findByReleaseIdAndOperationOrderByIdDesc(releaseId, operation, page);
  }

  /**
   * 查询指定之前的发布id和指定发布操作的发布历史信息
   *
   * @param previousReleaseId 之前的发布id
   * @param operation         发布操作
   * @param page              分页对象
   * @return 发布历史记录分页信息
   */
  public Page<ReleaseHistory> findByPreviousReleaseIdAndOperation(long previousReleaseId,
      int operation, Pageable page) {
    return releaseHistoryRepository.findByPreviousReleaseIdAndOperationOrderByIdDesc(
        previousReleaseId, operation, page);
  }

  /**
   * 通过发布id、发布类型集合以id降序查询发布历史分页信息
   *
   * @param releaseId  发布id
   * @param operations 发布操作集
   * @param page       分页对象
   * @return 历史发布分页信息
   */
  public Page<ReleaseHistory> findByReleaseIdAndOperationInOrderByIdDesc(long releaseId,
      Set<Integer> operations, Pageable page) {
    return releaseHistoryRepository.findByReleaseIdAndOperationInOrderByIdDesc(releaseId,
        operations, page);
  }

  /**
   * 创建发布历史
   *
   * @param appId             应用id
   * @param clusterName       集群名称
   * @param namespaceName     名称空间名称
   * @param branchName        分支名称
   * @param releaseId         发布id
   * @param previousReleaseId 上一次的发布id
   * @param operation         发布操作
   * @param operationContext  操作上下文
   * @param operator          操作者
   * @return 发布历史记录
   */
  @Transactional(rollbackFor = Exception.class)
  public ReleaseHistory createReleaseHistory(String appId, String clusterName, String
      namespaceName, String branchName, long releaseId, long previousReleaseId, int operation,
      Map<String, Object> operationContext, String operator) {
    ReleaseHistory releaseHistory = new ReleaseHistory();
    releaseHistory.setAppId(appId);
    releaseHistory.setClusterName(clusterName);
    releaseHistory.setNamespaceName(namespaceName);
    releaseHistory.setBranchName(branchName);
    releaseHistory.setReleaseId(releaseId);
    releaseHistory.setPreviousReleaseId(previousReleaseId);
    releaseHistory.setOperation(operation);
    //操作上下文默认为空，有值转换为JSON字符串
    if (operationContext == null) {
      releaseHistory.setOperationContext("{}");
    } else {
      releaseHistory.setOperationContext(GSON.toJson(operationContext));
    }
    releaseHistory.setDataChangeCreatedTime(new Date());
    releaseHistory.setDataChangeCreatedBy(operator);
    releaseHistory.setDataChangeLastModifiedBy(operator);

    // 保存并记录审记信息
    releaseHistoryRepository.save(releaseHistory);
    auditService.audit(ReleaseHistory.class.getSimpleName(), releaseHistory.getId(),
        Audit.OP.INSERT, releaseHistory.getDataChangeCreatedBy());

    return releaseHistory;
  }

  /**
   * 批量删除发布记录
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param operator      操作者
   * @return 影响的行数
   */
  @Transactional(rollbackFor = Exception.class)
  public int batchDelete(String appId, String clusterName, String namespaceName, String operator) {
    return releaseHistoryRepository.batchDelete(appId, clusterName, namespaceName, operator);
  }
}
