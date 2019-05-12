package com.ctrip.framework.apollo.spring.boot.extension;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.support.ResourceEditorRegistrar;
import org.springframework.boot.bind.ApolloExtensionPropertyNamePatternsMatcher;
import org.springframework.boot.bind.ApolloExtensionPropertySourcesPropertyValues;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.bind.RelaxedNames;
import org.springframework.boot.bind.apolloextension.ApolloPropertyNamePatternsMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.PropertySources;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.spring.annotation.AutoRefresh;
import com.ctrip.framework.apollo.spring.annotation.RefreshEnabled;
import com.ctrip.framework.apollo.spring.annotation.RefreshField;
import com.ctrip.framework.apollo.util.ConfigUtil;

/**
 * @author wangbo(wangle_r@163.com)
 */
class ApolloRefreshPropertiesConfigurationFactory<T> {

  private static final char[] EXACT_DELIMITERS = { '_', '.', '[' };

  private static final char[] TARGET_NAME_DELIMITERS = { '_', '.' };

  private static Logger logger = LoggerFactory.getLogger(ApolloRefreshPropertiesConfigurationFactory.class);

  private boolean ignoreUnknownFields = true;

  private boolean ignoreInvalidFields;

  private boolean exceptionIfInvalid = true;

  private PropertySources propertySources;

  private final T target;

  private Validator validator;

  private ApplicationContext applicationContext;

  private boolean ignoreNestedProperties = false;

  private String targetName;

  private ConversionService conversionService;

  private boolean resolvePlaceholders = true;

  private Annotation[] annotations;

  /**
   * Create a new {@link ApolloRefreshPropertiesConfigurationFactory} instance.
   * 
   * @param target the target object to bind too
   * @see #PropertiesConfigurationFactory(Class)
   */
  public ApolloRefreshPropertiesConfigurationFactory(T target) {
    Assert.notNull(target, "target must not be null");
    this.target = target;
  }

  /**
   * Create a new {@link ApolloRefreshPropertiesConfigurationFactory} instance.
   * 
   * @param type the target type
   * @see #PropertiesConfigurationFactory(Class)
   */
  @SuppressWarnings("unchecked")
  public ApolloRefreshPropertiesConfigurationFactory(Class<?> type) {
    Assert.notNull(type, "type must not be null");
    this.target = (T) BeanUtils.instantiate(type);
  }

  /**
   * Flag to disable binding of nested properties (i.e. those with period
   * separators in their paths). Can be useful to disable this if the name prefix
   * is empty and you don't want to ignore unknown fields.
   * 
   * @param ignoreNestedProperties the flag to set (default false)
   */
  public void setIgnoreNestedProperties(boolean ignoreNestedProperties) {
    this.ignoreNestedProperties = ignoreNestedProperties;
  }

  /**
   * Set whether to ignore unknown fields, that is, whether to ignore bind
   * parameters that do not have corresponding fields in the target object.
   * <p>
   * Default is "true". Turn this off to enforce that all bind parameters must
   * have a matching field in the target object.
   * 
   * @param ignoreUnknownFields if unknown fields should be ignored
   */
  public void setIgnoreUnknownFields(boolean ignoreUnknownFields) {
    this.ignoreUnknownFields = ignoreUnknownFields;
  }

  /**
   * Set whether to ignore invalid fields, that is, whether to ignore bind
   * parameters that have corresponding fields in the target object which are not
   * accessible (for example because of null values in the nested path).
   * <p>
   * Default is "false". Turn this on to ignore bind parameters for nested objects
   * in non-existing parts of the target object graph.
   * 
   * @param ignoreInvalidFields if invalid fields should be ignored
   */
  public void setIgnoreInvalidFields(boolean ignoreInvalidFields) {
    this.ignoreInvalidFields = ignoreInvalidFields;
  }

  /**
   * Set the target name.
   * 
   * @param targetName the target name
   */
  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }

  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  /**
   * Set the property sources.
   * 
   * @param propertySources the property sources
   */
  public void setPropertySources(PropertySources propertySources) {
    this.propertySources = propertySources;
  }

  /**
   * Set the conversion service.
   * 
   * @param conversionService the conversion service
   */
  public void setConversionService(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  /**
   * Set the validator.
   * 
   * @param validator the validator
   */
  public void setValidator(Validator validator) {
    this.validator = validator;
  }

  /**
   * Set a flag to indicate that an exception should be raised if a Validator is
   * available and validation fails.
   * 
   * @param exceptionIfInvalid the flag to set
   * @deprecated as of 1.5, do not specify a {@link Validator} if validation
   *             should not occur
   */
  @Deprecated
  public void setExceptionIfInvalid(boolean exceptionIfInvalid) {
    this.exceptionIfInvalid = exceptionIfInvalid;
  }

  /**
   * Flag to indicate that placeholders should be replaced during binding. Default
   * is true.
   * 
   * @param resolvePlaceholders flag value
   */
  public void setResolvePlaceholders(boolean resolvePlaceholders) {
    this.resolvePlaceholders = resolvePlaceholders;
  }

  public void bindPropertiesToTarget() throws BindException {
    Assert.state(this.propertySources != null, "PropertySources should not be null");
    try {
      if (logger.isTraceEnabled()) {
        logger.trace("Property Sources: " + this.propertySources);

      }
      doBindPropertiesToTarget();
    } catch (BindException ex) {
      if (this.exceptionIfInvalid) {
        throw ex;
      }
      ApolloRefreshPropertiesConfigurationFactory.logger
          .error("Failed to load Properties validation bean. " + "Your Properties may be invalid.", ex);
    }
  }

  private void doBindPropertiesToTarget() throws BindException {
    ConfigUtil configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
      RelaxedDataBinder dataBinder = (this.targetName != null ? new RelaxedDataBinder(this.target, this.targetName)
          : new RelaxedDataBinder(this.target));
      if (this.validator != null && this.validator.supports(dataBinder.getTarget().getClass())) {
        dataBinder.setValidator(this.validator);
      }
      if (this.conversionService != null) {
        dataBinder.setConversionService(this.conversionService);
      }
      dataBinder.setAutoGrowCollectionLimit(Integer.MAX_VALUE);
      dataBinder.setIgnoreNestedProperties(this.ignoreNestedProperties);
      dataBinder.setIgnoreInvalidFields(this.ignoreInvalidFields);
      dataBinder.setIgnoreUnknownFields(this.ignoreUnknownFields);
      customizeBinder(dataBinder);
      if (this.applicationContext != null) {
        ResourceEditorRegistrar resourceEditorRegistrar = new ResourceEditorRegistrar(this.applicationContext,
            this.applicationContext.getEnvironment());
        resourceEditorRegistrar.registerCustomEditors(dataBinder);
      }
      Iterable<String> relaxedTargetNames = getRelaxedTargetNames();
      Set<String> names = getNames(relaxedTargetNames);
      if (!names.isEmpty()) {
        PropertyValues propertyValues = getPropertySourcesPropertyValues(names, relaxedTargetNames);
        dataBinder.bind(propertyValues);
        if (this.validator != null) {
          dataBinder.validate();
        }
        checkForBindingErrors(dataBinder);
      }
    }
  }

  private Iterable<String> getRelaxedTargetNames() {
    return (this.target != null && StringUtils.hasLength(this.targetName) ? new RelaxedNames(this.targetName) : null);
  }

  private Set<String> getNames(Iterable<String> prefixes) {
    Set<String> names = new LinkedHashSet<String>();
    if (this.target != null) {
      Map<String, Field> allField = findAllField(this.target.getClass());
      PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(this.target.getClass());
      for (PropertyDescriptor descriptor : descriptors) {
        String name = descriptor.getName();
        if (!"class".equals(name)) {
          /*
           * Determines whether to run the update field value
           */
          boolean isRefresh = hasRefresh(descriptor, allField);
          if (isRefresh) {
            RelaxedNames relaxedNames = RelaxedNames.forCamelCase(name);
            if (prefixes == null) {
              for (String relaxedName : relaxedNames) {
                names.add(relaxedName);
              }
            } else {
              for (String prefix : prefixes) {
                for (String relaxedName : relaxedNames) {
                  names.add(prefix + "." + relaxedName);
                  names.add(prefix + "_" + relaxedName);
                }
              }
            }
          }
        }
      }
    }
    return names;
  }

  private boolean hasRefresh(PropertyDescriptor descriptor, Map<String, Field> allField) {
    /*
     * @RefreshEnabled has the highest priority
     */
    if (this.getAnnotation(RefreshEnabled.class) != null) {
      return true;
    }

    Field field = allField.get(descriptor.getName());
    if (field != null) {
      RefreshField annotation = AnnotationUtils.findAnnotation(field, RefreshField.class);
      if (annotation != null) {
        return annotation.value();
      }
    }
    /*
     * Determine if @RefreshField exists, and enable automatic updates
     */
    if(descriptor.getWriteMethod() != null) {
      RefreshField annotation = AnnotationUtils.findAnnotation(descriptor.getWriteMethod(), RefreshField.class);
      if (annotation != null) {
        return annotation.value();
      }
    }
    /*
     * If @Refreshfield does not exist, determine if @Autorefresh does
     */
    AutoRefresh autoRefresh = this.getAnnotation(AutoRefresh.class);
    if (autoRefresh != null) {
      return autoRefresh.value();
    }

    /*
     * The default enable automatic updates
     */
    return true;
  }

  private Map<String, Field> findAllField(Class<?> clazz) {
    final Map<String, Field> res = new HashMap<>();
    ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
      @Override
      public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        res.put(field.getName(), field);
      }
    });
    return res;
  }

  @SuppressWarnings("unchecked")
  private <A extends Annotation> A getAnnotation(Class<A> type) {
    for (Annotation annotation : this.annotations) {
      if (type.isInstance(annotation)) {
        return (A) annotation;
      }
    }
    return null;
  }

  private PropertyValues getPropertySourcesPropertyValues(Set<String> names, Iterable<String> relaxedTargetNames) {
    ApolloExtensionPropertyNamePatternsMatcher includes = getPropertyNamePatternsMatcher(names, relaxedTargetNames);
    return new ApolloExtensionPropertySourcesPropertyValues(this.propertySources, names, includes,
        this.resolvePlaceholders);
  }

  private ApolloExtensionPropertyNamePatternsMatcher getPropertyNamePatternsMatcher(Set<String> names,
      Iterable<String> relaxedTargetNames) {
    if (this.ignoreUnknownFields && !isMapTarget()) {
      // Since unknown fields are ignored we can filter them out early to save
      // unnecessary calls to the PropertySource.
      return new ApolloPropertyNamePatternsMatcher(EXACT_DELIMITERS, true, names);
    }
    if (relaxedTargetNames != null) {
      // We can filter properties to those starting with the target name, but
      // we can't do a complete filter since we need to trigger the
      // unknown fields check
      Set<String> relaxedNames = new HashSet<String>();
      for (String relaxedTargetName : relaxedTargetNames) {
        relaxedNames.add(relaxedTargetName);
      }
      return new ApolloPropertyNamePatternsMatcher(TARGET_NAME_DELIMITERS, true, relaxedNames);
    }
    // Not ideal, we basically can't filter anything
    return ApolloPropertyNamePatternsMatcher.ALL;
  }

  private boolean isMapTarget() {
    return this.target != null && Map.class.isAssignableFrom(this.target.getClass());
  }

  private void checkForBindingErrors(RelaxedDataBinder dataBinder) throws BindException {
    BindingResult errors = dataBinder.getBindingResult();
    if (errors.hasErrors()) {
      logger.error("Properties configuration failed validation");
      for (ObjectError error : errors.getAllErrors()) {
        logger.error("{}", error);
      }
      if (this.exceptionIfInvalid) {
        throw new BindException(errors);
      }
    }
  }

  /**
   * Customize the data binder.
   * 
   * @param dataBinder the data binder that will be used to bind and validate
   */
  protected void customizeBinder(DataBinder dataBinder) {
  }

  public void setAnnotations(Annotation[] annotations) {
    this.annotations = annotations;
  }

}
