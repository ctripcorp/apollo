package com.ctrip.framework.apollo.core.constants;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvTest {

    @Test
    public void valueOf() {
        assertEquals(Env.DEV, Env.valueOf(Env.DEV));
        assertEquals(Env.FAT, Env.valueOf(Env.FAT.toLowerCase()));
        assertEquals(Env.UAT, Env.valueOf(" " + Env.UAT.toUpperCase() + ""));
        assertEquals(Env.UNKNOWN, Env.valueOf("   "));
    }

    @Test
    public void fromString() {
        assertEquals(Env.DEV, Env.fromString(Env.DEV));
        assertEquals(Env.FAT, Env.fromString(Env.FAT.toLowerCase()));
        assertEquals(Env.UAT, Env.fromString(" " + Env.UAT.toUpperCase() + ""));
    }
}