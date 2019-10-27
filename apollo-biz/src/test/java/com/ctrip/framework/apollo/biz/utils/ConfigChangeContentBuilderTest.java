package com.ctrip.framework.apollo.biz.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.ctrip.framework.apollo.biz.MockBeanFactory;
import com.ctrip.framework.apollo.biz.entity.Item;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jian.tan
 */

public class ConfigChangeContentBuilderTest {

  private final ConfigChangeContentBuilder configChangeContentBuilder = new ConfigChangeContentBuilder();
  private String configString;

  @Before
  public void initConfig() {

    Item createdItem = MockBeanFactory.mockItem(1, 1, "timeout", "100", 1);
    Item updatedItem = MockBeanFactory.mockItem(1, 1, "timeout", "1001", 1);

    configChangeContentBuilder.createItem(createdItem);
    configChangeContentBuilder.updateItem(createdItem, updatedItem);
    configChangeContentBuilder.deleteItem(updatedItem);

    configString = configChangeContentBuilder.build();
  }

  @Test
  public void testHasContent() {
    assertTrue(configChangeContentBuilder.hasContent());
  }

  @Test
  public void testConvertJsonString() {
    ConfigChangeContentBuilder contentBuilder = ConfigChangeContentBuilder
        .convertJsonString(configString);

    assertNotNull(contentBuilder.getCreateItems());
    assertNotNull(contentBuilder.getUpdateItems().get(0).oldItem);
    assertNotNull(contentBuilder.getUpdateItems().get(0).newItem);
    assertNotNull(contentBuilder.getDeleteItems());
  }

}
