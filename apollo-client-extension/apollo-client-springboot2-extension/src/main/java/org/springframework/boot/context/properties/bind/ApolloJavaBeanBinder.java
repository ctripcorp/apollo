package org.springframework.boot.context.properties.bind;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.bind.ApolloBinder.ApolloContext;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyState;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.spring.annotation.AutoRefresh;
import com.ctrip.framework.apollo.spring.annotation.RefreshEnabled;
import com.ctrip.framework.apollo.spring.annotation.RefreshField;
import com.ctrip.framework.apollo.util.ConfigUtil;

/**
 * {@link BeanBinder} for mutable Java Beans.
 *
 *  @author wangbo(wangle_r@163.com)
 */
class ApolloJavaBeanBinder implements ExtensionBinder {
  private ConfigUtil configUtil;
  
  public ApolloJavaBeanBinder() {
    super();
    this.configUtil = ApolloInjector.getInstance(ConfigUtil.class);
  }

  @Override
  public <T> T bind(ConfigurationPropertyName name, Bindable<T> target, ApolloContext context,
      BeanPropertyBinder propertyBinder) {
    boolean hasKnownBindableProperties = context.streamSources().anyMatch((
        s) -> s.containsDescendantOf(name) == ConfigurationPropertyState.PRESENT);
    ApolloBeanExtension<T> bean = ApolloBeanExtension.get(target, hasKnownBindableProperties);
    if (bean == null) {
      return null;
    }
    ApolloBeanExtensionSupplier<T> beanSupplier = bean.getSupplier(target);
    boolean bound = bind(propertyBinder, bean, beanSupplier, target);
    return (bound ? beanSupplier.get() : null);
  }

  private <T> boolean bind(BeanPropertyBinder propertyBinder, ApolloBeanExtension<T> bean,
      ApolloBeanExtensionSupplier<T> beanSupplier, Bindable<T> target) {
    boolean bound = false;
    for (Map.Entry<String, ApolloBeanExtensionProperty> entry : bean.getProperties().entrySet()) {
      bound |= bind(beanSupplier, propertyBinder, entry.getValue(), target);
    }
    return bound;
  }

  private <T> boolean bind(ApolloBeanExtensionSupplier<T> beanSupplier,
      BeanPropertyBinder propertyBinder, ApolloBeanExtensionProperty property, Bindable<T> target) {
    if(configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
      /**
       * 
       */
      if(hasRefresh(target, property)) {
        String propertyName = property.getName();
        ResolvableType type = property.getType();
        Supplier<Object> value = property.getValue(beanSupplier);
        Annotation[] annotations = property.getAnnotations();
        Object bound = propertyBinder.bindProperty(propertyName,
            Bindable.of(type).withSuppliedValue(value).withAnnotations(annotations));
        if (bound == null) {
          return false;
        }
        if (property.isSettable()) {
          property.setValue(beanSupplier, bound);
        }
        else if (value == null || !bound.equals(value.get())) {
          throw new IllegalStateException(
              "No setter found for property: " + property.getName());
        }
      }
    }
    return true;
  }

  private <T> boolean hasRefresh(Bindable<T> target, ApolloBeanExtensionProperty property) {
    /*
     * @RefreshEnabled has the highest priority
     */
    if(target.getAnnotation(RefreshEnabled.class) != null) {
      return true;
    }
    /*
     * Determine if @RefreshField exists, and enable automatic updates
     */
    
    if(property.getField() != null) {
      RefreshField refreshField =  property.getField().getAnnotation(RefreshField.class);
      if(refreshField != null) {
        return refreshField.value();
      }
    }
    if(property.getSetMethod() != null) {
      RefreshField refreshField =  AnnotationUtils.findAnnotation(property.getSetMethod(), RefreshField.class);
      if(refreshField != null) {
        return refreshField.value();
      }
    }
    
    /*
     * If @Refreshfield does not exist, determine if @Autorefresh does
     */
    AutoRefresh autoRefresh = target.getAnnotation(AutoRefresh.class);
    if(autoRefresh != null) {
      return autoRefresh.value();
    }
    /*
     * The default enable automatic updates
     */
    return true;
  }

  /**
   * The bean being bound.
   */
  private static class ApolloBeanExtension<T> {

    private static ApolloBeanExtension<?> cached;

    private final Class<?> type;

    private final ResolvableType resolvableType;

    private final Map<String, ApolloBeanExtensionProperty> properties = new LinkedHashMap<>();

    ApolloBeanExtension(ResolvableType resolvableType, Class<?> type) {
      this.resolvableType = resolvableType;
      this.type = type;
      putProperties(type);
    }

    private void putProperties(Class<?> type) {
      while (type != null && !Object.class.equals(type)) {
        for (Method method : type.getDeclaredMethods()) {
          if (isCandidate(method)) {
            addMethod(method);
          }
        }
        for (Field field : type.getDeclaredFields()) {
          addField(field);
        }
        type = type.getSuperclass();
      }
    }

    private boolean isCandidate(Method method) {
      int modifiers = method.getModifiers();
      return Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers)
          && !Modifier.isStatic(modifiers)
          && !Object.class.equals(method.getDeclaringClass())
          && !Class.class.equals(method.getDeclaringClass());
    }

    private void addMethod(Method method) {
      addMethodIfPossible(method, "get", 0, ApolloBeanExtensionProperty::addGetter);
      addMethodIfPossible(method, "is", 0, ApolloBeanExtensionProperty::addGetter);
      addMethodIfPossible(method, "set", 1, ApolloBeanExtensionProperty::addSetter);
    }

    private void addMethodIfPossible(Method method, String prefix, int parameterCount,
        BiConsumer<ApolloBeanExtensionProperty, Method> consumer) {
      if (method.getParameterCount() == parameterCount
          && method.getName().startsWith(prefix)
          && method.getName().length() > prefix.length()) {
        String propertyName = Introspector
            .decapitalize(method.getName().substring(prefix.length()));
        consumer.accept(this.properties.computeIfAbsent(propertyName,
            this::getBeanProperty), method);
      }
    }

    private ApolloBeanExtensionProperty getBeanProperty(String name) {
      return new ApolloBeanExtensionProperty(name, this.resolvableType);
    }

    private void addField(Field field) {
      ApolloBeanExtensionProperty property = this.properties.get(field.getName());
      if (property != null) {
        property.addField(field);
      }
    }

    public Class<?> getType() {
      return this.type;
    }

    public Map<String, ApolloBeanExtensionProperty> getProperties() {
      return this.properties;
    }

    @SuppressWarnings("unchecked")
    public ApolloBeanExtensionSupplier<T> getSupplier(Bindable<T> target) {
      return new ApolloBeanExtensionSupplier<>(() -> {
        T instance = null;
        if (target.getValue() != null) {
          instance = target.getValue().get();
        }
        if (instance == null) {
          instance = (T) BeanUtils.instantiateClass(this.type);
        }
        return instance;
      });
    }

    @SuppressWarnings("unchecked")
    public static <T> ApolloBeanExtension<T> get(Bindable<T> bindable, boolean canCallGetValue) {
      Class<?> type = bindable.getType().resolve(Object.class);
      Supplier<T> value = bindable.getValue();
      T instance = null;
      if (canCallGetValue && value != null) {
        instance = value.get();
        type = (instance != null) ? instance.getClass() : type;
      }
      if (instance == null && !isInstantiable(type)) {
        return null;
      }
      ApolloBeanExtension<?> bean = ApolloBeanExtension.cached;
      if (bean == null || !type.equals(bean.getType())) {
        bean = new ApolloBeanExtension<>(bindable.getType(), type);
        cached = bean;
      }
      return (ApolloBeanExtension<T>) bean;
    }

    private static boolean isInstantiable(Class<?> type) {
      if (type.isInterface()) {
        return false;
      }
      try {
        type.getDeclaredConstructor();
        return true;
      }
      catch (Exception ex) {
        return false;
      }
    }

  }

  private static class ApolloBeanExtensionSupplier<T> implements Supplier<T> {

    private final Supplier<T> factory;

    private T instance;

    ApolloBeanExtensionSupplier(Supplier<T> factory) {
      this.factory = factory;
    }

    @Override
    public T get() {
      if (this.instance == null) {
        this.instance = this.factory.get();
      }
      return this.instance;
    }

  }

  /**
   * A bean property being bound.
   */
  private static class ApolloBeanExtensionProperty {

    private final String name;

    private final ResolvableType declaringClassType;

    private Method getter;

    private Method setter;

    private Field field;
    ApolloBeanExtensionProperty(String name, ResolvableType declaringClassType) {
      this.name = BeanPropertyName.toDashedForm(name);
      this.declaringClassType = declaringClassType;
      
    }

    public void addGetter(Method getter) {
      if (this.getter == null) {
        this.getter = getter;
      }
    }

    public void addSetter(Method setter) {
      if (this.setter == null) {
        this.setter = setter;
      }
    }

    public void addField(Field field) {
      if (this.field == null) {
        this.field = field;
      }
    }

    public String getName() {
      return this.name;
    }

    public ResolvableType getType() {
      if (this.setter != null) {
        MethodParameter methodParameter = new MethodParameter(this.setter, 0);
        return ResolvableType.forMethodParameter(methodParameter,
            this.declaringClassType);
      }
      MethodParameter methodParameter = new MethodParameter(this.getter, -1);
      return ResolvableType.forMethodParameter(methodParameter,
          this.declaringClassType);
    }

    public Annotation[] getAnnotations() {
      try {
        return (this.field != null) ? this.field.getDeclaredAnnotations() : null;
      }
      catch (Exception ex) {
        return null;
      }
    }

    public Method getSetMethod() {
      return setter;
    }

    public Field getField() {
      return field;
    }

    public Supplier<Object> getValue(Supplier<?> instance) {
      if (this.getter == null) {
        return null;
      }
      return () -> {
        try {
          this.getter.setAccessible(true);
          return this.getter.invoke(instance.get());
        }
        catch (Exception ex) {
          throw new IllegalStateException(
              "Unable to get value for property " + this.name, ex);
        }
      };
    }

    public boolean isSettable() {
      return this.setter != null;
    }

    public void setValue(Supplier<?> instance, Object value) {
      try {
        this.setter.setAccessible(true);
        this.setter.invoke(instance.get(), value);
      }
      catch (Exception ex) {
        throw new IllegalStateException(
            "Unable to set value for property " + this.name, ex);
      }
    }

  }

}
