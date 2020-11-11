package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.GrayReleaseRule;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 灰度规则 Repository层
 */
public interface GrayReleaseRuleRepository extends
    PagingAndSortingRepository<GrayReleaseRule, Long> {

  /**
   * 通过应用id、集群名称、名称空间名称、分支名称查询灰度规则信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @return 符合条件的灰度规则信息
   */
  GrayReleaseRule findTopByAppIdAndClusterNameAndNamespaceNameAndBranchNameOrderByIdDesc(
      String appId, String clusterName,
      String namespaceName, String branchName);

  /**
   * 通过应用id、集群名称、名称空间名称查询灰度规则信息列表
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 符合条件的灰度规则信息列表
   */
  List<GrayReleaseRule> findByAppIdAndClusterNameAndNamespaceName(String appId,
      String clusterName, String namespaceName);

  /**
   * 查看大于指定id的按升序排列的500条灰度规则信息
   *
   * @param id 主键id
   * @return 符合条件的灰度规则信息列表
   */
  List<GrayReleaseRule> findFirst500ByIdGreaterThanOrderByIdAsc(Long id);

}
