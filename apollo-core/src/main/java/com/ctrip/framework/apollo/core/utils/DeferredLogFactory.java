package com.ctrip.framework.apollo.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/5/19
 */
public class DeferredLogFactory {

    private DeferredLogFactory(){}

    public static Logger getLogger(Class<?> clazz){
        Logger logger = LoggerFactory.getLogger(clazz);
        return new DeferredLog(logger);
    }
}
