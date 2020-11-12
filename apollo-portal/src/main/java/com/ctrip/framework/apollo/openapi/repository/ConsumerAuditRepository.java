package com.ctrip.framework.apollo.openapi.repository;

import com.ctrip.framework.apollo.openapi.entity.ConsumerAudit;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 消息者审计 Repository
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConsumerAuditRepository extends PagingAndSortingRepository<ConsumerAudit, Long> {

}
