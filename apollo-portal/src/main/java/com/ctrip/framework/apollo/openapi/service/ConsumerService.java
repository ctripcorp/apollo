package com.ctrip.framework.apollo.openapi.service;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.hash.Hashing;

import com.ctrip.framework.apollo.openapi.entity.Consumer;
import com.ctrip.framework.apollo.openapi.entity.ConsumerAudit;
import com.ctrip.framework.apollo.openapi.entity.ConsumerToken;
import com.ctrip.framework.apollo.openapi.repository.ConsumerAuditRepository;
import com.ctrip.framework.apollo.openapi.repository.ConsumerRepository;
import com.ctrip.framework.apollo.openapi.repository.ConsumerTokenRepository;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;

import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ConsumerService {
  static final String TOKEN_SALT_KEY = "consumer.token.salt";
  private static final FastDateFormat TIMESTAMP_FORMAT = FastDateFormat.getInstance
      ("yyyyMMddHHmmss");
  private static final Joiner KEY_JOINER = Joiner.on("|");
  @Autowired
  private ConsumerTokenRepository consumerTokenRepository;
  @Autowired
  private ConsumerRepository consumerRepository;
  @Autowired
  private ConsumerAuditRepository consumerAuditRepository;
  @Autowired
  private PortalConfig portalConfig;

  public Long getConsumerIdByToken(String token) {
    if (Strings.isNullOrEmpty(token)) {
      return null;
    }
    ConsumerToken consumerToken = consumerTokenRepository.findTopByTokenAndExpiresAfter(token,
        new Date());
    return consumerToken == null ? null : consumerToken.getConsumerId();
  }

  public Consumer getConsumerByConsumerId(long consumerId) {
    return consumerRepository.findOne(consumerId);
  }

  public void generateAndEnrichConsumerToken(ConsumerToken consumerToken) {
    Consumer consumer = getConsumerByConsumerId(consumerToken.getConsumerId());

    Preconditions.checkState(consumer != null, String.format("Consumer with id: %d not found!",
        consumerToken.getConsumerId()));

    if (consumerToken.getDataChangeCreatedTime() == null) {
      consumerToken.setDataChangeCreatedTime(new Date());
    }
    consumerToken.setToken(generateConsumerToken(consumer.getAppId(), consumerToken
        .getDataChangeCreatedTime(), portalConfig.consumerTokenSalt()));
  }

  @Transactional
  public ConsumerToken createConsumerToken(ConsumerToken entity) {
    entity.setId(0); //for protection

    return consumerTokenRepository.save(entity);
  }

  @Transactional
  public void createConsumerAudits(Iterable<ConsumerAudit> consumerAudits) {
    consumerAuditRepository.save(consumerAudits);
  }

  String generateConsumerToken(String consumerAppId, Date generationTime, String
      consumerTokenSalt) {
    return Hashing.sha1().hashString(KEY_JOINER.join(consumerAppId, TIMESTAMP_FORMAT.format
        (generationTime), consumerTokenSalt), Charsets.UTF_8).toString();
  }

}
