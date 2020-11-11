package com.ctrip.framework.apollo.biz.message;

import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.repository.ReleaseMessageRepository;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * 发布消息扫描器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public class ReleaseMessageScanner implements InitializingBean {

  @Autowired
  private BizConfig bizConfig;
  @Autowired
  private ReleaseMessageRepository releaseMessageRepository;
  /**
   * 发布消息扫描间隔毫秒值
   */
  private int databaseScanInterval;
  /**
   * 发布消息监听列表
   */
  private List<ReleaseMessageListener> listeners;
  /**
   * 任务执行器
   */
  private ScheduledExecutorService executorService;
  /**
   * 扫描的最大id
   */
  private long maxIdScanned;

  public ReleaseMessageScanner() {
    listeners = Lists.newCopyOnWriteArrayList();
    executorService = Executors.newScheduledThreadPool(1, ApolloThreadFactory
        .create("ReleaseMessageScanner", true));
  }

  @Override
  public void afterPropertiesSet() {
    // 初始化变量
    databaseScanInterval = bizConfig.releaseMessageScanIntervalInMilli();
    maxIdScanned = loadLargestMessageId();
    executorService.scheduleWithFixedDelay(() -> {
      Transaction transaction = Tracer
          .newTransaction("Apollo.ReleaseMessageScanner", "scanMessage");
      try {
        scanMessages();
        transaction.setStatus(Transaction.SUCCESS);
      } catch (Throwable ex) {
        transaction.setStatus(ex);
        log.error("Scan and send message failed", ex);
      } finally {
        transaction.complete();
      }
    }, databaseScanInterval, databaseScanInterval, TimeUnit.MILLISECONDS);

  }

  /**
   * 添加消息监听器
   *
   * @param listener 消息监听器
   */
  public void addMessageListener(ReleaseMessageListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /**
   * 扫描消息，继续扫描直到没有消息为止
   */
  private void scanMessages() {
    boolean hasMoreMessages = true;
    while (hasMoreMessages && !Thread.currentThread().isInterrupted()) {
      hasMoreMessages = scanAndSendMessages();
    }
  }

  /**
   * 扫描消息并发布
   *
   * @return 有更多消息, true, 否则，false
   */
  private boolean scanAndSendMessages() {
    //current batch is 500
    // 批处理 500 条,根据 maxIdScanned 找到比这个 id 大的 500 条数据,
    List<ReleaseMessage> releaseMessages =
        releaseMessageRepository.findFirst500ByIdGreaterThanOrderByIdAsc(maxIdScanned);
    if (CollectionUtils.isEmpty(releaseMessages)) {
      return false;
    }

    // 开始通知 handleMessage 监听器
    fireMessageScanned(releaseMessages);
    // 消息数量
    int messageScanned = releaseMessages.size();
    // 更新最大 id
    maxIdScanned = releaseMessages.get(messageScanned - 1).getId();
    // 如果不足 500, 说明没有新消息了
    return messageScanned == 500;
  }

  /**
   * 查找当前最大的消息id
   *
   * @return 当前最大的消息id
   */
  private long loadLargestMessageId() {
    ReleaseMessage releaseMessage = releaseMessageRepository.findTopByOrderByIdDesc();
    return releaseMessage == null ? 0 : releaseMessage.getId();
  }

  /**
   * 通知监听器已加载消息
   *
   * @param messages 发布消息列表信息
   */
  private void fireMessageScanned(List<ReleaseMessage> messages) {
    for (ReleaseMessage message : messages) {
      for (ReleaseMessageListener listener : listeners) {
        try {
          listener.handleMessage(message, Topics.APOLLO_RELEASE_TOPIC);
        } catch (Throwable ex) {
          Tracer.logError(ex);
          log.error("Failed to invoke message listener {}", listener.getClass(), ex);
        }
      }
    }
  }
}
