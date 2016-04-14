package com.ctrip.apollo.adminservice;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ctrip.apollo.adminservice.controller.AppControllerTest;
import com.ctrip.apollo.adminservice.controller.ItemSetControllerTest;
import com.ctrip.apollo.adminservice.controller.ReleaseControllerTest;

@RunWith(Suite.class)
@SuiteClasses({AppControllerTest.class, ReleaseControllerTest.class, ItemSetControllerTest.class})
public class AllTests {

}
