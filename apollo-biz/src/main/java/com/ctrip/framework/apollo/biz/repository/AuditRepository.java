package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.Audit;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * 日志审计 Repository层
 */
public interface AuditRepository extends PagingAndSortingRepository<Audit, Long> {

  /**
   * 查询指定创建人的创建的日志审计
   *
   * @param owner 创建人名称
   * @return 指定创建人的创建的日志审计列表
   */
  @Query("SELECT a from Audit a WHERE a.dataChangeCreatedBy = :owner")
  List<Audit> findByOwner(@Param("owner") String owner);

  /**
   * 查询指定创建人指定操作类型指定操作表的日志审计列表
   *
   * @param owner  创建人
   * @param entity 操作表
   * @param op     操作类型
   * @return 指定创建人指定操作类型指定操作表的日志审计列表
   */
  @Query("SELECT a from Audit a WHERE a.dataChangeCreatedBy = :owner AND a.entityName =:entity AND a.opName = :op")
  List<Audit> findAudits(@Param("owner") String owner, @Param("entity") String entity,
      @Param("op") String op);
}
