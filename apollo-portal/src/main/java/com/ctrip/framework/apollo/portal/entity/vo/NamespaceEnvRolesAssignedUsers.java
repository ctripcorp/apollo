package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.core.enums.Env;

public class NamespaceEnvRolesAssignedUsers extends NamespaceRolesAssignedUsers {
    private Env env;

    public Env getEnv() {
        return env;
    }

    public void setEnv(Env env) {
        this.env = env;
    }
}
