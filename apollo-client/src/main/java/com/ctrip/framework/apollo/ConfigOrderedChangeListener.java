package com.ctrip.framework.apollo;

public interface ConfigOrderedChangeListener extends ConfigChangeListener {

    int order();
}
