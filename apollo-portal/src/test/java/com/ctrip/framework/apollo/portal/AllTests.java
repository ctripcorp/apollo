package com.ctrip.framework.apollo.portal;


import com.ctrip.framework.apollo.openapi.filter.ConsumerAuthenticationFilterTest;
import com.ctrip.framework.apollo.openapi.service.ConsumerRolePermissionServiceTest;
import com.ctrip.framework.apollo.openapi.service.ConsumerServiceTest;
import com.ctrip.framework.apollo.openapi.util.ConsumerAuditUtilTest;
import com.ctrip.framework.apollo.openapi.util.ConsumerAuthUtilTest;
import com.ctrip.framework.apollo.portal.component.txtresolver.FileTextResolverTest;
import com.ctrip.framework.apollo.portal.component.txtresolver.PropertyResolverTest;
import com.ctrip.framework.apollo.portal.config.ConfigTest;
import com.ctrip.framework.apollo.portal.service.AppNamespaceServiceTest;
import com.ctrip.framework.apollo.portal.service.ConfigServiceTest;
import com.ctrip.framework.apollo.portal.service.FavoriteServiceTest;
import com.ctrip.framework.apollo.portal.service.NamespaceServiceTest;
import com.ctrip.framework.apollo.portal.spi.defaultImpl.RoleInitializationServiceTest;
import com.ctrip.framework.apollo.portal.spi.defaultImpl.RolePermissionServiceTest;
import com.ctrip.framework.apollo.portal.spi.ctrip.CtripUserServiceTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    ConfigServiceTest.class, PropertyResolverTest.class,
    NamespaceServiceTest.class, ServiceExceptionTest.class, RolePermissionServiceTest.class,
    AppNamespaceServiceTest.class, RoleInitializationServiceTest.class, FileTextResolverTest.class,
    RetryableRestTemplateTest.class, ConsumerRolePermissionServiceTest.class,
    ConsumerAuthenticationFilterTest.class, ConsumerAuthUtilTest.class, ConsumerServiceTest.class,
    ConsumerAuditUtilTest.class, ConfigTest.class, FavoriteServiceTest.class,
    CtripUserServiceTest.class

})
public class AllTests {

}
