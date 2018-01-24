package com.ctrip.framework.apollo.boot;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.ctrip.framework.apollo.boot.bean.TestBean;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApolloTestConfigurationWithConditionalOnProperty.class})
public class ApolloBootStarterWithConditionalOnPropertyTests_Failed {

    @BeforeClass
    public static void beforeClass() {
//        System.setProperty("app.id", "1"); app.id not set
        System.setProperty("spring.profiles.active", "local");
        System.setProperty("env", "local");
    }

    @Autowired(required = false)
    private TestBean testBean;

    @Test
    public void testWithAppIdNotSet() {
        Assert.isNull(testBean, "testBean is not null");
    }

}
