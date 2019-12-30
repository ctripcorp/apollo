package com.ctrip.framework.apollo.core.signature;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.Test;

/**
 * @author nisiyong
 */
public class SignatureTest {

  @Test
  public void testSignature() {
    String timestamp = "1576478257344";
    String pathWithQuery = "/configs/100004458/default/application?ip=10.0.0.1";
    String secret = "df23df3f59884980844ff3dada30fa97";

    String actualSignature = Signature.signature(timestamp, pathWithQuery, secret);

    String expectedSignature = "EoKyziXvKqzHgwx+ijDJwgVTDgE=";
    assertThat(actualSignature).isEqualTo(expectedSignature);
  }

  @Test
  public void testBuildHttpHeaders() {
    String url = "http://10.0.0.1:8080/configs/100004458/default/application?ip=10.0.0.1";
    String appId = "100004458";
    String secret = "df23df3f59884980844ff3dada30fa97";

    Map<String, String> actualHttpHeaders = Signature.buildHttpHeaders(url, appId, secret);

    assertThat(actualHttpHeaders)
        .containsKeys(Signature.HTTP_HEADER_AUTHORIZATION, Signature.HTTP_HEADER_TIMESTAMP);
  }
}