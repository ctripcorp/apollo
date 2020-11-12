package com.ctrip.framework.apollo.tracer.internals;

import com.ctrip.framework.apollo.tracer.spi.Transaction;

/**
 * Null事务实现
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class NullTransaction implements Transaction {

  @Override
  public void setStatus(String status) {
  }

  @Override
  public void setStatus(Throwable e) {
  }

  @Override
  public void addData(String key, Object value) {
  }

  @Override
  public void complete() {
  }
}
