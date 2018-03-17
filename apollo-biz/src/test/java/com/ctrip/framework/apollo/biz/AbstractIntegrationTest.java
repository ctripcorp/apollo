package com.ctrip.framework.apollo.biz;

import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@Rollback
@Transactional
@WebIntegrationTest(randomPort = true)
@SpringApplicationConfiguration(classes = BizTestConfiguration.class)
public abstract class AbstractIntegrationTest {

}
