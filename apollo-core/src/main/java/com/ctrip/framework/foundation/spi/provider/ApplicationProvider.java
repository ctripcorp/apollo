package com.ctrip.framework.foundation.spi.provider;

import java.io.InputStream;

/**
 * Provider for application related properties
 */
public interface ApplicationProvider extends Provider {
  /**
   * @return the application's app id
   */
  public String getAppId();

  /**
   * @return the application's access key secret
   */
  public String getAccessKeySecret();

  /**
   * @return whether the application's app id is set or not
   */
  public boolean isAppIdSet();

  /**
   * Initialize the application provider with the specified input stream
   */
  public void initialize(InputStream in);
}
