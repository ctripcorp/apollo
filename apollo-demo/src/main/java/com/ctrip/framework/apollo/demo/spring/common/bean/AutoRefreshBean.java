package com.ctrip.framework.apollo.demo.spring.common.bean;

import com.ctrip.framework.apollo.spring.annotation.EnableAutoResfresh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author tony jiang(258737400@qq.com)
 */
@Component("autoRefreshBean")
public class AutoRefreshBean {
  private static final Logger logger = LoggerFactory.getLogger(AutoRefreshBean.class);

  @Value("${stringValue}")
  @EnableAutoResfresh("application")
  private String stringValue;

  @Value("${intValue}")
  @EnableAutoResfresh
  private int intValue;

  @Value("${longValue}")
  @EnableAutoResfresh("FX.apollo")
  private long longValue;

  @Value("${shortValue}")
  @EnableAutoResfresh
  private long shortValue;

  @Value("${floatValue}")
  @EnableAutoResfresh
  private long floatValue;

  @Value("${doubleValue}")
  @EnableAutoResfresh
  private long doubleValue;

  @Value("${byteValue}")
  @EnableAutoResfresh
  private long byteValue;

  @Value("${booleanValue}")
  @EnableAutoResfresh
  private boolean booleanValue;

  @Value("${arrayValue}")
  @EnableAutoResfresh
  private String[] arrayValue;

  @Value("${dateValue}")
  @EnableAutoResfresh
  private Date dateValue;

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }

  public int getIntValue() {
    return intValue;
  }

  public void setIntValue(int intValue) {
    this.intValue = intValue;
  }

  public long getLongValue() {
    return longValue;
  }

  public void setLongValue(long longValue) {
    this.longValue = longValue;
  }

  public long getShortValue() {
    return shortValue;
  }

  public void setShortValue(long shortValue) {
    this.shortValue = shortValue;
  }

  public long getFloatValue() {
    return floatValue;
  }

  public void setFloatValue(long floatValue) {
    this.floatValue = floatValue;
  }

  public long getDoubleValue() {
    return doubleValue;
  }

  public void setDoubleValue(long doubleValue) {
    this.doubleValue = doubleValue;
  }

  public long getByteValue() {
    return byteValue;
  }

  public void setByteValue(long byteValue) {
    this.byteValue = byteValue;
  }

  public boolean isBooleanValue() {
    return booleanValue;
  }

  public void setBooleanValue(boolean booleanValue) {
    this.booleanValue = booleanValue;
  }

  public String[] getArrayValue() {
    return arrayValue;
  }

  public void setArrayValue(String[] arrayValue) {
    this.arrayValue = arrayValue;
  }

  public Date getDateValue() {
    return dateValue;
  }

  public void setDateValue(Date dateValue) {
    this.dateValue = dateValue;
  }
}
