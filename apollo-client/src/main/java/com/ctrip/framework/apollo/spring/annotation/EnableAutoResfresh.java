package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.core.ConfigConsts;

import java.lang.annotation.*;

/**
 * Use this annotation to auto refresh Apollo property .
 *
 * <p>Configuration example:</p>
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAutoResfresh({"someNamespace"})
 * private int timeout;
 * </pre>
 *
 * @author Tony Jiang(258737400@qq.com)
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableAutoResfresh {
  /**
   * Apollo namespace for the config, if not specified then default to application
   */
  String value() default ConfigConsts.NAMESPACE_APPLICATION;
}
