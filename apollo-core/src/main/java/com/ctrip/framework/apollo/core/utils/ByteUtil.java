package com.ctrip.framework.apollo.core.utils;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ByteUtil {

  /**
   * 16进制字符
   */
  private static final char[] HEX_CHARS = new char[]{
      '0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  /**
   * 获取int最高位的字节
   *
   * @param x 整数值
   * @return int最高位的字节
   */
  public static byte int3(final int x) {
    return (byte) (x >> 24);
  }

  /**
   * 获取int第二高位的字节
   *
   * @param x 整数值
   * @return int第二高位的字节
   */
  public static byte int2(final int x) {
    return (byte) (x >> 16);
  }

  /**
   * 获取int第二低位的字节
   *
   * @param x 整数值
   * @return int第二低位的字节
   */
  public static byte int1(final int x) {
    return (byte) (x >> 8);
  }

  /**
   * 获取int最低位的字节
   *
   * @param x 整数值
   * @return int最低位的字节
   */
  public static byte int0(final int x) {
    return (byte) (x);
  }

  /**
   * byte转16进制字符串
   *
   * @param bytes 字节数组
   * @return 16进制字符串
   */
  public static String toHexString(byte[] bytes) {
    char[] chars = new char[bytes.length * 2];
    int i = 0;
    for (byte b : bytes) {
      chars[i++] = HEX_CHARS[b >> 4 & 0xF];
      chars[i++] = HEX_CHARS[b & 0xF];
    }
    return new String(chars);
  }
}
