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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 此类用于包装包含编码{@link ByteOrderMark}的流作为其第一个字节。
 * <p>
 * 这个类检测这些字节，如果需要，可以自动跳过它们，并将后续字节作为流中的第一个字节返回。
 * <p>
 * {@link ByteOrderMark}实现具有以下预定义的bom：
 *
 * <ul>
 * <li>UTF-8 - {@link ByteOrderMark#UTF_8}</li>
 * <li>UTF-16BE - {@link ByteOrderMark#UTF_16LE}</li>
 * <li>UTF-16LE - {@link ByteOrderMark#UTF_16BE}</li>
 * <li>UTF-32BE - {@link ByteOrderMark#UTF_32LE}</li>
 * <li>UTF-32LE - {@link ByteOrderMark#UTF_32BE}</li>
 * </ul>
 *
 *
 * <h3>示例1-检测并排除UTF-8 BOM</h3>
 *
 * <pre>
 * BOMInputStream bomIn = new BOMInputStream(in);
 * if (bomIn.hasBOM()) {
 *   // has a UTF-8 BOM
 * }
 * </pre>
 *
 * <h3>示例2 - 检测UTF-8 BOM（但不要排除它）</h3>
 *
 * <pre>
 * boolean include = true;
 * BOMInputStream bomIn = new BOMInputStream(in, include);
 * if (bomIn.hasBOM()) {
 *   // has a UTF-8 BOM
 * }
 * </pre>
 *
 * <h3>示例3 - 检测多个BOM表</h3>
 *
 * <pre>
 * BOMInputStream bomIn = new BOMInputStream(in, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_32LE,
 *     ByteOrderMark.UTF_32BE);
 * if (bomIn.hasBOM() == false) {
 *   // No BOM found
 * } else if (bomIn.hasBOM(ByteOrderMark.UTF_16LE)) {
 *   // has a UTF-16LE BOM
 * } else if (bomIn.hasBOM(ByteOrderMark.UTF_16BE)) {
 *   // has a UTF-16BE BOM
 * } else if (bomIn.hasBOM(ByteOrderMark.UTF_32LE)) {
 *   // has a UTF-32LE BOM
 * } else if (bomIn.hasBOM(ByteOrderMark.UTF_32BE)) {
 *   // has a UTF-32BE BOM
 * }
 * </pre>
 *
 * @version $Id: BOMInputStream.java 1686527 2015-06-20 06:31:39Z krosenvold $
 * @see ByteOrderMark
 * @see <a href="http://en.wikipedia.org/wiki/Byte_order_mark">Wikipedia - Byte Order Mark</a>
 * @since 2.0
 */
public class BOMInputStream extends ProxyInputStream {

  /**
   * 是否包含
   */
  private final boolean include;
  /**
   * BOM表按最长到最短排序。
   */
  private final List<ByteOrderMark> boms;
  /**
   * 字节排序标记
   */
  private ByteOrderMark byteOrderMark;
  /**
   * 第一字节列表
   */
  private int[] firstBytes;
  /**
   * 长度
   */
  private int fbLength;
  /**
   * 下标
   */
  private int fbIndex;
  /**
   * 标记的下标
   */
  private int markFbIndex;
  /**
   * 是否开始标记
   */
  private boolean markedAtStart;

  /**
   * 构造一个新的字节序输入流，它排除{@link ByteOrderMark#UTF_8}字节序。
   *
   * @param delegate 要委托给的InputStream
   */
  public BOMInputStream(final InputStream delegate) {
    this(delegate, false, ByteOrderMark.UTF_8);
  }

  /**
   * 构造一个新的字节序输入流来检测{@link ByteOrderMark#UTF_8}并可选地包含它。
   *
   * @param delegate 要委托给的InputStream
   * @param include  如果为true，则包含UTF-8 字节序；如果为false，则排除它
   */
  public BOMInputStream(final InputStream delegate, final boolean include) {
    this(delegate, include, ByteOrderMark.UTF_8);
  }

  /**
   * 构造排除指定字节序的新字节序输入流
   *
   * @param delegate 要委托给的InputStream
   * @param boms     要发现和排除的字节序列表
   */
  public BOMInputStream(final InputStream delegate, final ByteOrderMark... boms) {
    this(delegate, false, boms);
  }

  /**
   * 按长度降序比较字节序标记类对象
   */
  private static final Comparator<ByteOrderMark> ByteOrderMarkLengthComparator = (bom1, bom2) -> {
    final int len1 = bom1.length();
    final int len2 = bom2.length();
    if (len1 > len2) {
      return EOF;
    }
    if (len2 > len1) {
      return 1;
    }
    return 0;
  };

  /**
   * 构造新的字节序输入流，该输入流检测指定的字节序并选择包含这些字节序.
   *
   * @param delegate 要委托给的InputStream
   * @param include  如果为true，则包含指定的字节序；如果为false，则排除这些字节序
   * @param boms     要发现和排除的字节序列表
   */
  public BOMInputStream(final InputStream delegate, final boolean include,
      final ByteOrderMark... boms) {
    super(delegate);
    if (boms == null || boms.length == 0) {
      throw new IllegalArgumentException("No BOMs specified");
    }
    this.include = include;
    // Sort the BOMs to match the longest BOM first because some BOMs have the same starting two bytes.
    Arrays.sort(boms, ByteOrderMarkLengthComparator);
    this.boms = Arrays.asList(boms);

  }

  /**
   * 判断流是否包含指定的字节序之一.
   *
   * @return 如果流有一个指定的bom，否则返回false
   * @throws IOException 如果在读取流的第一个字节时发生错误，抛出
   */
  public boolean hasBOM() throws IOException {
    return getBOM() != null;
  }

  /**
   * Indicates whether the stream contains the specified BOM.
   *
   * @param bom The BOM to check for
   * @return true if the stream has the specified BOM, otherwise false if it does not
   * @throws IllegalArgumentException if the BOM is not one the stream is configured to detect
   * @throws IOException              if an error reading the first bytes of the stream occurs
   */
  public boolean hasBOM(final ByteOrderMark bom) throws IOException {
    if (!boms.contains(bom)) {
      throw new IllegalArgumentException("Stream not configure to detect " + bom);
    }
    getBOM();
    return byteOrderMark != null && byteOrderMark.equals(bom);
  }

  /**
   * 返回字节序
   *
   * @return 如果没有，则为空
   * @throws IOException 如果在读取流的第一个字节时发生错误，抛出
   */
  public ByteOrderMark getBOM() throws IOException {
    if (firstBytes == null) {
      fbLength = 0;
      // BOMs are sorted from longest to shortest
      final int maxBomSize = boms.get(0).length();
      firstBytes = new int[maxBomSize];
      // Read first maxBomSize bytes
      for (int i = 0; i < firstBytes.length; i++) {
        firstBytes[i] = in.read();
        fbLength++;
        if (firstBytes[i] < 0) {
          break;
        }
      }
      // match BOM in firstBytes
      byteOrderMark = find();
      if (byteOrderMark != null) {
        if (!include) {
          if (byteOrderMark.length() < firstBytes.length) {
            fbIndex = byteOrderMark.length();
          } else {
            fbLength = 0;
          }
        }
      }
    }
    return byteOrderMark;
  }

  /**
   * 返回字节序字符集名称- {@link ByteOrderMark#getCharsetName()}.
   *
   * @return 字节序字符集名称，如果找不到字节序，则为空
   * @throws IOException 如果在读取流的第一个字节时发生错误，抛出
   */
  public String getBOMCharsetName() throws IOException {
    getBOM();
    return byteOrderMark == null ? null : byteOrderMark.getCharsetName();
  }

  /**
   * 此方法读取并保留或跳过流中的第一个字节。它的行为类似于单字节的<code>read()</code>方法，要么返回一个有效的字节，要么返回-1以表示初始字节已经被处理。
   *
   * @return 读取的字节（不包括字节序）,如果流结束，返回-1
   * @throws IOException 如果发生I/O错误 抛出
   */
  private int readFirstBytes() throws IOException {
    getBOM();
    return fbIndex < fbLength ? firstBytes[fbIndex++] : EOF;
  }

  /**
   * 查找具有指定字节的字节序。
   *
   * @return 匹配的字节序，如果没有匹配，则为空
   */
  private ByteOrderMark find() {
    for (final ByteOrderMark bom : boms) {
      if (matches(bom)) {
        return bom;
      }
    }
    return null;
  }

  /**
   * 检查字节是否与字节序匹配
   *
   * @param bom 字节序对象
   * @return 如果字节与字节序匹配，则为true，否则为false
   */
  private boolean matches(final ByteOrderMark bom) {
    // if (bom.length() != fbLength) {
    // return false;
    // }
    // 第一个字节可能大于BOM字节
    for (int i = 0; i < bom.length(); i++) {
      if (bom.get(i) != firstBytes[i]) {
        return false;
      }
    }
    return true;
  }

  // ----------------------------------------------------------------------------
  // Implementation of InputStream
  // ----------------------------------------------------------------------------

  @Override
  public int read() throws IOException {
    final int b = readFirstBytes();
    return b >= 0 ? b : in.read();
  }

  @Override
  public int read(final byte[] buf, int off, int len) throws IOException {
    int firstCount = 0;
    int b = 0;
    while (len > 0 && b >= 0) {
      b = readFirstBytes();
      if (b >= 0) {
        buf[off++] = (byte) (b & 0xFF);
        len--;
        firstCount++;
      }
    }
    final int secondCount = in.read(buf, off, len);
    return secondCount < 0 ? firstCount > 0 ? firstCount : EOF : firstCount + secondCount;
  }

  @Override
  public int read(final byte[] buf) throws IOException {
    return read(buf, 0, buf.length);
  }

  @Override
  public synchronized void mark(final int readlimit) {
    markFbIndex = fbIndex;
    markedAtStart = firstBytes == null;
    in.mark(readlimit);
  }

  @Override
  public synchronized void reset() throws IOException {
    fbIndex = markFbIndex;
    if (markedAtStart) {
      firstBytes = null;
    }

    in.reset();
  }

  @Override
  public long skip(long n) throws IOException {
    int skipped = 0;
    while ((n > skipped) && (readFirstBytes() >= 0)) {
      skipped++;
    }
    return in.skip(n - skipped) + skipped;
  }
}
