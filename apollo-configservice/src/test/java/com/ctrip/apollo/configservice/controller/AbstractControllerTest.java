package com.ctrip.apollo.configservice.controller;

import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ctrip.apollo.ConfigServiceTestConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ConfigServiceTestConfiguration.class)
@WebIntegrationTest(randomPort = true)
public abstract class AbstractControllerTest {

}
