package com.ctrip.apollo.biz.repository;

import com.ctrip.apollo.biz.entity.ConfigItem;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ConfigItemRepository extends PagingAndSortingRepository<ConfigItem, Long> {

    List<ConfigItem> findByClusterIdIsIn(List<Long> clusterIds);

}
