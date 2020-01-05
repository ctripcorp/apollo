package com.ctrip.framework.apollo.core.signature;

import com.google.common.io.BaseEncoding;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author nisiyong
 */
public class HmacSha1Utils {

  private static final String ALGORITHM_NAME = "HmacSHA1";
  private static final String ENCODING = "UTF-8";

  public static String signString(String stringToSign, String accessKeySecret) {
    try {
      Mac mac = Mac.getInstance(ALGORITHM_NAME);
      mac.init(new SecretKeySpec(
          accessKeySecret.getBytes(ENCODING),
          ALGORITHM_NAME
      ));
      byte[] signData = mac.doFinal(stringToSign.getBytes(ENCODING));
      return BaseEncoding.base64().encode(signData);
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
      throw new IllegalArgumentException(e.toString());
    }
  }
}
