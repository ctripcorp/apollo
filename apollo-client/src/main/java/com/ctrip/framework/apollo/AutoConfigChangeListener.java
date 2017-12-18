package com.ctrip.framework.apollo;

        import com.ctrip.framework.apollo.model.ConfigChangeEvent;

/**
 * @author tony Jiang(258737400@qq.com)
 */
public interface AutoConfigChangeListener {
    /**
     * To auto refresh the change value for the namespace.
     *
     * @param changeEvent the event for this change
     */
    public void autoChange(ConfigChangeEvent changeEvent);


}
