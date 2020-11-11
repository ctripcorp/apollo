package com.ctrip.framework.apollo.openapi.util;

import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.openapi.entity.ConsumerAudit;
import com.ctrip.framework.apollo.openapi.service.ConsumerService;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * ConsumerAudit 工具类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ConsumerAuditUtil implements InitializingBean {

  /**
   * 审计队列最大大小
   */
  private static final int CONSUMER_AUDIT_MAX_SIZE = 10000;
  /**
   * 队列
   */
  private BlockingQueue<ConsumerAudit> audits = Queues
      .newLinkedBlockingQueue(CONSUMER_AUDIT_MAX_SIZE);
  /**
   * ExecutorService 对象
   */
  private final ExecutorService auditExecutorService;
  /**
   * 是否停止
   */
  private final AtomicBoolean auditStopped;
  /**
   * 批任务 ConsumerAudit 数量
   */
  private int BATCH_SIZE = 100;
  /**
   * 批任务 ConsumerAudit 等待超时时间
   */
  private long BATCH_TIMEOUT = 5;
  /**
   * 批任务超时时间单位
   */
  private TimeUnit BATCH_TIMEUNIT = TimeUnit.SECONDS;

  private final ConsumerService consumerService;

  public ConsumerAuditUtil(final ConsumerService consumerService) {
    this.consumerService = consumerService;
    auditExecutorService = Executors.newSingleThreadExecutor(
        ApolloThreadFactory.create("ConsumerAuditUtil", true));
    auditStopped = new AtomicBoolean(false);
  }

  public boolean audit(HttpServletRequest request, long consumerId) {
    //ignore GET request
    // 忽略 GET 请求
    if ("GET".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    // 组装 URI
    String uri = request.getRequestURI();
    if (!Strings.isNullOrEmpty(request.getQueryString())) {
      uri += "?" + request.getQueryString();
    }
    // 创建 ConsumerAudit 对象
    ConsumerAudit consumerAudit = new ConsumerAudit();
    Date now = new Date();
    consumerAudit.setConsumerId(consumerId);
    consumerAudit.setUri(uri);
    consumerAudit.setMethod(request.getMethod());
    consumerAudit.setDataChangeCreatedTime(now);
    consumerAudit.setDataChangeLastModifiedTime(now);

    //throw away audits if exceeds the max size
    // 添加到队列
    return this.audits.offer(consumerAudit);
  }

  @Override
  public void afterPropertiesSet() {
    auditExecutorService.submit(() -> {
      // 循环【批任务】，直到停止
      while (!auditStopped.get() && !Thread.currentThread().isInterrupted()) {
        List<ConsumerAudit> toAudit = Lists.newArrayList();
        try {
          // 获得 ConsumerAudit 批任务，直到到达上限，或者超时
          Queues.drain(audits, toAudit, BATCH_SIZE, BATCH_TIMEOUT, BATCH_TIMEUNIT);
          // 批量保存到数据库
          if (!toAudit.isEmpty()) {
            consumerService.createConsumerAudits(toAudit);
          }
        } catch (Throwable ex) {
          Tracer.logError(ex);
        }
      }
    });
  }

  /**
   * 停止审计
   */
  public void stopAudit() {
    auditStopped.set(true);
  }
}
