package com.ctrip.framework.apollo.biz.repository;


import com.ctrip.framework.apollo.biz.entity.AccessKey;
import java.util.Date;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 访问密钥 Repository层
 */
public interface AccessKeyRepository extends PagingAndSortingRepository<AccessKey, Long> {

  /**
   * 统计应用id的访问密钥总数量
   *
   * @param appId 应用id
   * @return 指定应用id的访问密钥总数量
   */
  long countByAppId(String appId);

  /**
   * 通过应用id和主键id获取访问密钥
   *
   * @param appId 应用id
   * @param id    主键id
   * @return 指定应用id和主键id的访问密钥
   */
  AccessKey findOneByAppIdAndId(String appId, long id);

  /**
   * 通过应用id获取访问密钥列表
   *
   * @param appId 应用id
   * @return 访问密钥列表
   */
  List<AccessKey> findByAppId(String appId);

  /**
   * 查询最后修改时间大于指定日期的最近500条访问密钥
   * <p>查找最后修改时间大于指定日期并按最后修改时间升序的500条数据
   *
   * @param date 指定时间
   * @return 最后修改时间大于指定日期的最近500条访问密钥
   */
  List<AccessKey> findFirst500ByDataChangeLastModifiedTimeGreaterThanOrderByDataChangeLastModifiedTimeAsc(
      Date date);

  /**
   * 通过最后修改时间查询访问密钥列表
   *
   * @param date 最后修改时间
   * @return 符合条件的访问密钥列表
   */
  List<AccessKey> findByDataChangeLastModifiedTime(Date date);
}
