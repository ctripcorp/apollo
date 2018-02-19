package com.ctrip.framework.apollo.spring.auto;

import java.lang.reflect.Method;

/**
 * Spring @Value method info
 * @author github.com/zhegexiaohuozi  seimimaster@gmail.com
 * @since 2018/2/6.
 */
public class SpringMethodValue extends SpringValue {
    private Method method;

    private SpringMethodValue(String key, Object ins, Method method) {
        this.bean = ins;
        this.method = method;
        this.className = ins.getClass().getName();
        this.fieldName = method.getName() + "(*)";
        Class<?>[] paramTps = method.getParameterTypes();
        if (paramTps.length != 1) {
            logger.error("invalid setter,can not update in {}.{}", className, fieldName);
            return;
        }
        this.parser = findParser(paramTps[0]);
        this.valKey = key;
    }

    public static SpringMethodValue create(String key, Object ins, Method method) {
        return new SpringMethodValue(key, ins, method);
    }

    @Override
    public void updateVal(String newVal) {
        try {
            Class<?>[] paramTps = method.getParameterTypes();
            if (paramTps.length != 1) {
                logger.error("invalid setter ,can not update key={} val={} in {}.{}", valKey, newVal, className, fieldName);
                return;
            }
            method.invoke(bean, parseVal(newVal));
            logger.info("auto update apollo changed value, key={}, newVal={} in {}.{}", valKey, newVal, className, fieldName);
        } catch (Exception e) {
            logger.error("update field {}.{} fail with new val={},key = {}, msg = {}", className, fieldName, newVal, valKey, e.getMessage());
        }
    }
}
