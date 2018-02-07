package com.ctrip.framework.apollo.spring.auto;

import com.ctrip.framework.apollo.util.function.Functions;
import com.google.common.base.Function;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Date;

/**
 * Spring @Value or @ApolloValue field or method info
 * @author github.com/zhegexiaohuozi [wanghaomiao@huli.com]
 * @since 2017/12/20.
 */
public class SpringValue {
    private static Gson gson = new Gson();

    private Object bean;
    private Field field;
    private Method method;
    private boolean isField = false;
    private String className;
    private String fieldName;
    private Function<String, ?> parser;
    private Type genericType;
    private Class type;

    private Logger logger = LoggerFactory.getLogger(SpringValue.class);

    private SpringValue(Object ins, Field field) {
        this.bean = ins;
        this.className = ins.getClass().getName();
        this.fieldName = field.getName();
        this.field = field;
        this.isField = true;
        this.genericType = field.getGenericType();
        this.type = field.getType();
        this.parser = findParser(field.getType());
    }

    private SpringValue(Object ins, Method method) {
        this.bean = ins;
        this.method = method;
        this.className = ins.getClass().getName();
        this.fieldName = method.getName() + "(*)";
        Class<?>[] paramTps = method.getParameterTypes();
        if (paramTps.length != 1){
            logger.error("invalid setter,can not update in {}.{}",className,fieldName);
            return;
        }
        this.type = paramTps[0];
        this.genericType = method.getGenericParameterTypes()[0];
        this.parser = findParser(paramTps[0]);
    }

    public static SpringValue create(Object ins, Field field){
        return new SpringValue(ins,field);
    }

    public static SpringValue create(Object ins, Method method){
        return new SpringValue(ins,method);
    }

    public void updateVal(String newVal){
        try {
            if (isField){
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                field.set(bean,parseVal(newVal));
                field.setAccessible(accessible);
            }else {
                Class<?>[] paramTps = method.getParameterTypes();
                if (paramTps.length != 1){
                    logger.error("invalid setter ,can not update val={} in {}.{}",newVal,className,fieldName);
                    return;
                }
                method.invoke(bean,parseVal(newVal));
            }
            logger.info("auto update apollo changed value, newVal={} in {}.{}",newVal,className,fieldName);
        }catch (Exception e){
            logger.error("update field {}.{} filed with new val={},msg ={}",className,fieldName,newVal,e.getMessage());
        }
    }

    private Object parseVal(String newVal){
        if (parser == null){
            if(type.equals(String.class)){
                return newVal;
            }
            return gson.fromJson(newVal,genericType);
        }
        return parser.apply(newVal);
    }

    private Function<String, ?> findParser(Class<?> targetType) {
        Function<String, ?> res = null;
        if (targetType.equals(int.class) || targetType.equals(Integer.class)) {
            res = Functions.TO_INT_FUNCTION;
        } else if (targetType.equals(long.class) || targetType.equals(Long.class)) {
            res = Functions.TO_LONG_FUNCTION;
        }  else if (targetType.equals(boolean.class) || targetType.equals(Boolean.class)) {
            res = Functions.TO_BOOLEAN_FUNCTION;
        } else if (targetType.equals(Date.class)) {
            res = Functions.TO_DATE_FUNCTION;
        }else if (targetType.equals(short.class) || targetType.equals(Short.class)){
            res = Functions.TO_SHORT_FUNCTION;
        }else if (targetType.equals(double.class) || targetType.equals(Double.class)){
            res = Functions.TO_DOUBLE_FUNCTION;
        }else if (targetType.equals(float.class) || targetType.equals(Float.class)){
            res = Functions.TO_FLOAT_FUNCTION;
        }
        return res;
    }

}
