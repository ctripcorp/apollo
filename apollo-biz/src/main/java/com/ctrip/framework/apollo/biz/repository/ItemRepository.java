package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.Item;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {

  @Query("SELECT i FROM Item i WHERE i.namespaceId = ?1 AND i.key=?2 AND i.isDeleted = 0")
  Item findByNamespaceIdAndKey(Long namespaceId, String key);

  @Query("SELECT i FROM Item i WHERE i.namespaceId = ?1 AND i.isDeleted = 0 ORDER BY i.lineNum ASC")
  List<Item> findByNamespaceIdOrderByLineNumAsc(Long namespaceId);

  @Query("SELECT i FROM Item i WHERE i.namespaceId = ?1 AND i.isDeleted = 0")
  List<Item> findByNamespaceId(Long namespaceId);

  @Query("SELECT i FROM Item i WHERE i.namespaceId = ?1 AND i.isDeleted = 0 AND i.dataChangeLastModifiedTime > ?2")
  List<Item> findByNamespaceIdAndDataChangeLastModifiedTimeGreaterThan(Long namespaceId, Date date);

  @Query("SELECT i FROM Item i WHERE i.namespaceId = ?1 AND i.isDeleted = 0 ORDER BY i.lineNum DESC")
  List<Item> findFirst1ByNamespaceIdOrderByLineNumDesc(Long namespaceId, Pageable page);

  @Modifying
  @Query("update Item set isdeleted=1,DataChange_LastModifiedBy = ?2 where namespaceId = ?1")
  int deleteByNamespaceId(long namespaceId, String operator);

  @Query("SELECT i FROM Item i WHERE i.namespaceId = ?1 AND i.key = ?2 ORDER BY i.dataChangeLastModifiedTime DESC")
  List<Item> findFirst1ByKeyOrderByDataChangeLastModifiedTime(Long namespaceId, String key, Pageable page);

}
