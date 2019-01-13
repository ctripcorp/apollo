package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.repository.ReleaseMessageRepository;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ReleaseMessageService {
  private final ReleaseMessageRepository releaseMessageRepository;

  public ReleaseMessageService(final ReleaseMessageRepository releaseMessageRepository) {
    this.releaseMessageRepository = releaseMessageRepository;
  }

  public ReleaseMessage findLatestReleaseMessageForMessages(Collection<String> messages) {
    if (CollectionUtils.isEmpty(messages)) {
      return null;
    }
    return releaseMessageRepository.findTopByMessageInOrderByIdDesc(messages);
  }

  public List<ReleaseMessage> findLatestReleaseMessagesGroupByMessages(Collection<String> messages) {
    if (CollectionUtils.isEmpty(messages)) {
      return Collections.emptyList();
    }
    List<Object[]> result =
        releaseMessageRepository.findLatestReleaseMessagesGroupByMessages(messages);
    List<ReleaseMessage> releaseMessages = Lists.newArrayList();
    for (Object[] o : result) {
      try {
        ReleaseMessage releaseMessage = new ReleaseMessage((String) o[0]);
        releaseMessage.setId((Long) o[1]);
        releaseMessages.add(releaseMessage);
      } catch (Exception ex) {
        Tracer.logError("Parsing LatestReleaseMessagesGroupByMessages failed", ex);
      }
    }
    return releaseMessages;
  }
}
