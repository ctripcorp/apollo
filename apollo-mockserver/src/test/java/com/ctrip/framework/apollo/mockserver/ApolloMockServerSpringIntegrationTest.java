package com.ctrip.framework.apollo.mockserver;

import static org.junit.Assert.assertEquals;

import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.mockserver.ApolloMockServerSpringIntegrationTest.TestConfiguration;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Create by zhangzheng on 8/16/18 Email:zhangzheng@youzan.com
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestConfiguration.class)
public class ApolloMockServerSpringIntegrationTest {

  private static final String otherNamespace = "otherNamespace";

  @ClassRule
  public static EmbeddedApollo embeddedApollo = new EmbeddedApollo();

  @Autowired
  private TestBean testBean;

  @Test
  @DirtiesContext
  public void testPropertyInject() {
    assertEquals("value1", testBean.key1);
    assertEquals("value2", testBean.key2);
  }

  @Test
  @DirtiesContext
  public void testListenerTriggeredByAdd() throws InterruptedException, ExecutionException, TimeoutException {
    embeddedApollo.addOrModifyProperty(otherNamespace, "someKey", "someValue");
    ConfigChangeEvent changeEvent = testBean.futureData.get(5000, TimeUnit.MILLISECONDS);
    assertEquals(otherNamespace, changeEvent.getNamespace());
    assertEquals("someValue", changeEvent.getChange("someKey").getNewValue());
  }

  @Test
  @DirtiesContext
  public void testListenerTriggeredByDel()
      throws InterruptedException, ExecutionException, TimeoutException {
    embeddedApollo.deleteProperty(otherNamespace, "key1");
    ConfigChangeEvent changeEvent = testBean.futureData.get(5000, TimeUnit.MILLISECONDS);
    assertEquals(otherNamespace, changeEvent.getNamespace());
    assertEquals(PropertyChangeType.DELETED, changeEvent.getChange("key1").getChangeType());
  }

  @EnableApolloConfig
  @Configuration
  static class TestConfiguration {

    @Bean
    public TestBean testBean() {
      return new TestBean();
    }
  }

  private static class TestBean {

    @Value("${key1:default}")
    private String key1;
    @Value("${key2:default}")
    private String key2;

    private SettableFuture<ConfigChangeEvent> futureData = SettableFuture.create();

    @ApolloConfigChangeListener(otherNamespace)
    private void onChange(ConfigChangeEvent changeEvent) {
      futureData.set(changeEvent);
    }
  }
}
