package com.ctrip.framework.apollo.core.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/5/11
 */
public class DeferredLogUtilTest{

    private final Logger logger = LoggerFactory.getLogger(DeferredLogUtilTest.class);

    @Test
    public void testMaxLogSize() {
        for (int i = 0; i < 20000; i++) {
            DeferredLogUtil.info(logger, "DeferredLogUtilTest");
        }
        Assert.assertEquals(DeferredLogUtil.logSize(), DeferredLogUtil.MAX_LOG_SIZE);
    }
}
