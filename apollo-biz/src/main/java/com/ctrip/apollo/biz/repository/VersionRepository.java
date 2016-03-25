package com.ctrip.apollo.biz.repository;

import com.ctrip.apollo.biz.entity.Version;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface VersionRepository extends PagingAndSortingRepository<Version, Long> {
    Version findByAppIdAndName(long appId, String name);

    Version findById(long id);

    List<Version> findByAppId(long appId);
}
