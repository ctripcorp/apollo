package com.ctrip.framework.apollo.configservice;

import com.ctrip.framework.apollo.configservice.controller.ConfigControllerTest;
import com.ctrip.framework.apollo.configservice.controller.ConfigFileControllerTest;
import com.ctrip.framework.apollo.configservice.controller.NotificationControllerTest;
import com.ctrip.framework.apollo.configservice.controller.NotificationControllerV2Test;
import com.ctrip.framework.apollo.configservice.integration.ConfigControllerIntegrationTest;
import com.ctrip.framework.apollo.configservice.integration.ConfigFileControllerIntegrationTest;
import com.ctrip.framework.apollo.configservice.integration.NotificationControllerIntegrationTest;
import com.ctrip.framework.apollo.configservice.integration.NotificationControllerV2IntegrationTest;
import com.ctrip.framework.apollo.configservice.service.AppNamespaceServiceWithCacheTest;
import com.ctrip.framework.apollo.configservice.service.ReleaseMessageServiceWithCacheTest;
import com.ctrip.framework.apollo.configservice.util.InstanceConfigAuditUtilTest;
import com.ctrip.framework.apollo.configservice.util.NamespaceUtilTest;
import com.ctrip.framework.apollo.configservice.util.WatchKeysUtilTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ConfigControllerTest.class, NotificationControllerTest.class,
    ConfigControllerIntegrationTest.class, NotificationControllerIntegrationTest.class,
    NamespaceUtilTest.class, ConfigFileControllerTest.class,
    ConfigFileControllerIntegrationTest.class, WatchKeysUtilTest.class,
    NotificationControllerV2Test.class, NotificationControllerV2IntegrationTest.class,
    InstanceConfigAuditUtilTest.class, AppNamespaceServiceWithCacheTest.class,
    ReleaseMessageServiceWithCacheTest.class
})
public class AllTests {

}
