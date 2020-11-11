package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.NamespaceLock;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 名称空间编辑锁 Repository
 */
public interface NamespaceLockRepository extends PagingAndSortingRepository<NamespaceLock, Long> {

  /**
   * 查询指定集群NamespaceId的名称空间编辑锁的信息
   *
   * @param namespaceId 集群名称空间Id
   * @return 指定集群NamespaceId的名称空间编辑锁的信息
   */
  NamespaceLock findByNamespaceId(Long namespaceId);

  /**
   * 删除指定集群名称空间Id的编辑锁信息
   *
   * @param namespaceId 集群NamespaceId
   * @return 影响的行数
   */
  Long deleteByNamespaceId(Long namespaceId);
}
