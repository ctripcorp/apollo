package com.ctrip.framework.apollo.biz.utils;

import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.biz.MockBeanFactory;
import com.ctrip.framework.apollo.biz.entity.Namespace;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ReleaseKeyGeneratorTest {
  private static final Logger logger = LoggerFactory.getLogger(ReleaseKeyGeneratorTest.class);
  @Test
  public void testGenerateReleaseKey() throws Exception {
    String someAppId = "someAppId";
    String someCluster = "someCluster";
    String someNamespace = "someNamespace";

    String anotherAppId = "anotherAppId";

    Namespace namespace = MockBeanFactory.mockNamespace(someAppId, someCluster, someNamespace);
    Namespace anotherNamespace = MockBeanFactory.mockNamespace(anotherAppId, someCluster, someNamespace);
    int generateTimes = 50000;
    Set<String> releaseKeys = Sets.newConcurrentHashSet();

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(1);

    executorService.submit(generateReleaseKeysTask(namespace, releaseKeys, generateTimes, latch));
    executorService.submit(generateReleaseKeysTask(anotherNamespace, releaseKeys, generateTimes, latch));

    latch.countDown();

    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS);

    //make sure keys are unique
    assertEquals(generateTimes * 2, releaseKeys.size());
  }

  private Runnable generateReleaseKeysTask(Namespace namespace, Set<String> releaseKeys,
                                   int generateTimes, CountDownLatch latch) {
    return () -> {
      try {
        latch.await();
      } catch (InterruptedException e) {
        //ignore
      }
      for (int i = 0; i < generateTimes; i++) {
        releaseKeys.add(ReleaseKeyGenerator.generateReleaseKey(namespace));
      }
    };
  }

}
