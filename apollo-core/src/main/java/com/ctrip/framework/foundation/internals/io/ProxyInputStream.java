/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.ctrip.framework.foundation.internals.io;

import static com.ctrip.framework.foundation.internals.io.IOUtils.EOF;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 代理输入流，它按预期的方式运行，即它将方法调用传递到代理流，并且不会更改正在调用的方法。
 * <p>它是FilterInputStream的另一个基类，以提高可重用性，因为FilterInputStream会更改所调用的方法，例如读取（字节[]），以读取（字节[]、int、int）。
 * <p>请参阅受保护的方法，以了解子类可以轻松地使用自定义的预处理、后处理或错误处理功能来装饰流的方法。
 *
 * @version $Id: ProxyInputStream.java 1603493 2014-06-18 15:46:07Z ggregory $
 */
public abstract class ProxyInputStream extends FilterInputStream {

  /**
   * 构造新的ProxyInputStream.
   *
   * @param proxy 被委托的InputStream
   */
  public ProxyInputStream(final InputStream proxy) {
    super(proxy);
    // 代理存储在名为“in”的受保护超类变量中
  }

  /**
   * 调用委托类的<code>read（）</code>方法。
   *
   * @return 字节读取。或者流结束，返回-1
   * @throws IOException 如果发生I/O错误 抛出
   */
  @Override
  public int read() throws IOException {
    try {
      beforeRead(1);
      final int b = in.read();
      afterRead(b != EOF ? 1 : EOF);
      return b;
    } catch (final IOException e) {
      handleIOException(e);
      return EOF;
    }
  }

  /**
   * 调用委托类的<code>read（final byte[] bts）</code>方法。
   *
   * @param bts 要将字节读入的缓冲区
   * @return 如果流结束，则为读取的字节数或者流结束，返回-1
   * @throws IOException 如果发生I/O错误 抛出
   */
  @Override
  public int read(final byte[] bts) throws IOException {
    try {
      beforeRead(bts != null ? bts.length : 0);
      final int n = in.read(bts);
      afterRead(n);
      return n;
    } catch (final IOException e) {
      handleIOException(e);
      return EOF;
    }
  }

  /**
   * 调用委托类的 <code>read(byte[], int, int)</code> 方法.
   *
   * @param bts 要将字节读入的缓冲区
   * @param off 起始偏移量
   * @param len 要读取的字节数
   * @return 如果流结束，则为读取的字节数或者流结束，返回-1
   * @throws IOException 如果发生I/O错误 抛出
   */
  @Override
  public int read(final byte[] bts, final int off, final int len) throws IOException {
    try {
      beforeRead(len);
      final int n = in.read(bts, off, len);
      afterRead(n);
      return n;
    } catch (final IOException e) {
      handleIOException(e);
      return EOF;
    }
  }

  /**
   * 调用委托类的  <code>skip(long)</code> 方法.
   *
   * @param ln 要跳过的字节数
   * @return 实际跳过的字节数
   * @throws IOException 如果发生I/O错误 抛出
   */
  @Override
  public long skip(final long ln) throws IOException {
    try {
      return in.skip(ln);
    } catch (final IOException e) {
      handleIOException(e);
      return 0;
    }
  }

  /**
   * 调用委托类的  <code>available()</code> 方法.
   *
   * @return 可用的字节数
   * @throws IOException 如果发生I/O错误 抛出
   */
  @Override
  public int available() throws IOException {
    try {
      return super.available();
    } catch (final IOException e) {
      handleIOException(e);
      return 0;
    }
  }

  /**
   * 调用委托类的  <code>close()</code> 方法.
   *
   * @throws IOException 如果发生I/O错误 抛出
   */
  @Override
  public void close() throws IOException {
    try {
      in.close();
    } catch (final IOException e) {
      handleIOException(e);
    }
  }

  /**
   * 调用委托类的 <code>mark(int)</code> 方法.
   *
   * @param readlimit 预读限制
   */
  @Override
  public synchronized void mark(final int readlimit) {
    in.mark(readlimit);
  }

  /**
   * 调用委托类的 <code>reset()</code> 方法.
   *
   * @throws IOException 如果发生I/O错误 抛出
   */
  @Override
  public synchronized void reset() throws IOException {
    try {
      in.reset();
    } catch (final IOException e) {
      handleIOException(e);
    }
  }

  /**
   * 调用委托类的 <code>markSupported()</code> 方法.
   *
   * @return 如果支持标记，则为true，否则为false
   */
  @Override
  public boolean markSupported() {
    return in.markSupported();
  }

  /**
   * 在代理调用成功返回后由read方法调用。返回给调用者的字节数（如果到达流的末尾，则返回-1）作为参数。
   * <p>
   * 子类可以重写此方法以添加公共的后处理功能，而不必重写所有的read方法。默认实现什么也不做。
   * <p>
   * 注意这个方法不是从{@link #skip（long）}或{@link #reset（）}调用的。如果还想向这些方法添加后处理步骤，则需要显式重写这些方法。
   *
   * @param n 调用方要求读取的字节数
   * @throws IOException 如果预处理失败，抛出
   * @since 2.0
   */
  protected void beforeRead(final int n) throws IOException {
    // no-op
  }

  /**
   * 在代理调用成功返回后由read方法调用。返回给调用者的字节数（如果到达流的末尾，则返回-1）作为参数。
   * <p>
   * 子类可以重写此方法以添加公共的后处理功能，而不必重写所有的read方法。默认实现什么也不做。
   * <p>
   * 注意这个方法不是从{@link #skip（long）}或{@link #reset（）}调用的。如果还想向这些方法添加后处理步骤，则需要显式重写这些方法
   *
   * @param n 读取的字节数，如果到达流的结尾，则为-1
   * @throws IOException 如果后置处理失败，抛出
   * @since 2.0
   */
  protected void afterRead(final int n) throws IOException {
    // no-op
  }

  /**
   * 处理引发的任何IOException。
   * <p>
   * 此方法提供了一个实现自定义异常处理的点。默认行为是重新抛出异常。
   *
   * @param e The IOException thrown
   * @throws IOException if an I/O error occurs
   * @since 2.0
   */
  protected void handleIOException(final IOException e) throws IOException {
    throw e;
  }

}
