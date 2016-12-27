package com.ctrip.framework.apollo.openapi.service;

import com.ctrip.framework.apollo.openapi.entity.Consumer;
import com.ctrip.framework.apollo.openapi.entity.ConsumerToken;
import com.ctrip.framework.apollo.openapi.repository.ConsumerRepository;
import com.ctrip.framework.apollo.openapi.repository.ConsumerTokenRepository;
import com.ctrip.framework.apollo.portal.components.config.PortalConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class ConsumerServiceTest {
  private ConsumerService consumerService;
  @Mock
  private ConsumerTokenRepository consumerTokenRepository;
  @Mock
  private ConsumerRepository consumerRepository;
  @Mock
  private PortalConfig portalConfig;
  private String someTokenSalt;

  @Before
  public void setUp() throws Exception {
    consumerService = spy(new ConsumerService());
    ReflectionTestUtils.setField(consumerService, "consumerTokenRepository",
        consumerTokenRepository);
    ReflectionTestUtils.setField(consumerService, "consumerRepository",
        consumerRepository);
    ReflectionTestUtils.setField(consumerService, "portalConfig",
                                 portalConfig);
    someTokenSalt = "someTokenSalt";
    when(portalConfig.consumerTokenSalt()).thenReturn(someTokenSalt);

  }

  @Test
  public void testGetConsumerId() throws Exception {
    String someToken = "someToken";
    long someConsumerId = 1;
    ConsumerToken someConsumerToken = new ConsumerToken();
    someConsumerToken.setConsumerId(someConsumerId);

    when(consumerTokenRepository.findTopByTokenAndExpiresAfter(eq(someToken), any(Date.class)))
        .thenReturn(someConsumerToken);

    assertEquals(someConsumerId, consumerService.getConsumerIdByToken(someToken).longValue());
  }

  @Test
  public void testGetConsumerIdWithNullToken() throws Exception {
    Long consumerId = consumerService.getConsumerIdByToken(null);

    assertNull(consumerId);
    verify(consumerTokenRepository, never()).findTopByTokenAndExpiresAfter(anyString(), any(Date
        .class));
  }

  @Test
  public void testGetConsumerByConsumerId() throws Exception {
    long someConsumerId = 1;
    Consumer someConsumer = mock(Consumer.class);

    when(consumerRepository.findOne(someConsumerId)).thenReturn(someConsumer);

    assertEquals(someConsumer, consumerService.getConsumerByConsumerId(someConsumerId));
    verify(consumerRepository, times(1)).findOne(someConsumerId);
  }

  @Test
  public void testCreateConsumerToken() throws Exception {
    ConsumerToken someConsumerToken = mock(ConsumerToken.class);
    ConsumerToken savedConsumerToken = mock(ConsumerToken.class);

    when(consumerTokenRepository.save(someConsumerToken)).thenReturn(savedConsumerToken);

    assertEquals(savedConsumerToken, consumerService.createConsumerToken(someConsumerToken));
  }

  @Test
  public void testGenerateConsumerToken() throws Exception {
    String someConsumerAppId = "100003171";
    Date generationTime = new GregorianCalendar(2016, Calendar.AUGUST, 9, 12, 10, 50).getTime();
    String tokenSalt = "apollo";

    assertEquals("d0da35292dd5079eeb73cc3a5f7c0759afabd806", consumerService
        .generateConsumerToken(someConsumerAppId, generationTime, tokenSalt));
  }

  @Test
  public void testGenerateAndEnrichConsumerToken() throws Exception {
    String someConsumerAppId = "someAppId";
    long someConsumerId = 1;
    String someToken = "someToken";
    Date generationTime = new Date();
    Consumer consumer = mock(Consumer.class);

    when(consumerRepository.findOne(someConsumerId)).thenReturn(consumer);
    when(consumer.getAppId()).thenReturn(someConsumerAppId);
    when(consumerService.generateConsumerToken(someConsumerAppId, generationTime, someTokenSalt))
        .thenReturn(someToken);

    ConsumerToken consumerToken = new ConsumerToken();
    consumerToken.setConsumerId(someConsumerId);
    consumerToken.setDataChangeCreatedTime(generationTime);

    consumerService.generateAndEnrichConsumerToken(consumerToken);

    assertEquals(someToken, consumerToken.getToken());
  }

  @Test(expected = IllegalStateException.class)
  public void testGenerateAndEnrichConsumerTokenWithConsumerNotFound() throws Exception {
    long someConsumerIdNotExist = 1;

    ConsumerToken consumerToken = new ConsumerToken();
    consumerToken.setConsumerId(someConsumerIdNotExist);

    when(consumerRepository.findOne(someConsumerIdNotExist)).thenReturn(null);

    consumerService.generateAndEnrichConsumerToken(consumerToken);
  }
}
