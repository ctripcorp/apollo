package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.Item;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 属性的配置项 Repository层
 */
public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {

  /**
   * 通过名称空间Id和配置项key查询属性的配置项信息
   *
   * @param namespaceId 名称空间Id
   * @param key         配置项key
   * @return 符合条件的配置项信息
   */
  Item findByNamespaceIdAndKey(Long namespaceId, String key);

  /**
   * 通过名称空间Id以行号升序查询属性的配置项列表
   *
   * @param namespaceId 名称空间Id
   * @return 符合条件的配置项列表
   */
  List<Item> findByNamespaceIdOrderByLineNumAsc(Long namespaceId);

  /**
   * 查询指定名称空间的所有配置项信息
   *
   * @param namespaceId 名称空间Id
   * @return 符合条件的配置项列表
   */
  List<Item> findByNamespaceId(Long namespaceId);

  /**
   * 通过名称空间Id查询最后修改时间大于指定日期的配置项列表
   *
   * @param namespaceId 名称空间Id
   * @param date        指定日期
   * @return 符合条件的配置项列表
   */
  List<Item> findByNamespaceIdAndDataChangeLastModifiedTimeGreaterThan(Long namespaceId, Date date);

  /**
   * 查询指定名称空间最新的配置项信息
   *
   * @param namespaceId 名称空间Id
   * @return 指定名称空间Id最后一项的配置项列表
   */
  Item findFirst1ByNamespaceIdOrderByLineNumDesc(Long namespaceId);

  /**
   * 通过名称空间Id删除配置项信息
   *
   * @param namespaceId 名称空间Id
   * @param operator    操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("update Item set isdeleted=1,DataChange_LastModifiedBy = ?2 where namespaceId = ?1")
  int deleteByNamespaceId(long namespaceId, String operator);

}
