/*
 * Copyright 2021 Apollo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.ctrip.framework.apollo.config.data.extension.webclient.injector;

import com.ctrip.framework.apollo.config.data.extension.webclient.ApolloWebClientHttpClient;
import com.ctrip.framework.apollo.core.spi.Ordered;
import com.ctrip.framework.apollo.spi.ApolloInjectorCustomizer;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ApolloClientCustomHttpClientInjectorCustomizer implements ApolloInjectorCustomizer {

  /**
   * the order of the injector customizer
   */
  public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 100;

  private static ApolloWebClientHttpClient CUSTOM_HTTP_CLIENT;

  /**
   * set the webClient to use
   *
   * @param webClient webClient to use
   */
  public static void setCustomWebClient(WebClient webClient) {
    CUSTOM_HTTP_CLIENT = new ApolloWebClientHttpClient(webClient);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getInstance(Class<T> clazz) {
    if (clazz.isInstance(CUSTOM_HTTP_CLIENT)) {
      return (T) CUSTOM_HTTP_CLIENT;
    }
    return null;
  }

  @Override
  public <T> T getInstance(Class<T> clazz, String name) {
    return null;
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}
