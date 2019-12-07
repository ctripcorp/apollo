package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.openapi.auth.ConsumerPermissionValidator;
import com.ctrip.framework.apollo.openapi.util.ConsumerAuthUtil;
import com.ctrip.framework.apollo.portal.component.PermissionValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by kezhenxu at 2019/1/8 20:19.
 *
 * Configuration class that will disable authorization.
 *
 * @author kezhenxu (kezhenxu94@163.com)
 */
@Profile("skipAuthorization")
@Configuration
public class SkipAuthorizationConfiguration {
  @Primary
  @Bean
  public ConsumerPermissionValidator consumerPermissionValidator() {
    final ConsumerPermissionValidator mock = mock(ConsumerPermissionValidator.class);
    when(mock.hasCreateNamespacePermission(any(), any())).thenReturn(true);
    return mock;
  }

  @Primary
  @Bean
  public ConsumerAuthUtil consumerAuthUtil() {
    final ConsumerAuthUtil mock = mock(ConsumerAuthUtil.class);
    when(mock.getConsumerId(any())).thenReturn(1L);
    return mock;
  }

  @Primary
  @Bean("permissionValidator")
  public PermissionValidator permissionValidator() {
    final PermissionValidator mock = mock(PermissionValidator.class);
    when(mock.isSuperAdmin()).thenReturn(true);
    return mock;
  }
}
