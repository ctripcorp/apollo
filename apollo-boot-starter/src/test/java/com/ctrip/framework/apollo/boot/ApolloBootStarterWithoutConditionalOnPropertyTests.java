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
@SpringBootTest(classes = {ApolloTestConfigurationWithoutConditionalOnProperty.class})
public class ApolloBootStarterWithoutConditionalOnPropertyTests {

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("app.id", "1"); // C:\opt\data\1\config-cache\1+default+application.properties文件内容：apollo.test.testBean=true
        System.setProperty("spring.profiles.active", "local");
        System.setProperty("env", "local");
    }

    @Autowired
    private TestBean testBean;

    @Test
    public void testSuccess() {
        Assert.notNull(testBean, "testBean is not null");
        Assert.isTrue(testBean.execute(), "testBean.execute success");
    }

}
