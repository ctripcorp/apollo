package com.ctrip.framework.apollo.portal.environment;

import com.ctrip.framework.apollo.core.utils.ResourceUtils;
import com.ctrip.framework.apollo.portal.util.KeyValueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Only use in apollo-portal
 * load all meta server address from
 *  - System Property           [key ends with "_meta" (case insensitive)]
 *  - OS environment variable   [key ends with "_meta" (case insensitive)]
 *  - user's configuration file [key ends with ".meta" (case insensitive)]
 * when apollo-portal start up.
 * @see com.ctrip.framework.apollo.core.internals.LegacyMetaServerProvider
 * @author wxq
 */
public class PortalMetaServerProvider {

    private static final Logger logger = LoggerFactory.getLogger(PortalMetaServerProvider.class);

    /**
     * environments and their meta server address
     * properties file path
     */
    private static final String APOLLO_ENV_PROPERTIES_FILE_PATH = "apollo-env.properties";

    // thread safe
    private static volatile Map<Env, String> domains = initializeDomains();

    public PortalMetaServerProvider() {

    }

    /**
     * load all environment's meta address dynamically when this class loaded by JVM
     */
    private static Map<Env, String> initializeDomains() {
        // find key-value from System Property which key ends with "_meta" (case insensitive)
        Map<String, String> metaServerAddressesFromSystemProperty = KeyValueUtils.filterWithKeyIgnoreCaseEndsWith(System.getProperties(), "_meta");
        // remove key's suffix "_meta" (case insensitive)
        metaServerAddressesFromSystemProperty = KeyValueUtils.removeKeySuffix(metaServerAddressesFromSystemProperty, "_meta".length());

        // find key-value from OS environment variable which key ends with "_meta" (case insensitive)
        Map<String, String> metaServerAddressesFromOSEnvironment = KeyValueUtils.filterWithKeyIgnoreCaseEndsWith(System.getenv(), "_meta");
        // remove key's suffix "_meta" (case insensitive)
        metaServerAddressesFromOSEnvironment = KeyValueUtils.removeKeySuffix(metaServerAddressesFromOSEnvironment, "_meta".length());

        // find key-value from properties file which key ends with ".meta" (case insensitive)
        Properties properties = new Properties();
        properties = ResourceUtils.readConfigFile(APOLLO_ENV_PROPERTIES_FILE_PATH, properties);
        Map<String, String> metaServerAddressesFromPropertiesFile = KeyValueUtils.filterWithKeyIgnoreCaseEndsWith(properties, ".meta");
        // remove key's suffix ".meta" (case insensitive)
        metaServerAddressesFromPropertiesFile = KeyValueUtils.removeKeySuffix(metaServerAddressesFromPropertiesFile, ".meta".length());

        // begin to add key-value, key is environment, value is meta server address matched
        Map<String, String> metaServerAddresses = new HashMap<>();
        // lower priority add first
        metaServerAddresses.putAll(metaServerAddressesFromPropertiesFile);
        metaServerAddresses.putAll(metaServerAddressesFromOSEnvironment);
        metaServerAddresses.putAll(metaServerAddressesFromSystemProperty);

        // add to domain
        Map<Env, String> map = new ConcurrentHashMap<>();
        for(Map.Entry<String, String> entry : metaServerAddresses.entrySet()) {
            // add new environment
            Env env = Env.addEnvironment(entry.getKey());
            // get meta server address value
            String value = entry.getValue();
            // put pair (Env, meta server address)
            map.put(env, value);
        }

        // log all
        logger.info("All environment's meta server address: {}", map);
        return map;
    }

    /**
     * reload all
     * environments and meta server addresses
     */
    public static void reloadAll() {
        domains = initializeDomains();
    }

    public static String getMetaServerAddress(Env targetEnv) {
        String metaServerAddress = domains.get(targetEnv);
        return metaServerAddress == null ? null : metaServerAddress.trim();
    }

    /**
     * add a environment's meta server address
     * for the feature: add self-define environment in the web ui
     * @param env
     * @param metaServerAddress
     */
    public static void addMetaServerAddress(Env env, String metaServerAddress) {
        domains.put(env, metaServerAddress);
    }

    /**
     * delete the meta server address of the environment
     * @param env
     */
    public static void deleteMetaServerAddress(Env env) {
        domains.remove(env);
    }

    /**
     * update the meta server address of the environment
     * @param env
     * @param metaServerAddress
     */
    public static void updateMetaServerAddress(Env env, String metaServerAddress) {
        domains.put(env, metaServerAddress);
    }

    /**
     * clear all environments and meta server addresses saved
     */
    public static void clear() {
        domains.clear();
    }

}
