package com.ctrip.framework.apollo.spring.auto;

import java.lang.reflect.Field;

/**
 * Spring @Value field info
 * @author github.com/zhegexiaohuozi  seimimaster@gmail.com
 * @since 2018/2/6.
 */
public class SpringFieldValue extends SpringValue {
    private Field field;

    private SpringFieldValue(String key, Object ins, Field field) {
        super();
        this.bean = ins;
        this.className = ins.getClass().getName();
        this.fieldName = field.getName();
        this.field = field;
        this.parser = findParser(field.getType());
        this.valKey = key;
    }

    public static SpringFieldValue create(String key, Object ins, Field field) {
        return new SpringFieldValue(key, ins, field);
    }

    @Override
    public void updateVal(String newVal) {
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(bean, parseVal(newVal));
            field.setAccessible(accessible);
            logger.info("auto update apollo changed value, key={}, newVal={} in {}.{}", valKey, newVal, className, fieldName);
        } catch (Exception e) {
            logger.error("update field {}.{} fail with new val={},key = {}, msg = {}", className, fieldName, newVal, valKey, e.getMessage());
        }
    }
}
