package com.ctrip.framework.apollo.core.signature;

import com.google.common.io.BaseEncoding;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * HmacSHA1加密工具类
 *
 * @author nisiyong
 */
public class HmacSha1Utils {

  /**
   * 算法名称
   */
  private static final String ALGORITHM_NAME = "HmacSHA1";
  /**
   * 编码格式
   */
  private static final String ENCODING = "UTF-8";

  /**
   * 加密后的签名
   *
   * @param stringToSign    签名字符串
   * @param accessKeySecret 访问密钥
   * @return 加密后的签名字符串
   */
  public static String signString(String stringToSign, String accessKeySecret) {
    try {
      byte[] data = accessKeySecret.getBytes(ENCODING);
      // 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
      SecretKey secretKey = new SecretKeySpec(data, ALGORITHM_NAME);
      // 生成一个指定 Mac 算法 的 Mac 对象
      Mac mac = Mac.getInstance(ALGORITHM_NAME);
      // 用给定密钥初始化 Mac 对象
      mac.init(secretKey);
      byte[] text = stringToSign.getBytes(ENCODING);
      // 完成 Mac 操作
      byte[] signData = mac.doFinal(text);
      return BaseEncoding.base64().encode(signData);
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
      throw new IllegalArgumentException(e.toString());
    }
  }
}
