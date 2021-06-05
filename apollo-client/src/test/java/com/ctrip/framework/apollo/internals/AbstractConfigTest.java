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
package com.ctrip.framework.apollo.internals;

import static org.junit.Assert.*;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import java.util.Set;
import org.junit.Test;

/**
 * @author wxq
 */
public class AbstractConfigTest {

  @Test
  public void fireConfigChange() {
    AbstractConfig abstractConfig = new EmptyAbstractConfig();

    ConfigChangeListener configChangeListener = new ConfigChangeListener() {
      @Override
      public void onChange(ConfigChangeEvent changeEvent) {
        assertTrue(changeEvent.interestedChangedKeys().size() > 0);
      }
    };
//    abstractConfig.addChangeListener();
//    abstractConfig.fi
  }

  @Test
  public void resolveInterestedChangedKeys() {

  }

  /**
   * Empty class for test.
   *
   * @author wxq
   */
  private static class EmptyAbstractConfig extends AbstractConfig {

    @Override
    public String getProperty(String key, String defaultValue) {
      return null;
    }

    @Override
    public Set<String> getPropertyNames() {
      return null;
    }

    @Override
    public ConfigSourceType getSourceType() {
      return null;
    }
  }
}