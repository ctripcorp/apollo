package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.common.entity.App;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * 应用表 Repository
 */
public interface AppRepository extends PagingAndSortingRepository<App, Long> {

  /**
   * 通过应用名模糊匹配，找到应用列表
   *
   * @param name 应用名
   * @return 模糊匹配的应用列表
   */
  @Query("SELECT a from App a WHERE a.name LIKE %:name%")
  List<App> findByName(@Param("name") String name);

  /**
   * 查询指定的appid的应用
   *
   * @param appId 应用id
   * @return 指定的appid的应用
   */
  App findByAppId(String appId);
}
