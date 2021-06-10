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
package com.ctrip.framework.apollo.config.data.importer;

import com.ctrip.framework.apollo.core.ConfigConsts;
import org.springframework.boot.context.config.ConfigDataResource;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class ApolloConfigDataResource extends ConfigDataResource {

  /**
   * default resource instance
   */
  public static final ApolloConfigDataResource DEFAULT = new ApolloConfigDataResource(
      ConfigConsts.NAMESPACE_APPLICATION);

  /**
   * apollo config namespace
   */
  private final String namespace;

  public ApolloConfigDataResource(String namespace) {
    this.namespace = namespace;
  }

  public String getNamespace() {
    return namespace;
  }

  @Override
  public String toString() {
    return "ApolloConfigDataResource{" +
        "namespace='" + namespace + '\'' +
        '}';
  }
}
