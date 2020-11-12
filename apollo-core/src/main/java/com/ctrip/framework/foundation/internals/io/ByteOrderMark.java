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

import java.io.Serializable;

/**
 * 字节序标记类（BOM）- 详情请查阅 {@link BOMInputStream}.
 *
 * @version $Id: ByteOrderMark.java 1586504 2014-04-10 23:34:37Z ggregory $
 * @see BOMInputStream
 * @see <a href="http://en.wikipedia.org/wiki/Byte_order_mark">Wikipedia: Byte Order Mark</a>
 * @see <a href="http://www.w3.org/TR/2006/REC-xml-20060816/#sec-guessing">W3C: Autodetection of
 * Character Encodings (Non-Normative)</a>
 * @since 2.0
 */
public class ByteOrderMark implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * UTF-8字节序
   */
  public static final ByteOrderMark UTF_8 = new ByteOrderMark("UTF-8", 0xEF, 0xBB, 0xBF);

  /**
   * UTF-16BE 字节序 (大端)
   */
  public static final ByteOrderMark UTF_16BE = new ByteOrderMark("UTF-16BE", 0xFE, 0xFF);

  /**
   * UTF-16LE 字节序 (小端)
   */
  public static final ByteOrderMark UTF_16LE = new ByteOrderMark("UTF-16LE", 0xFF, 0xFE);

  /**
   * UTF-32BE 字节序(大端)
   *
   * @since 2.2
   */
  public static final ByteOrderMark UTF_32BE = new ByteOrderMark("UTF-32BE", 0x00, 0x00, 0xFE,
      0xFF);

  /**
   * UTF-32LE 字节序(小端)
   *
   * @since 2.2
   */
  public static final ByteOrderMark UTF_32LE = new ByteOrderMark("UTF-32LE", 0xFF, 0xFE, 0x00,
      0x00);

  /**
   * Unicode字节序字符；外部形式取决于编码
   *
   * @see <a href="http://unicode.org/faq/utf_bom.html#BOM">Byte Order Mark (BOM) FAQ</a>
   * @since 2.5
   */
  public static final char UTF_BOM = '\uFEFF';
  /**
   * 字符集名称
   */
  private final String charsetName;
  /**
   * 字节数组
   */
  private final int[] bytes;

  /**
   * 构建新的字节序
   *
   * @param charsetName 字节序所代表的字符集名称
   * @param bytes       字节序字节数组
   * @throws IllegalArgumentException 如果charsetName为null或长度为零，抛出
   * @throws IllegalArgumentException 如果字节为空或零长度，抛出
   */
  public ByteOrderMark(final String charsetName, final int... bytes) {
    if (charsetName == null || charsetName.isEmpty()) {
      throw new IllegalArgumentException("No charsetName specified");
    }
    if (bytes == null || bytes.length == 0) {
      throw new IllegalArgumentException("No bytes specified");
    }
    this.charsetName = charsetName;
    this.bytes = new int[bytes.length];
    System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
  }

  /**
   * 返回字节序所代表的{@link java.nio.charset.Charset}名称。
   *
   * @return 字符集名称
   */
  public String getCharsetName() {
    return charsetName;
  }

  /**
   * 返回字节序的字节长度
   *
   * @return 字节序的字节长度
   */
  public int length() {
    return bytes.length;
  }

  /**
   * 指定位置的字节
   *
   * @param pos 指定位置
   * @return 指定位置的字节
   */
  public int get(final int pos) {
    return bytes[pos];
  }

  /**
   * 返回字节序字节的副本
   *
   * @return 字节序字节的副本
   */
  public byte[] getBytes() {
    final byte[] copy = new byte[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      copy[i] = (byte) bytes[i];
    }
    return copy;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof ByteOrderMark)) {
      return false;
    }
    final ByteOrderMark bom = (ByteOrderMark) obj;
    if (bytes.length != bom.length()) {
      return false;
    }
    for (int i = 0; i < bytes.length; i++) {
      if (bytes[i] != bom.get(i)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = getClass().hashCode();
    for (final int b : bytes) {
      hashCode += b;
    }
    return hashCode;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append('[');
    builder.append(charsetName);
    builder.append(": ");
    for (int i = 0; i < bytes.length; i++) {
      if (i > 0) {
        builder.append(",");
      }
      builder.append("0x");
      builder.append(Integer.toHexString(0xFF & bytes[i]).toUpperCase());
    }
    builder.append(']');
    return builder.toString();
  }

}


