package com.ctrip.framework.apollo.openapi.repository;

import com.ctrip.framework.apollo.openapi.entity.ConsumerAudit;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConsumerAuditRepository extends PagingAndSortingRepository<ConsumerAudit, Long> {
}
