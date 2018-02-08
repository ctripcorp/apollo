package com.ctrip.framework.apollo.spring.auto;

import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.util.function.Functions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SpringValue Tester.
 *
 * @author github.com/zhegexiaohuozi  seimimaster@gmail.com
 * @version 1.0
 */
public class SpringValueTest {

    private SpringValue defaultVal;
    private ConfigChange testTarget;
    //SpringValueProcessor.pattern
    private Pattern pattern = Pattern.compile("\\$\\{([^:]*)\\}:?(.*)");

    @Before
    public void before() throws Exception {
        Field field = ConfigChange.class.getDeclaredField("newValue");
        field.setAccessible(true);
        testTarget = new ConfigChange("test","test","testO","testN", PropertyChangeType.MODIFIED);
        defaultVal = SpringFieldValue.create("test",testTarget,field);
    }

    /**
     * Method: updateVal(String newVal)
     */
    @Test
    public void testUpdateVal() throws Exception {
        defaultVal.updateVal("testUp");
        Assert.assertEquals("testUp",testTarget.getNewValue());
    }

    /**
     * Method: findParser(Class<?> targetType)
     */
    @Test
    public void testFindParser() throws Exception {
        Method findParser = SpringValue.class.getDeclaredMethod("findParser",Class.class);
        findParser.setAccessible(true);

        Assert.assertNull(findParser.invoke(defaultVal,String.class));
        Assert.assertEquals(Functions.TO_INT_FUNCTION,findParser.invoke(defaultVal,int.class));
        Assert.assertEquals(Functions.TO_INT_FUNCTION,findParser.invoke(defaultVal,Integer.class));
        Assert.assertEquals(Functions.TO_LONG_FUNCTION,findParser.invoke(defaultVal,long.class));
        Assert.assertEquals(Functions.TO_LONG_FUNCTION,findParser.invoke(defaultVal,Long.class));
        Assert.assertEquals(Functions.TO_DOUBLE_FUNCTION,findParser.invoke(defaultVal,double.class));
        Assert.assertEquals(Functions.TO_DOUBLE_FUNCTION,findParser.invoke(defaultVal,Double.class));
        Assert.assertEquals(Functions.TO_FLOAT_FUNCTION,findParser.invoke(defaultVal,float.class));
        Assert.assertEquals(Functions.TO_FLOAT_FUNCTION,findParser.invoke(defaultVal,Float.class));
        Assert.assertEquals(Functions.TO_BYTE_FUNCTION,findParser.invoke(defaultVal,byte.class));
        Assert.assertEquals(Functions.TO_BYTE_FUNCTION,findParser.invoke(defaultVal,Byte.class));
        Assert.assertEquals(Functions.TO_BOOLEAN_FUNCTION,findParser.invoke(defaultVal,boolean.class));
        Assert.assertEquals(Functions.TO_BOOLEAN_FUNCTION,findParser.invoke(defaultVal,Boolean.class));
        Assert.assertEquals(Functions.TO_SHORT_FUNCTION,findParser.invoke(defaultVal,short.class));
        Assert.assertEquals(Functions.TO_SHORT_FUNCTION,findParser.invoke(defaultVal,Short.class));
        Assert.assertEquals(Functions.TO_DATE_FUNCTION,findParser.invoke(defaultVal,Date.class));

    }

    @Test
    public void testPattern(){
        String valP = "${some.timeout:5000}";
        Matcher matcher = pattern.matcher(valP);
        if (matcher.matches()) {
            String key = matcher.group(1);
            Assert.assertEquals("some.timeout",key);
        }
    }

} 
