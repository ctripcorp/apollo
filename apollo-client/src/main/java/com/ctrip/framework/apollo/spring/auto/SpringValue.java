package com.ctrip.framework.apollo.spring.auto;

import com.ctrip.framework.apollo.util.function.Functions;
import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Spring @Value field and method common info
 *
 * @author github.com/zhegexiaohuozi  seimimaster@gmail.com
 * @since 2017/12/20.
 */
public abstract class SpringValue {
    protected Object bean;
    protected String className;
    protected String fieldName;
    protected String valKey;
    protected Function<String, ?> parser;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public abstract void updateVal(String newVal);

    protected Object parseVal(String newVal) {
        if (parser == null) {
            return newVal;
        }
        return parser.apply(newVal);
    }

    protected Function<String, ?> findParser(Class<?> targetType) {
        Function<String, ?> res = null;
        if (targetType.equals(String.class)) {
            return null;
        } else if (targetType.equals(int.class) || targetType.equals(Integer.class)) {
            res = Functions.TO_INT_FUNCTION;
        } else if (targetType.equals(long.class) || targetType.equals(Long.class)) {
            res = Functions.TO_LONG_FUNCTION;
        } else if (targetType.equals(boolean.class) || targetType.equals(Boolean.class)) {
            res = Functions.TO_BOOLEAN_FUNCTION;
        } else if (targetType.equals(Date.class)) {
            res = Functions.TO_DATE_FUNCTION;
        } else if (targetType.equals(short.class) || targetType.equals(Short.class)) {
            res = Functions.TO_SHORT_FUNCTION;
        } else if (targetType.equals(double.class) || targetType.equals(Double.class)) {
            res = Functions.TO_DOUBLE_FUNCTION;
        } else if (targetType.equals(float.class) || targetType.equals(Float.class)) {
            res = Functions.TO_FLOAT_FUNCTION;
        } else if (targetType.equals(byte.class) || targetType.equals(Byte.class)) {
            res = Functions.TO_BYTE_FUNCTION;
        }
        return res;
    }

}
