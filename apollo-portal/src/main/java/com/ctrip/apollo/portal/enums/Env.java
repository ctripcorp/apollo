package com.ctrip.apollo.portal.enums;

public enum Env {

    DEV("dev"), FWS("fws"), FAT("fat"), UAT("uat"), LPT("lpt"), PROD("prod"), TOOLS("tools"), UN_KNOW("");

    private String value;

    Env(String value) {
        this.value = value;
    }

    public static Env valueFrom(String env) {
        if (env == null || "".equals(env)) {
            return UN_KNOW;
        } else if ("dev".equals(env)) {
            return DEV;
        } else if ("fws".equals(env)) {
            return FWS;
        } else if ("fat".equals(env)) {
            return FAT;
        } else if ("uat".equals(env)) {
            return UAT;
        } else if ("prod".equals(env)) {
            return PROD;
        } else if ("tools".equals(env)) {
            return TOOLS;
        } else{
            return UN_KNOW;
        }
    }
}
