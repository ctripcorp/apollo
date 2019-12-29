package com.ctrip.framework.apollo.portal.environment;

import com.ctrip.framework.apollo.core.utils.ResourceUtils;
import com.ctrip.framework.apollo.portal.util.KeyValueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Only use in apollo-portal
 * load all meta server address from
 *  - System Property           [key ends with "_meta"]
 *  - OS environment variable   [key ends with "_meta"]
 *  - user's configuration file [key ends with ".meta"]
 * when apollo-portal start up.
 * @see com.ctrip.framework.apollo.core.internals.LegacyMetaServerProvider
 * @author wxq
 */
public class PortalLegacyMetaServerProvider {

    private static final Logger logger = LoggerFactory.getLogger(PortalLegacyMetaServerProvider.class);

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

    public String getMetaServerAddress(Env targetEnv) {
        String metaServerAddress = domains.get(targetEnv);
        return metaServerAddress == null ? null : metaServerAddress.trim();
    }

}
