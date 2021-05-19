package com.ctrip.framework.apollo.core.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/5/11
 */
public class DeferredLogCacheTest {

    private final Logger logger = LoggerFactory.getLogger(DeferredLogCacheTest.class);

    @Test
    public void testMaxLogSize() {
        for (int i = 0; i < 20000; i++) {
            DeferredLogCache.info(logger, "DeferredLogUtilTest");
        }
        Assert.assertEquals(DeferredLogCache.logSize(), DeferredLogCache.MAX_LOG_SIZE);
    }
}
