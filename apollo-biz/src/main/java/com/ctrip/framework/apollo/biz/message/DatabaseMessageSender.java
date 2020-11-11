package com.ctrip.framework.apollo.biz.message;

import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.repository.ReleaseMessageRepository;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.google.common.collect.Queues;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据库消息发送器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
@Component
public class DatabaseMessageSender implements MessageSender {

  /**
   * 清理队列的最大长度
   */
  private static final int CLEAN_QUEUE_MAX_SIZE = 100;
  /**
   * 待清理队列
   */
  private BlockingQueue<Long> toClean = Queues.newLinkedBlockingQueue(CLEAN_QUEUE_MAX_SIZE);
  /**
   * 清理线程
   */
  private final ExecutorService cleanExecutorService;
  /**
   * 停止清理
   */
  private final AtomicBoolean cleanStopped;

  private final ReleaseMessageRepository releaseMessageRepository;

  public DatabaseMessageSender(final ReleaseMessageRepository releaseMessageRepository) {
    cleanExecutorService = Executors.newSingleThreadExecutor(
        ApolloThreadFactory.create("DatabaseMessageSender", true));
    cleanStopped = new AtomicBoolean(false);
    this.releaseMessageRepository = releaseMessageRepository;
  }

  /**
   * 发送消息
   *
   * @param message 消息
   * @param channel 通道
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void sendMessage(String message, String channel) {
    log.info("Sending message {} to channel {}", message, channel);
    if (!Objects.equals(channel, Topics.APOLLO_RELEASE_TOPIC)) {
      log.warn("Channel {} not supported by DatabaseMessageSender!", channel);
      return;
    }

    Tracer.logEvent("Apollo.AdminService.ReleaseMessage", message);
    Transaction transaction = Tracer.newTransaction("Apollo.AdminService", "sendMessage");
    try {
      // 保存发布消息
      ReleaseMessage newMessage = releaseMessageRepository.save(new ReleaseMessage(message));
      // 加入待清理队列
      toClean.offer(newMessage.getId());
      transaction.setStatus(Transaction.SUCCESS);
    } catch (RuntimeException ex) {
      log.error("Sending message to database failed", ex);
      transaction.setStatus(ex);
      throw ex;
    } finally {
      transaction.complete();
    }
  }

  /**
   * 初始化，不停的清理消息
   */
  @PostConstruct
  private void initialize() {
    cleanExecutorService.submit(() -> {
      // 只要没有停止并且线程没有中断，就不停的清理消息
      while (!cleanStopped.get() && !Thread.currentThread().isInterrupted()) {
        try {
          Long rm = toClean.poll(1, TimeUnit.SECONDS);
          if (rm != null) {
            cleanMessage(rm);
          } else {
            TimeUnit.SECONDS.sleep(5);
          }
        } catch (InterruptedException ex) {
          Tracer.logError(ex);
          Thread.currentThread().interrupt();
        }
      }
    });
  }

  /**
   * 消息清理
   *
   * @param id 消息id
   */
  private void cleanMessage(Long id) {
    // 请仔细检查发布消息是否回滚
    ReleaseMessage releaseMessage = releaseMessageRepository.findById(id).orElse(null);
    if (releaseMessage == null) {
      return;
    }

    // 每次清理最新发布的前100条消息
    boolean hasMore = true;
    while (hasMore && !Thread.currentThread().isInterrupted()) {
      List<ReleaseMessage> messages = releaseMessageRepository
          .findFirst100ByMessageAndIdLessThanOrderByIdAsc(
              releaseMessage.getMessage(), releaseMessage.getId());

      releaseMessageRepository.deleteAll(messages);
      // 如果数据没有超过100说明已经没有更多消息了
      hasMore = messages.size() == 100;

      messages.forEach(toRemove -> Tracer.logEvent(String.format("ReleaseMessage.Clean.%s",
          toRemove.getMessage()), String.valueOf(toRemove.getId())));
    }
  }

  /**
   * 停止清理.
   */
  void stopClean() {
    cleanStopped.set(true);
  }
}
