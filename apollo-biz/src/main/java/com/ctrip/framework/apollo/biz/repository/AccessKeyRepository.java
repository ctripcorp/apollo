package com.ctrip.framework.apollo.biz.repository;


import com.ctrip.framework.apollo.biz.entity.AccessKey;
import java.util.Date;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AccessKeyRepository extends PagingAndSortingRepository<AccessKey, Long> {

  long countByAppId(String appId);

  AccessKey findOneByAppIdAndId(String appId, long id);

  List<AccessKey> findByAppId(String appId);

  List<AccessKey> findFirst500ByDataChangeLastModifiedTimeGreaterThanOrderByDataChangeLastModifiedTimeAsc(Date date);

  List<AccessKey> findByDataChangeLastModifiedTime(Date date);
}
