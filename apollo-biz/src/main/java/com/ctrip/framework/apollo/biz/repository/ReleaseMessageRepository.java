package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ReleaseMessageRepository extends PagingAndSortingRepository<ReleaseMessage, Long> {
  List<ReleaseMessage> findFirst500ByIdGreaterThanOrderByIdAsc(Long id);

  ReleaseMessage findTopByOrderByIdDesc();

  ReleaseMessage findTopByMessageInOrderByIdDesc(Collection<String> messages);

  List<ReleaseMessage> findFirst100ByMessageAndIdLessThanOrderByIdAsc(String message, Long id);

  @Query("select message, max(id) as id from ReleaseMessage where message in :messages group by message")
  List<Object[]> findLatestReleaseMessagesGroupByMessages(@Param("messages") Collection<String> messages);
}
