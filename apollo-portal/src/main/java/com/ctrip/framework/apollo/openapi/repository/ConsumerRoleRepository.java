package com.ctrip.framework.apollo.openapi.repository;

import com.ctrip.framework.apollo.openapi.entity.ConsumerRole;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 消息者角色  Repository.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConsumerRoleRepository extends PagingAndSortingRepository<ConsumerRole, Long> {

  /**
   * 通过用户id查找消费者角色列表.
   *
   * @param consumerId 消费者id
   * @return 符合条件的消费者角色列表
   */
  List<ConsumerRole> findByConsumerId(long consumerId);

  /**
   * 通过角色id查询消费者角色列表.
   *
   * @param roleId 角色id
   * @return 符合条件的消费者角色列表
   */
  List<ConsumerRole> findByRoleId(long roleId);

  /**
   * 通过消费者id和角色id查询消费者角色.
   *
   * @param consumerId 消费者id
   * @param roleId     角色id
   * @return 符合条件的消费者角色
   */
  ConsumerRole findByConsumerIdAndRoleId(long consumerId, long roleId);

  /**
   * 通过角色id列表批量删除消费者角色.
   *
   * @param roleIds  角色id列表
   * @param operator 操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("UPDATE ConsumerRole SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE RoleId in ?1")
  Integer batchDeleteByRoleIds(List<Long> roleIds, String operator);
}
