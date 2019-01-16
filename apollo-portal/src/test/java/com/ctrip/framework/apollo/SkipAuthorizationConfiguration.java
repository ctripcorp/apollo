package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.openapi.auth.ConsumerPermissionValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by kezhenxu at 2019/1/8 20:19.
 *
 * @author kezhenxu (kezhenxu94@163.com)
 */
@Profile("skipAuthorization")
@Configuration
public class SkipAuthorizationConfiguration {
  @Primary
  @Bean
  @Qualifier("consumerPermissionValidator")
  public ConsumerPermissionValidator consumerPermissionValidator() {
    ConsumerPermissionValidator mock = mock(ConsumerPermissionValidator.class);
    when(mock.hasCreateNamespacePermission(any(), any())).thenReturn(true);
    return mock;
  }
}
