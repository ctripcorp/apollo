package com.ctrip.framework.apollo.openapi.util;

import com.ctrip.framework.apollo.openapi.service.ConsumerService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class ConsumerAuthUtilTest {
  private ConsumerAuthUtil consumerAuthUtil;
  @Mock
  private ConsumerService consumerService;
  @Mock
  private HttpServletRequest request;

  @Before
  public void setUp() throws Exception {
    consumerAuthUtil = new ConsumerAuthUtil();
    ReflectionTestUtils.setField(consumerAuthUtil, "consumerService", consumerService);
  }

  @Test
  public void testGetConsumerId() throws Exception {
    String someToken = "someToken";
    Long someConsumerId = 1L;

    when(consumerService.getConsumerIdByToken(someToken)).thenReturn(someConsumerId);

    assertEquals(someConsumerId, consumerAuthUtil.getConsumerId(someToken));
    verify(consumerService, times(1)).getConsumerIdByToken(someToken);
  }

  @Test
  public void testStoreConsumerId() throws Exception {
    long someConsumerId = 1L;

    consumerAuthUtil.storeConsumerId(request, someConsumerId);

    verify(request, times(1)).setAttribute(ConsumerAuthUtil.CONSUMER_ID, someConsumerId);
  }

  @Test
  public void testRetrieveConsumerId() throws Exception {
    long someConsumerId = 1;

    when(request.getAttribute(ConsumerAuthUtil.CONSUMER_ID)).thenReturn(someConsumerId);

    assertEquals(someConsumerId, consumerAuthUtil.retrieveConsumerId(request));
    verify(request, times(1)).getAttribute(ConsumerAuthUtil.CONSUMER_ID);
  }

  @Test(expected = IllegalStateException.class)
  public void testRetrieveConsumerIdWithConsumerIdNotSet() throws Exception {
    consumerAuthUtil.retrieveConsumerId(request);
  }

  @Test(expected = IllegalStateException.class)
  public void testRetrieveConsumerIdWithConsumerIdInvalid() throws Exception {
    String someInvalidConsumerId = "abc";

    when(request.getAttribute(ConsumerAuthUtil.CONSUMER_ID)).thenReturn(someInvalidConsumerId);
    consumerAuthUtil.retrieveConsumerId(request);
  }

}
