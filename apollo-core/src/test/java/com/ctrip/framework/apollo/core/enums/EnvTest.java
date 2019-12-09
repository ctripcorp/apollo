package com.ctrip.framework.apollo.core.enums;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class EnvTest {

    @Test
    public void exist() {
        assertEquals(false, Env.exist("xxxyyy234"));
        assertEquals(true, Env.exist("local"));
        assertEquals(true, Env.exist("dev"));
    }

    @Test
    public void addEnv() {
        String name = "someEEEE";
        assertEquals(false, Env.exist(name));
        Env.addEnv(name);
        assertEquals(true, Env.exist(name));
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOf() {
        String name = "notexist";
        assertEquals(false, Env.exist(name));
        assertEquals(Env.valueOf(name), Env.UNKNOWN);
        assertEquals(Env.valueOf("dev"), Env.DEV);
        assertEquals(Env.valueOf("UAT"), Env.UAT);
    }

    @Test
    public void testEquals() {
        assertTrue(Env.DEV.equals(Env.valueOf("dEv")));
        String name = "someEEEE";
        Env.addEnv(name);
        assertTrue(!Env.valueOf(name).equals(Env.DEV));
    }

    @Test(expected = RuntimeException.class)
    public void testEqualsWithRuntimeException()
            throws NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException {
        // get private constructor
        Constructor<Env> envConstructor = Env.class.getDeclaredConstructor(String.class);
        // make private constructor accessible
        envConstructor.setAccessible(true);
        // make a fake Env
        Env fakeDevEnv = envConstructor.newInstance(Env.DEV.toString());
        // compare, then a RuntimeException will invoke
        fakeDevEnv.equals(Env.DEV);
    }

    @Test
    public void testEqualWithoutException() {
        assertTrue(Env.DEV.equals(Env.DEV));
        assertTrue(Env.DEV.equals(Env.valueOf("dEV")));
        assertFalse(Env.PRO.equals(Env.DEV));
        assertFalse(Env.DEV.equals(Env.valueOf("uaT")));
    }

    @Test
    public void testToString() {
        assertEquals("DEV", Env.DEV.toString());
    }

    @Test
    public void name() {
        assertEquals("DEV", Env.DEV.name());
    }

    @Test
    public void getName() {
        String name = "getName";
        Env.addEnv(name);
        assertEquals(name.trim().toUpperCase(), Env.valueOf(name).toString());
    }

}