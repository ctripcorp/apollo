package com.ctrip.framework.foundation.spi.provider;

import java.io.InputStream;

public interface TimeoutProvider extends Provider{
    /**
     * @return the server side's long polling timeout
     */
    public String getTimeout();

    /**
     * @return whether the server side's long polling timeout is set or not
     */
    public boolean isTimeoutSet();

    /**
     * Initialize the the server side's long polling timeout provider with the specified input stream
     */
    public void initialize(InputStream in);
}
