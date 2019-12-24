package com.ctrip.framework.apollo.portal.environment;

import com.ctrip.framework.apollo.core.utils.ResourceUtils;
import com.ctrip.framework.apollo.portal.util.KeyValueUtils;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * a fake class to replace com.ctrip.framework.apollo.core.internals.LegacyMetaServerProvider in apollo-portal
 * @see com.ctrip.framework.apollo.core.internals.LegacyMetaServerProvider
 * @author wxq
 */
public class PortalLegacyMetaServerProvider implements PortalMetaServerProvider {

    private static final Logger logger = LoggerFactory.getLogger(PortalLegacyMetaServerProvider.class);

    // make it as lowest as possible, yet not the lowest
    public static final int ORDER = PortalMetaServerProvider.LOWEST_PRECEDENCE - 1;
    private static final Map<Env, String> domains = new HashMap<>();

    public PortalLegacyMetaServerProvider() {
        initialize();
    }

    /**
     * load all environment's meta address dynamically
     */
    private void initialize() {
        // find key-value from System Property which key ends with "_meta"
        Map<String, String> metaServerAddressesFromSystemProperty = KeyValueUtils.filterWithKeyEndswith(System.getProperties(), "_meta");
        // remove key's suffix "_meta"
        metaServerAddressesFromSystemProperty = KeyValueUtils.removeKeySuffix(metaServerAddressesFromSystemProperty, "_meta".length());

        // find key-value from OS environment variable which key ends with "_meta"
        Map<String, String> metaServerAddressesFromOSEnvironment = KeyValueUtils.filterWithKeyEndswith(System.getenv(), "_meta");
        // remove key's suffix "_meta"
        metaServerAddressesFromOSEnvironment = KeyValueUtils.removeKeySuffix(metaServerAddressesFromOSEnvironment, "_meta".length());

        // find key-value from properties file which key ends with ".meta"
        Properties properties = new Properties();
        properties = ResourceUtils.readConfigFile("apollo-env.properties", properties);
        Map<String, String> metaServerAddressesFromPropertiesFile = KeyValueUtils.filterWithKeyEndswith(properties, ".meta");
        // remove key's suffix ".meta"
        metaServerAddressesFromPropertiesFile = KeyValueUtils.removeKeySuffix(metaServerAddressesFromPropertiesFile, ".meta".length());

        // begin to add key-value, key is environment, value is meta server address matched
        Map<String, String> metaServerAddresses = new HashMap<>();
        // lower priority add first
        metaServerAddresses.putAll(metaServerAddressesFromPropertiesFile);
        metaServerAddresses.putAll(metaServerAddressesFromOSEnvironment);
        metaServerAddresses.putAll(metaServerAddressesFromSystemProperty);

        // add to domain
        for(Map.Entry<String, String> entry : metaServerAddresses.entrySet()) {
            // add new environment
            Env env = Env.addEnvironment(entry.getKey());
            // get meta server address value
            String value = entry.getValue();
            // put pair (Env, meta server address)
            domains.put(env, value);
        }

        // log all
        logger.info("All environment's meta server address: {}", domains);
    }

    private String getMetaServerAddress(Properties prop, String sourceName, String propName) {
        // 1. Get from System Property.
        String metaAddress = System.getProperty(sourceName);
        if (Strings.isNullOrEmpty(metaAddress)) {
            // 2. Get from OS environment variable, which could not contain dot and is normally in UPPER case,like DEV_META.
            metaAddress = System.getenv(sourceName.toUpperCase());
        }
        if (Strings.isNullOrEmpty(metaAddress)) {
            // 3. Get from properties file.
            metaAddress = prop.getProperty(propName);
        }
        return metaAddress;
    }

    @Override
    public String getMetaServerAddress(Env targetEnv) {
        String metaServerAddress = domains.get(targetEnv);
        return metaServerAddress == null ? null : metaServerAddress.trim();
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

}
