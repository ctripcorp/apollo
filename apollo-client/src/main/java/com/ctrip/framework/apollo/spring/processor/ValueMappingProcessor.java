package com.ctrip.framework.apollo.spring.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.MethodCallback;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ValueMapping;
import com.ctrip.framework.apollo.spring.property.PlaceholderHelper;
import com.ctrip.framework.apollo.spring.property.ValueMappingElement;
import com.ctrip.framework.apollo.spring.property.ValueMappingHolder;
import com.ctrip.framework.apollo.spring.property.ValueMappingOriginValue;
import com.ctrip.framework.apollo.spring.property.ValueMappingProperty;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * The processor to update property value for {@link ValueMapping} annotated argument
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingProcessor {

  private final Logger logger = LoggerFactory.getLogger(ValueMappingProcessor.class);

  private final PlaceholderHelper placeholderHelper;

  // Thread safe
  private final Gson gson = new Gson();

  /**
   * all the config namespace
   */
  private String[] allNamespaces;

  public ValueMappingProcessor() {
    placeholderHelper = ApolloInjector.getInstance(PlaceholderHelper.class);
  }

  /**
   * create a formated log
   * 
   * @param method
   * @param log
   * @param elem
   * @return
   */
  private String createLog(String method, String log, ValueMappingElement elem) {
    return String.format("[op:%s] %s, property='%s', %s='%s', class='%s'", method, log,
        elem.getPropKeyDesc(), elem.isField() ? "field" : "method", elem.getElement().getName(),
        elem.getElement().getDeclaringClass().getName());
  }

  /**
   * ensure argument is not null
   * 
   * @param arg
   * @param msg
   */
  private static void notNull(Object arg, String msg) {
    if (arg == null) {
      throw new IllegalArgumentException(msg);
    }
  }

  /**
   * create the value mapping elements of an Apollo config instance
   * 
   * @param bean Config bean
   * @return
   */
  public List<ValueMappingElement> createValueMappingElements(Object bean) {
    // argument check
    notNull(bean, "bean may not be null");

    final Class<?> clazz = bean.getClass();

    // value mapping elements
    final List<ValueMappingElement> elemList = new ArrayList<>();

    // find value mapping field
    ReflectionUtils.doWithFields(clazz, new FieldCallback() {

      @Override
      public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        ValueMappingElement elem = createValueMappingField(field, clazz);
        if (elem != null) {
          elemList.add(elem);
        }
      }

    });

    // find value mapping method
    ReflectionUtils.doWithMethods(clazz, new MethodCallback() {

      @Override
      public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        ValueMappingElement elem = createValueMappingMethod(method, clazz);
        if (elem != null) {
          elemList.add(elem);
        }
      }

    });

    return elemList;
  }

  /**
   * create value mapping field
   * 
   * @param field
   * @param namespaces
   * @param beanClass
   * @return
   */
  private ValueMappingElement createValueMappingField(Field field, final Class<?> beanClass) {
    if (Modifier.isFinal(field.getModifiers())) {
      return null;
    }
    ValueMapping mappingAnno = field.getAnnotation(ValueMapping.class);
    if (mappingAnno == null) {
      return null;
    }

    // create value mapping holder
    ValueMappingHolder holder = createValueMappingHolder(field, field.getType(),
        field.getGenericType(), mappingAnno, beanClass);
    if (holder == null) {
      // invalid annotation
      return null;
    }

    // create value mapping element of field
    return createValueMappingElement(field, beanClass, holder);
  }

  /**
   * create value mapping method
   * 
   * @param method
   * @param namespaces
   * @param beanClass
   * @return
   */
  private ValueMappingElement createValueMappingMethod(Method method, final Class<?> beanClass) {

    ValueMapping mappingAnno = method.getAnnotation(ValueMapping.class);
    if (mappingAnno == null) {
      // may annotate on method parameters
      return createValueMappingMethodParams(method, beanClass);
    }

    // annotated on method
    Class<?>[] paramTypes = method.getParameterTypes();
    Type[] paramGenTypes = method.getGenericParameterTypes();
    if (paramTypes == null || paramTypes.length != 1) {
      logger.error(
          "[op:createValueMappingMethod] invalid annotation, argument number is not 1. method={}, class={}",
          method.getName(), beanClass.getName());
      return null;
    }

    // create value mapping holder
    ValueMappingHolder holder =
        createValueMappingHolder(method, paramTypes[0], paramGenTypes[0], mappingAnno, beanClass);
    if (holder == null) {
      // invalid annotation
      return null;
    }

    // create value mapping element of method
    return createValueMappingElement(method, beanClass, holder);
  }

  /**
   * Create value mapping method, which is annotated on parameters
   * 
   * @param method
   * @param beanClass
   * @return
   */
  private ValueMappingElement createValueMappingMethodParams(Method method,
      final Class<?> beanClass) {

    // check if config is valid
    Annotation[][] paramAnnos = method.getParameterAnnotations();
    boolean validAnno = false;
    if (paramAnnos != null && paramAnnos.length > 0) {
      validAnno = true;
      for (Annotation[] annos : paramAnnos) {
        if (annos == null || annos.length != 1
            || !(annos[0] instanceof Value || annos[0] instanceof ValueMapping)) {
          validAnno = false;
          break;
        }
      }
    }
    if (!validAnno) {
      return null;
    }

    // check if annotated all the method parameters
    Class<?>[] paramTypes = method.getParameterTypes();
    Type[] paramGenTypes = method.getGenericParameterTypes();
    if (paramAnnos.length != paramTypes.length) {
      logger.error(
          "[op:createValueMappingMethodParams] invalid config, not annotated all arguments. method={}, class={}",
          method.getName(), beanClass.getName());
      return null;
    }

    // create parameter property
    ValueMappingHolder[] holders = new ValueMappingHolder[paramAnnos.length];
    for (int i = 0; i < paramAnnos.length; i++) {
      // handle annotation config
      Annotation anno = paramAnnos[i][0];
      Class<?> type = paramTypes[i];
      Type genType = paramGenTypes[i];

      // create value mapping holder
      if (anno instanceof Value) {
        Value valueAnno = (Value) anno;
        String keyExpr = valueAnno.value();
        PropertyKey propKey = parseMappingPropKey(keyExpr, false);
        holders[i] = createValueMappingHolder(propKey, type, genType, null, null);
      } else {
        ValueMapping mappingAnno = (ValueMapping) anno;
        holders[i] = createValueMappingHolder(method, type, genType, mappingAnno, beanClass);
      }
      if (holders[i] == null) {
        // invalid annotation
        return null;
      }
    }

    // create value mapping element of method
    return createValueMappingElement(method, beanClass, holders);
  }

  private ValueMappingElement createValueMappingElement(Member method, final Class<?> beanClass,
      ValueMappingHolder... holders) {
    String[] namespaces = getConfigNamespaces(beanClass);
    return new ValueMappingElement(namespaces, method, holders);
  }

  /**
   * Get the namespaces of config bean
   * 
   * @param beanClass
   * @return String[] The specific namespaces if the bean annotated by EnableApolloConfig, otherwise
   *         return all the namespaces of application
   */
  private String[] getConfigNamespaces(final Class<?> beanClass) {
    String[] namespaces;
    EnableApolloConfig anno = AnnotationUtils.findAnnotation(beanClass, EnableApolloConfig.class);
    if (anno != null) {
      namespaces = anno.value();
    } else {
      if (allNamespaces == null) {
        // the namespaces will no longer be changed, store in global variable
        synchronized (this) {
          if (allNamespaces == null) {
            allNamespaces = ConfigService.getAllNamespaces().toArray(new String[0]);
          }
        }
      }
      namespaces = allNamespaces;
    }
    return namespaces;
  }

  private ValueMappingHolder createValueMappingHolder(Member mappingElem, Class<?> valType,
      Type valGenType, ValueMapping mappingAnno, Class<?> beanClass) {

    // check if a collection property is valid
    ValueMappingCollector collector = getCollector(mappingAnno);
    if (collector != null && !isCollectPropType(valType)) {
      logger.error(
          "[op:createValueMappingHolder] invalid config, the mapping type is not Collection or Array. valType={} element={}, class={}",
          valType, mappingElem.getName(), beanClass.getName());
      return null;
    }

    // parse property key
    boolean hasCollector = collector != null;
    String keyExpr = mappingAnno.value();
    PropertyKey propKey = parseMappingPropKey(keyExpr, hasCollector);
    if (propKey == null) {
      logger.error(
          "[op:createValueMappingHolder] invalid property key. keyExpr={} element={} class={}",
          keyExpr, mappingElem.getName(), beanClass.getName());
      return null;
    }

    // get parser instance
    ValueMappingParser parser = ApolloInjector.getInstance(mappingAnno.parser());
    if (parser == null) {
      logger.error(
          "[op:createValueMappingHolder] invalid parser. parserClass={} element={} class={}",
          mappingAnno.parser().getName(), mappingElem.getName(), beanClass.getName());
      return null;
    }

    return createValueMappingHolder(propKey, valType, valGenType, parser, collector);
  }

  private ValueMappingHolder createValueMappingHolder(PropertyKey propKey, Class<?> type,
      Type genericType, ValueMappingParser parser, ValueMappingCollector collector) {
    return new ValueMappingHolder(propKey.getPlaceholder(), propKey.getPropKey(),
        propKey.getDefaultValue(), type, genericType, parser, collector);
  }

  private ValueMappingCollector getCollector(ValueMapping valueMapping) {
    if (valueMapping.collector() == ValueMappingCollector.class) {
      return null;
    }
    return ApolloInjector.getInstance(valueMapping.collector());
  }

  /**
   * Judge if the type is Collection or Array
   * 
   * @param type
   * @return
   */
  private boolean isCollectPropType(Class<?> type) {
    return Collection.class.isAssignableFrom(type) || type.isArray();
  }

  private PropertyKey parseMappingPropKey(String keyExpr, boolean hasCollector) {

    // parse property key
    Set<String> keys = placeholderHelper.extractPlaceholderKeys(keyExpr);
    if (keys == null || keys.size() != 1) {
      // only support one key
      return null;
    }

    // parse default value
    String key = keys.iterator().next();
    int sepIdx = keyExpr.indexOf(':');
    int endIdx = keyExpr.lastIndexOf('}');
    String defaultValStr = null;
    // default value is unsupported for the collection property
    if (!hasCollector && sepIdx != -1 && sepIdx < endIdx) {
      defaultValStr = keyExpr.substring(sepIdx + 1, endIdx);
    }

    return new PropertyKey(keyExpr, key, defaultValStr);
  }

  /**
   * Update the property value of value mapping element
   * 
   * @param elem The value mapping annotated class element
   * @param environment Property environment
   * @return new value
   */
  public Object updateProperty(Object bean, ValueMappingElement elem, Environment environment) {
    // argument check
    notNull(bean, "bean may not be null");
    notNull(elem, "elem may not be null");
    notNull(environment, "environment may not be null");

    // check whether the mapping properties exists
    if (!isPropertyExists(elem, environment)) {
      logger.info(createLog("updateProperty", "Not exists property", elem));
      return null;
    }

    if (elem.isField()) {
      // update field property
      return updateFieldProperty(bean, elem, environment);
    } else if (elem.isMethod()) {
      // update method property
      return updateMethodProperty(bean, elem, environment);
    } else {
      logger.error(createLog("updateProperty", "unsupported elememnt type", elem));
      return null;
    }
  }

  /**
   * all property keys of namespaces
   * 
   * @param namespaces
   * @return
   */
  private Set<String> getAllProperyKeys(String[] namespaces) {

    if (namespaces == null || namespaces.length == 0) {
      return Collections.emptySet();
    }

    Set<String> keys = null;
    for (int i = 0; i < namespaces.length; i++) {
      Config config = ConfigService.getConfig(namespaces[i]);
      Set<String> propNames = config.getPropertyNames();
      if (propNames == null) {
        propNames = Collections.emptySet();
      }
      if (i == 0) {
        keys = propNames;
      } else {
        if (i == 1) {
          keys = new LinkedHashSet<>(keys);
        }
        keys.addAll(propNames);
      }
    }
    if (keys == null) {
      keys = Collections.emptySet();
    }
    return keys;
  }

  /**
   * Update the property value for field
   * 
   * @param bean
   * @param elem
   * @param environment
   * @return new value
   */
  private Object updateFieldProperty(Object bean, ValueMappingElement elem,
      Environment environment) {

    // get type converted property value
    ValueMappingHolder holder = elem.getFirstHolder();
    Object fieldVal = resolvePropertyValue(holder, elem, environment);

    // set property value to field
    Field field = (Field) elem.getElement();
    setFieldValue(bean, elem, field, fieldVal);

    if (logger.isDebugEnabled()) {
      logger.debug(createLog("updateProperty",
          "update property value successfully, value='" + gson.toJson(fieldVal) + "'", elem));
    }
    return fieldVal;
  }

  /**
   * Update the property value for method
   * 
   * @param bean
   * @param elem
   * @param environment
   * @return new argument values
   */
  private Object updateMethodProperty(Object bean, ValueMappingElement elem,
      Environment environment) {

    // create method argument values
    ValueMappingHolder[] holders = elem.getHolders();
    Object[] argVals = new Object[holders.length];
    for (int i = 0; i < holders.length; i++) {
      // get type converted property value
      argVals[i] = resolvePropertyValue(holders[i], elem, environment);
    }

    // invoke method to update property value
    Method method = (Method) elem.getElement();
    invokeMethod(bean, elem, method, argVals);

    if (logger.isDebugEnabled()) {
      logger.debug(createLog("updateProperty",
          "update property value successfully, value='" + gson.toJson(argVals) + "'", elem));
    }
    return argVals;
  }

  /**
   * get type converted property value
   * 
   * @param holder
   * @param elem
   * @param environment
   * @return
   */
  private Object resolvePropertyValue(ValueMappingHolder holder, ValueMappingElement elem,
      Environment environment) {
    String propKey = holder.getPropKey();
    Object value;
    if (holder.hasCollector()) {
      // mapping multiple properties
      Map<String, String> propMap = new LinkedHashMap<>();
      ValueMappingCollector collector = holder.getCollector();
      Set<String> allPropKeys = getAllProperyKeys(elem.getNamespaces());
      for (String remoteKey : allPropKeys) {
        String propValue = environment.getProperty(remoteKey);
        if (collector.filter(propKey, remoteKey, propValue)) {
          propMap.put(remoteKey, propValue);
        }
      }
      value = convertCollectValue(propMap, holder, elem);
    } else {
      // mapping single property
      String valStr = environment.getProperty(propKey, holder.getDefaultValue());
      if (holder.isValueMapping()) {
        value = parseMappingPropValue(valStr, holder);
      } else {
        // for the basic type
        value = resolvePropertyValue(valStr, holder.getType(), holder.getGenericType());
      }
    }
    return value;
  }

  /**
   * get type converted property value for basic java type
   * 
   * @param valStr
   * @param valueType
   * @param valueGenType
   * @return
   */
  private Object resolvePropertyValue(String valStr, Class<?> valueType, Type valueGenType) {
    if (valStr == null) {
      return null;
    }
    Object value;
    if (valueType == String.class) {
      value = valStr;
    } else if (valueType == int.class || valueType == Integer.class) {
      value = Integer.valueOf(valStr);
    } else if (valueType == long.class || valueType == Long.class) {
      value = Long.valueOf(valStr);
    } else if (valueType == boolean.class || valueType == Boolean.class) {
      value = Boolean.valueOf(valStr);
    } else if (valueType == double.class || valueType == Double.class) {
      value = Double.valueOf(valStr);
    } else if (valueType == short.class || valueType == Short.class) {
      value = Short.valueOf(valStr);
    } else if (valueType == float.class || valueType == Float.class) {
      value = Float.valueOf(valStr);
    } else if (valueType == byte.class || valueType == Byte.class) {
      value = Byte.valueOf(valStr);
    } else {
      // for the other java types like BigDecimal, assume it satisfies JSON format
      value = gson.fromJson(valStr, valueGenType);
    }
    return value;
  }

  /**
   * @param value
   * @param holder
   * @param type
   * @param genType
   * @param elem
   * @return
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private Object convertCollectValue(Map<String, String> propMap, ValueMappingHolder holder,
      ValueMappingElement elem) {
    Class<?> type = holder.getType();
    Type genType = holder.getGenericType();
    // obtain the generic type of type argument
    Type argType = getArgGenericType(genType);
    if (argType == null) {
      // should not be executed
      logger.error(createLog("convertCollectValue", "unsupported type argument", elem));
      return null;
    }

    // obtain the class type of type argument
    Class<?> argClass = getArgClass(argType);
    if (argClass == null) {
      // obtain class type failed，use JsonElement as default
      argClass = JsonElement.class;
      logger.warn(createLog("convertCollectValue",
          "cannot abtain the calss of type argument, argType='" + argType + "'", elem));
    }

    // create property list
    List<ValueMappingProperty> propList = new ArrayList<>(propMap.size());
    for (Entry<String, String> entry : propMap.entrySet()) {
      String propKey = entry.getKey();
      String valStr = entry.getValue();
      // parse mapping value
      Object propVal = parseMappingPropValue(valStr, holder, argClass, argType);
      // add to list
      propList.add(new ValueMappingProperty(propKey, propVal));
    }

    // execute the post method after filtering
    holder.getCollector().postFilter(propList);

    // convert to the declared Collection or Array
    if (type == List.class || type == ArrayList.class || type == Collection.class) {
      // for the most common List, convert to a raw ArrayList directly for better performance
      List result = new ArrayList(propList.size());
      for (ValueMappingProperty prop : propList) {
        result.add(prop.getValue());
      }
      return result;
    } else if (type == Set.class || type == HashSet.class) {
      // for the most common Set, convert to a raw HashSet directly for better performance
      Set result = new HashSet(propList.size());
      for (ValueMappingProperty prop : propList) {
        result.add(prop.getValue());
      }
      return result;
    } else {
      // Use JSON to convert
      JsonArray jsonArray = new JsonArray();
      for (ValueMappingProperty prop : propList) {
        JsonElement json = gson.toJsonTree(prop.getValue());
        jsonArray.add(json);
      }
      return gson.fromJson(jsonArray, genType);
    }
  }


  /**
   * @param genType
   * @return
   */
  private Type getArgGenericType(Type genType) {
    Type argType = null;
    if (genType instanceof ParameterizedType) {
      // Collection type
      ParameterizedType parmType = (ParameterizedType) genType;
      Type[] typeArgs = parmType.getActualTypeArguments();
      argType = typeArgs[0];
    } else if (genType instanceof Class) {
      // Array Type
      Class<?> clazz = (Class<?>) genType;
      if (clazz.isArray()) {
        argType = clazz.getComponentType();
      }
    }
    return argType;
  }

  /**
   * @param argGenType
   * @return
   */
  private Class<?> getArgClass(Type argGenType) {
    Class<?> argClass = null;
    if (argGenType instanceof Class) {
      argClass = (Class<?>) argGenType;
    } else if (argGenType instanceof ParameterizedType) {
      ParameterizedType p = (ParameterizedType) argGenType;
      Type rawType = p.getRawType();
      if (rawType instanceof Class) {
        argClass = (Class<?>) rawType;
      }
    }
    return argClass;
  }

  /**
   * convert value by the specific parser and holder
   * 
   * @param value
   * @param holder
   * @return
   */
  private Object parseMappingPropValue(String value, ValueMappingHolder holder) {
    return parseMappingPropValue(value, holder, holder.getType(), holder.getGenericType());
  }

  /**
   * convert value by the specific parser and type
   * 
   * @param value
   * @param holder
   * @param type
   * @param genericType
   * @return
   */
  private Object parseMappingPropValue(String value, ValueMappingHolder holder, Class<?> type,
      Type genericType) {
    return holder.getParser().parse(new ValueMappingOriginValue(value, type, genericType));
  }

  /**
   * set property value to field
   * 
   * @param bean
   * @param elem
   * @param field
   * @param value
   */
  private void setFieldValue(Object bean, ValueMappingElement elem, Field field, Object value) {
    try {
      // For thread safe, always make accessible, since save the accessibility at first and set it
      // back could be wrong in a concurrent access scenario.
      ReflectionUtils.makeAccessible(field);
      ReflectionUtils.setField(field, bean, value);
    } catch (Exception e) {
      logger.error(createLog("setFieldValue", "set value failed", elem), e);
      throw new IllegalArgumentException(
          String.format("Set value failed for the field '%s' of class '%s'", field.getName(),
              bean.getClass().getName()),
          e);
    }
  }

  /**
   * invoke method to update property value
   * 
   * @param bean
   * @param elem
   * @param method
   * @param argVals
   */
  private void invokeMethod(Object bean, ValueMappingElement elem, Method method,
      Object[] argVals) {
    try {
      ReflectionUtils.makeAccessible(method);
      ReflectionUtils.invokeMethod(method, bean, argVals);
    } catch (Exception e) {
      logger.error(createLog("invokeMethod", "set value failed", elem), e);
      throw new IllegalArgumentException(
          String.format("Set value failed for the method '%s' of class '%s'", method.getName(),
              bean.getClass().getName()),
          e);
    }
  }

  /**
   * Judge if all the value mapping properties exists
   * 
   * @param elem The value mapping annotated class element
   * @param environment Property environment
   * @return boolean true: exists, false: not exists
   */
  private boolean isPropertyExists(ValueMappingElement elem, Environment environment) {
    // holders array cann't be empty
    for (ValueMappingHolder holder : elem.getHolders()) {
      if (!isPropertyExists(holder, elem.getNamespaces(), environment)) {
        // Not all properties exists
        return false;
      }
    }
    return true;
  }

  /**
   * Judge if all the value mapping properties exists
   * 
   * @param holder Value mapping holder
   * @param namespaces The namespaces of the config bean
   * @return boolean true: exists, false: not exists
   */
  private boolean isPropertyExists(ValueMappingHolder holder, String[] namespaces,
      Environment environment) {

    // normal property, the property key is explicit
    if (!holder.hasCollector()) {
      return environment.getProperty(holder.getPropKey(), holder.getDefaultValue()) != null;
    }

    // collection property , the property key is ambiguous.
    String localKey = holder.getPropKey();
    ValueMappingCollector collector = holder.getCollector();
    Set<String> allPropKeys = getAllProperyKeys(namespaces);
    for (String remoteKey : allPropKeys) {
      String propVal = environment.getProperty(remoteKey);
      if (collector.filter(localKey, remoteKey, propVal)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Judge if the value mapping properties are changed
   * 
   * @param elem The value mapping annotated class element
   * @param changeEvent Apollo config update event
   * @param environment Property environment
   * @return boolean true: changed, false: not change
   */
  public boolean isPropertyChanged(ValueMappingElement elem, ConfigChangeEvent changeEvent,
      Environment environment) {
    // argument check
    notNull(elem, "elem may not be null");
    notNull(changeEvent, "changeEvent may not be null");
    notNull(environment, "environment may not be null");

    // check the namespace of this change event.
    boolean namespaceChanged = false;
    for (String namespace : elem.getNamespaces()) {
      if (namespace.equals(changeEvent.getNamespace())) {
        namespaceChanged = true;
        break;
      }
    }
    if (!namespaceChanged) {
      return false;
    }

    // holders array cann't be empty
    for (ValueMappingHolder holder : elem.getHolders()) {
      if (holder.hasCollector()) {
        // collection property , the property key is ambiguous.
        String localKey = holder.getPropKey();
        ValueMappingCollector collector = holder.getCollector();

        for (String remoteKey : changeEvent.changedKeys()) {
          String propVal = environment.getProperty(remoteKey);
          ConfigChange configChange = changeEvent.getChange(remoteKey);
          // check whether the value is really changed and filtered
          if (Objects.equals(propVal, configChange.getNewValue())
              && collector.filter(localKey, remoteKey, propVal)) {
            return true;
          }
        }
      } else {
        // normal property, the property key is explicit
        String key = holder.getPropKey();
        if (changeEvent.isChanged(key)) {
          ConfigChange configChange = changeEvent.getChange(key);
          // check whether the value is really changed
          if (Objects.equals(environment.getProperty(key), configChange.getNewValue())) {
            return true;
          }
        }
      }
    }

    return false;
  }

  private static final class PropertyKey {

    /**
     * placeholder expression
     */
    private final String placeholder;

    /**
     * property key
     */
    private final String propKey;

    /**
     * property default value
     */
    private final String defaultValue;

    public PropertyKey(String placeholder, String propKey, String defaultValue) {
      notNull(placeholder, "placeholder may not be null");
      notNull(propKey, "propKey may not be null");
      this.placeholder = placeholder;
      this.propKey = propKey;
      this.defaultValue = defaultValue;
    }

    public String getPlaceholder() {
      return placeholder;
    }

    public String getPropKey() {
      return propKey;
    }

    public String getDefaultValue() {
      return defaultValue;
    }

  }
}
