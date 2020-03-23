package com.ctrip.framework.apollo.core.signature;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author nisiyong
 */
public class HmacSha1UtilsTest {

  @Test
  public void testSignString() {
    String stringToSign = "1576478257344\n/configs/100004458/default/application?ip=10.0.0.1";
    String accessKeySecret = "df23df3f59884980844ff3dada30fa97";

    String actualSignature = HmacSha1Utils.signString(stringToSign, accessKeySecret);

    String expectedSignature = "EoKyziXvKqzHgwx+ijDJwgVTDgE=";
    assertEquals(expectedSignature, actualSignature);
  }
}