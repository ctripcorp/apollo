/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ctrip.framework.foundation.internals.util;

import static org.junit.Assert.*;

import static com.ctrip.framework.foundation.internals.util.ConfigUtilsTest.SystemPropertyKeys.KEY;
import static com.ctrip.framework.foundation.internals.util.ConfigUtilsTest.SystemPropertyValues.CUSTOM_VALUE;

import org.junit.After;
import org.junit.Test;

public class ConfigUtilsTest {

  @After
  public void tearDown() {
    System.clearProperty(KEY);
  }

  @Test
  public void testGetValue_null() {
    assertNull(ConfigUtils.getValue(KEY, "nothing"));
  }

  @Test
  public void testGetValue_custom() {
    System.setProperty(KEY, CUSTOM_VALUE);
    String value = ConfigUtils.getValue(KEY, "nothing");
    assertEquals(CUSTOM_VALUE, value);
  }

  protected static class SystemPropertyKeys {

    static final String KEY = "system.property.key.2020.11.28";
  }

  protected static class SystemPropertyValues {

    static final String CUSTOM_VALUE = "/simple/custom/path";
  }
}
