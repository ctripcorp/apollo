package com.ctrip.framework.apollo.demo.spring.common.bean;

import com.ctrip.framework.apollo.spring.annotation.EnableAutoResfresh;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author tony jiang(258737400@qq.com)
 */
@Component("autoRefreshBean")
public class AutoRefreshBean {

    @Value("${intValue:0}")
    @EnableAutoResfresh
    private int intValue;

    @Value("${svalue:}")
    @EnableAutoResfresh("application")
    private String svalue;

    @Value("${longValue:0}")
    @EnableAutoResfresh("FX.apollo")
    private long longValue;

    @Value("${shortValue:0}")
    @EnableAutoResfresh
    private short shortValue;

    @Value("${floatValue:0}")
    @EnableAutoResfresh
    private float floatValue;

    @Value("${doubleValue:0}")
    @EnableAutoResfresh
    private double doubleValue;

    @Value("${byteValue:0}")
    @EnableAutoResfresh
    private byte byteValue;

    @Value("${booleanValue:false}")
    @EnableAutoResfresh
    private boolean booleanValue;

    public int getIntValue() {
        return intValue;
    }

    public String getSvalue() {
        return svalue;
    }

    public long getLongValue() {
        return longValue;
    }

    public short getShortValue() {
        return shortValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public byte getByteValue() {
        return byteValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }
}
