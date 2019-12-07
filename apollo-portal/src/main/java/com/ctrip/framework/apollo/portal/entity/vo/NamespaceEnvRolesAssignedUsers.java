package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.core.constants.Env;

public class NamespaceEnvRolesAssignedUsers extends NamespaceRolesAssignedUsers {
    private String env;

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = Env.valueOf(env);
    }
}
