package com.ctrip.framework.apollo.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.ctrip.framework.apollo.spring.processor.ValueMappingAntMatchCollector;
import com.ctrip.framework.apollo.spring.processor.ValueMappingCollector;
import com.ctrip.framework.apollo.spring.processor.ValueMappingJsonParser;
import com.ctrip.framework.apollo.spring.processor.ValueMappingParser;
import com.ctrip.framework.apollo.spring.processor.ValueMappingRegMatchCollector;
import com.ctrip.framework.apollo.spring.processor.ValueMappingXmlBeanParser;
import com.google.gson.Gson;


/**
 ** Annotation at the field or method parameter level that indicates a property key for the affected
 * argument. The mapped property value will be converted to a value with the type of argument by the
 * specified parser, and always be able to update automatically. It can also collect multiple
 * properties to convert to a value of Collection or Array if specify a collector.
 * 
 * <pre>
 *   Usage example:
 *
 *    &#064;Component
 *    &#064;EnableApolloConfig("someNamespace")
 *    public class CustomConfig {
 *    
 *      // Support static field, and update it automatically
 *      &#064;ValueMapping("${customName:zxh}")
 *      private static String customName;
 *      
 *      // Mapping JSON value of Map
 *      &#064;ValueMapping("${key.userMapStr:{\"userName\":\"xxxx@123.com\",\"password\":\"zxh@123.com\"}}")
 *      private Map&lt;String, Object&gt; userMap;
 *      
 *      // Mapping JSON value of List
 *      &#064;ValueMapping("${key.ipListStr:[\"10.11.10.11\",\"127.0.0.1\"]}")
 *      private List&lt;String&gt; ipList;
 *      
 *      // Mapping XML value of Spring Bean
 *      &#064;ValueMapping(value = "${userXmlStr:&lt;?xml version=\"1.0\" encoding=\"UTF-8\" ?&gt;\n" + 
 *          "&lt;beans &gt;\n" + 
 *          "    &lt;bean id=\"user\" class=\"com.ctrip.framework.apollo.spring.processor.ValueMappingProcessorTest$User\"&gt;\n" + 
 *          "        &lt;property name=\"userName\"&gt;\n" + 
 *          "            &lt;value&gt;yx@123.com&lt;/value&gt;\n" + 
 *          "        &lt;/property&gt;\n" + 
 *          "        &lt;property name=\"mobiles\"&gt;\n" + 
 *          "            &lt;list&gt;\n" + 
 *          "               &lt;value&gt;12345678901&lt;/value&gt;\n" + 
 *          "            &lt;/list&gt;\n" + 
 *          "        &lt;/property&gt;\n" + 
 *          "        &lt;property name=\"favors\"&gt;\n" + 
 *          "            &lt;map&gt;\n" + 
 *          "               &lt;entry key=\"color\" value=\"blue\"/&gt;\n" + 
 *          "               &lt;entry key=\"number\" value=\"13579\"/&gt;\n" + 
 *          "            &lt;/map&gt;\n" + 
 *          "        &lt;/property&gt;\n" + 
 *          "    &lt;/bean&gt;\n" + 
 *          "&lt;/beans&gt;}", parser = ValueMappingXmlBeanParser.class)
 *      private User xmlUser;
 *     
 *      // Collect property whose key matches the ant path pattern specified in the placeholder.
 *      // Only collect property from the someNamespace that EnableApolloConfig specified, otherwise it will collect from all config namespaces.
 *      // Not support to define default value, which may create ambiguous to the parser that targets at the type of collection element.
 *      &#064;ValueMapping(value = "${key.userM?pS*}", collector = ValueMappingAntMatchCollector.class)
 *      private List&lt;Map&lt;String, Object&gt;&gt; userMapList;
 *      
 *      // Collect property whose key matches the regular expression specified in the placeholder
 *      &#064;ValueMapping(value = "${userXmlS[tr]{2}}", collector = ValueMappingRegMatchCollector.class, parser = ValueMappingXmlBeanParser.class)
 *      private User[] xmlUserArray;
 *      
 *      // Support to annotate on method with only one parameter of complex type, and invoke automatically if property updated
 *      &#064;ValueMapping("${userMapStr}")
 *      private void setUserMap(Map&lt;String, Object&gt; userMap){
 *      }
 *      
 *      // Support to annotate on method parameters, and invoke automatically if one or more properties updated.
 *      private void setAll(&#064;Value("${customName:zxh}") String customName,
 *          &#064;ValueMapping("${key.userMapStr}") Map&lt;String, Object&gt; userMap,
 *          &#064;ValueMapping("${key.ipListStr}") List&lt;String&gt; ipList,
 *          &#064;ValueMapping(value = "${userXmlStr}", parser = ValueMappingXmlBeanParser.class) User xmlUser) {
 *          
 *      }
 *      
 *      ...
 *   }
 * </pre>
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValueMapping {

  /**
   * (Required) The key expression of mapping property: e.g. "${userName:zxh}".
   */
  String value();

  /**
   * The parser to convert property value to the type of annotated argument, use
   * {@link ValueMappingJsonParser} as default. Thus the value type could be any complex one if
   * {@link Gson#fromJson(String, java.lang.reflect.Type)} supports.
   * 
   * @see ValueMappingXmlBeanParser
   */
  Class<? extends ValueMappingParser> parser() default ValueMappingJsonParser.class;

  /**
   * The collector to collect a kind of properties and convert them to the value of Collection or
   * Array. Use the interface {@link ValueMappingCollector} itself as default, which means this
   * parameter default value is invalid, has no affect.
   * 
   * @see ValueMappingAntMatchCollector
   * @see ValueMappingRegMatchCollector
   */
  Class<? extends ValueMappingCollector> collector() default ValueMappingCollector.class;
}
