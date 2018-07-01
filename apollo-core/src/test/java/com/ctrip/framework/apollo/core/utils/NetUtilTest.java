package com.ctrip.framework.apollo.core.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by gl49 on 2018/6/8.
 */
public class NetUtilTest {
    @Test
    public void testCheckUrl(){
        String apolloUrl = "http://www.ctrip.com";
        Assert.assertEquals( NetUtil.checkUrl(apolloUrl), true);
    }

    @Test
    public void testGetValidAddress(){
        String addressStr = "http://www.ctrip.com,http://www.ctrip.com2";
        String address = NetUtil.getValidAddress(addressStr);
        Assert.assertEquals( address, "http://www.ctrip.com");
    }
}
