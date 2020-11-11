package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.repository.ReleaseMessageRepository;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 发布消息  Service层
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ReleaseMessageService {

  private final ReleaseMessageRepository releaseMessageRepository;

  public ReleaseMessageService(final ReleaseMessageRepository releaseMessageRepository) {
    this.releaseMessageRepository = releaseMessageRepository;
  }

  /**
   * 查询最后发布消息信息
   *
   * @param messages 指定发布消息内容集合
   * @return 最后发布消息信息
   */
  public ReleaseMessage findLatestReleaseMessageForMessages(Collection<String> messages) {
    if (CollectionUtils.isEmpty(messages)) {
      return null;
    }
    return releaseMessageRepository.findTopByMessageInOrderByIdDesc(messages);
  }

  /**
   * 查询最新的发布消息列表
   *
   * @param messages 指定发布消息内容集合
   * @return 最新的发布消息列表
   */
  public List<ReleaseMessage> findLatestReleaseMessagesGroupByMessages(
      Collection<String> messages) {
    if (CollectionUtils.isEmpty(messages)) {
      return Collections.emptyList();
    }
    // 发布消息内容与最大的发布id数组列表
    List<Object[]> result =
        releaseMessageRepository.findLatestReleaseMessagesGroupByMessages(messages);

    // 组装为发布消息列表
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
