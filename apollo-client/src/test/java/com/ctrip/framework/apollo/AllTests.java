package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.integration.ConfigIntegrationTest;
import com.ctrip.framework.apollo.internals.DefaultConfigManagerTest;
import com.ctrip.framework.apollo.internals.DefaultConfigTest;
import com.ctrip.framework.apollo.internals.JsonConfigFileTest;
import com.ctrip.framework.apollo.internals.LocalFileConfigRepositoryTest;
import com.ctrip.framework.apollo.internals.PropertiesConfigFileTest;
import com.ctrip.framework.apollo.internals.RemoteConfigLongPollServiceTest;
import com.ctrip.framework.apollo.internals.RemoteConfigRepositoryTest;
import com.ctrip.framework.apollo.internals.SimpleConfigTest;
import com.ctrip.framework.apollo.internals.XmlConfigFileTest;
import com.ctrip.framework.apollo.spi.DefaultConfigFactoryManagerTest;
import com.ctrip.framework.apollo.spi.DefaultConfigFactoryTest;
import com.ctrip.framework.apollo.spi.DefaultConfigRegistryTest;
import com.ctrip.framework.apollo.spring.BootstrapConfigTest;
import com.ctrip.framework.apollo.spring.JavaConfigAnnotationTest;
import com.ctrip.framework.apollo.spring.JavaConfigPlaceholderAutoUpdateTest;
import com.ctrip.framework.apollo.spring.JavaConfigPlaceholderTest;
import com.ctrip.framework.apollo.spring.XMLConfigAnnotationTest;
import com.ctrip.framework.apollo.spring.XmlConfigPlaceholderAutoUpdateTest;
import com.ctrip.framework.apollo.spring.XmlConfigPlaceholderTest;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceTest;
import com.ctrip.framework.apollo.spring.property.PlaceholderHelperTest;
import com.ctrip.framework.apollo.util.ConfigUtilTest;
import com.ctrip.framework.apollo.util.ExceptionUtilTest;
import com.ctrip.framework.apollo.util.parser.DateParserTest;
import com.ctrip.framework.apollo.util.parser.DurationParserTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    ConfigServiceTest.class, DefaultConfigRegistryTest.class, DefaultConfigFactoryManagerTest.class,
    DefaultConfigManagerTest.class, DefaultConfigTest.class, LocalFileConfigRepositoryTest.class,
    RemoteConfigRepositoryTest.class, SimpleConfigTest.class, DefaultConfigFactoryTest.class,
    ConfigIntegrationTest.class, ExceptionUtilTest.class, XmlConfigFileTest.class,
    PropertiesConfigFileTest.class, RemoteConfigLongPollServiceTest.class, DateParserTest.class,
    DurationParserTest.class, JsonConfigFileTest.class, XmlConfigPlaceholderTest.class,
    JavaConfigPlaceholderTest.class, XMLConfigAnnotationTest.class, JavaConfigAnnotationTest.class,
    ConfigUtilTest.class, BootstrapConfigTest.class, JavaConfigPlaceholderAutoUpdateTest.class,
    XmlConfigPlaceholderAutoUpdateTest.class, ConfigPropertySourceTest.class,
    PlaceholderHelperTest.class
})
public class AllTests {

}
