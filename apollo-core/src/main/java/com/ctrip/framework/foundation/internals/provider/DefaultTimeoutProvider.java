package com.ctrip.framework.foundation.internals.provider;

import com.ctrip.framework.foundation.internals.Utils;
import com.ctrip.framework.foundation.internals.io.BOMInputStream;
import com.ctrip.framework.foundation.spi.provider.Provider;
import com.ctrip.framework.foundation.spi.provider.TimeoutProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class DefaultTimeoutProvider implements TimeoutProvider {
    private static final Logger logger = LoggerFactory.getLogger(DefaultTimeoutProvider.class);
    public static final String APP_PROPERTIES_CLASSPATH = "/META-INF/app.properties";
    private Properties m_appProperties = new Properties();

    private String m_timeout;

    @Override
    public void initialize() {
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(APP_PROPERTIES_CLASSPATH);
            if (in == null) {
                in = DefaultTimeoutProvider.class.getResourceAsStream(APP_PROPERTIES_CLASSPATH);
            }

            initialize(in);
        } catch (Throwable ex) {
            logger.error("Initialize DefaultTimeoutProvider failed.", ex);
        }
    }

    @Override
    public void initialize(InputStream in) {
        try {
            if (in != null) {
                try {
                    m_appProperties.load(new InputStreamReader(new BOMInputStream(in), StandardCharsets.UTF_8));
                } finally {
                    in.close();
                }
            }

            initTimeOut();
        } catch (Throwable ex) {
            logger.error("Initialize DefaultTimeoutProvider failed.", ex);
        }
    }

    @Override
    public String getTimeout() {
        return m_timeout;
    }

    @Override
    public boolean isTimeoutSet() {
        return Utils.isBlank(m_timeout);
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        if ("timeout".equals(name)) {
            String val = getTimeout();
            return val == null ? defaultValue : val;
        } else {
            String val = m_appProperties.getProperty(name, defaultValue);
            return val == null ? defaultValue : val;
        }
    }

    @Override
    public Class<? extends Provider> getType() {
        return TimeoutProvider.class;
    }

    private void initTimeOut() {
        // 1.Get timeout from System Property
        m_timeout = System.getProperty("timeout");
        if (!Utils.isBlank(m_timeout)) {
            m_timeout = m_timeout.trim();
            logger.info("timeout is set to {} by timeout property from System Property", m_timeout);
            return;
        }

        // 2.Get timeout from OS environment variable
        m_timeout = System.getenv("TIMEOUT");
        if (!Utils.isBlank(m_timeout)) {
            m_timeout = m_timeout.trim();
            logger.info("timeout is set to {} by timeout property from OS environment variable", m_timeout);
            return;
        }

        // 3.Get timeout from app.properties.
        m_timeout = m_appProperties.getProperty("timeout");
        if (!Utils.isBlank(m_timeout)) {
            m_timeout = m_timeout.trim();
            logger.info("timeout is set to {} by timeout property from app.properties.", m_timeout, APP_PROPERTIES_CLASSPATH);
            return;
        }

        m_timeout = null;
        logger.warn("timeout is not available from System Property and {}. It is set to null", APP_PROPERTIES_CLASSPATH);
    }

    @Override
    public String toString() {
        return "timeout [" + getTimeout() + "] properties: " + m_appProperties + " (DefaultTimeoutProvider)";
    }
}
