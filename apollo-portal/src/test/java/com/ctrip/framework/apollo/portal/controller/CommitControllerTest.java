package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.portal.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by kezhenxu at 2019/1/14 12:49.
 *
 * @author kezhenxu (kezhenxu at lizhi dot fm)
 */
public class CommitControllerTest extends AbstractIntegrationTest {

  @Test
  public void shouldFailWhenPageOrSiseIsNegative() {
    try {
      restTemplate.getForEntity(
          url("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/commits?page=-1"),
          List.class, "1", "env", "cl", "ns"
      );
      fail("should throw");
    } catch (final HttpClientErrorException e) {
      assertThat(
          new String(e.getResponseBodyAsByteArray()), containsString("page should be positive or 0")
      );
    }
    try {
      restTemplate.getForEntity(
          url("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/commits?size=0"),
          List.class, "1", "env", "cl", "ns"
      );
      fail("should throw");
    } catch (final HttpClientErrorException e) {
      assertThat(
          new String(e.getResponseBodyAsByteArray()), containsString("size should be positive number")
      );
    }
  }
}
