package com.ctrip.framework.apollo.tracer.internals;

import com.ctrip.framework.apollo.core.utils.ClassLoaderUtil;
import com.ctrip.framework.apollo.tracer.internals.cat.CatMessageProducer;
import com.ctrip.framework.apollo.tracer.internals.cat.CatNames;
import com.ctrip.framework.apollo.tracer.spi.MessageProducer;
import com.ctrip.framework.apollo.tracer.spi.MessageProducerManager;

/**
 * 默认的消息生产者管理器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultMessageProducerManager implements MessageProducerManager {

  /**
   * 消息生产者
   */
  private static MessageProducer producer;

  /**
   * 构造配置的消息生产者管理器
   * <p>如果类存在，使用CatMessageProducer，不存在,使用NullMessageProducerManager</p>
   */
  public DefaultMessageProducerManager() {
    if (ClassLoaderUtil.isClassPresent(CatNames.CAT_CLASS)) {
      producer = new CatMessageProducer();
    } else {
      producer = new NullMessageProducerManager().getProducer();
    }
  }

  @Override
  public MessageProducer getProducer() {
    return producer;
  }
}
