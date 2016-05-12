package com.ctrip.apollo.portal;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    ConfigServiceTest.class, PropertyResolverTest.class,
    NamespaceServiceTest.class
})
public class AllTests {

}
