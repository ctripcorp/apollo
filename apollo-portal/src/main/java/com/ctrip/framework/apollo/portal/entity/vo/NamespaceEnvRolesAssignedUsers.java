package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.portal.environment.Env;

public class NamespaceEnvRolesAssignedUsers extends NamespaceRolesAssignedUsers {
    private String env;

    public Env getEnv() {
        return Env.valueOf(env);
    }

    public void setEnv(Env env) {
        this.env = env.toString();
    }
}
