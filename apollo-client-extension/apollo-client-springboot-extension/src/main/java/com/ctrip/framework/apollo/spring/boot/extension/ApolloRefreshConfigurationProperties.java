package com.ctrip.framework.apollo.spring.boot.extension;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationBeanFactoryMetaData;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.ctrip.framework.apollo.spring.annotation.AutoRefresh;
import com.ctrip.framework.apollo.spring.annotation.RefreshEnabled;
import com.ctrip.framework.apollo.spring.boot.AbstractRefreshConfigurationProperties;
import com.ctrip.framework.apollo.spring.boot.BeanFactoryMetadata;
import com.ctrip.framework.apollo.spring.util.ApolloRefreshUtil;

/**
 * {@link ConfigurationProperties} The specific implementation of automatic
 * update update implementation
 * 
 * @author wangbo(wangle_r@163.com)
 */
public class ApolloRefreshConfigurationProperties extends AbstractRefreshConfigurationProperties
    implements BeanFactoryAware, InitializingBean, ApplicationContextAware, EnvironmentAware {

  /**
   * The bean name of the configuration properties validator.
   */
  public static final String VALIDATOR_BEAN_NAME = "configurationPropertiesValidator";

  private static final String[] VALIDATOR_CLASSES = { "javax.validation.Validator", "javax.validation.ValidatorFactory",
      "javax.validation.bootstrap.GenericBootstrap" };

  private Validator validator;

  private volatile Validator localValidator;

  private ConversionService conversionService;

  private DefaultConversionService defaultConversionService;

  private BeanFactory beanFactory;

  private ApplicationContext applicationContext;

  private List<Converter<?, ?>> converters = Collections.emptyList();

  private List<GenericConverter> genericConverters = Collections.emptyList();

  private BeanFactoryMetadata beanFactoryMetadata;

  private Environment environment;

  /**
   * The bean name that this post-processor is registered with.
   */
  public static final String BEAN_NAME = ApolloRefreshConfigurationProperties.class.getName();

  /**
   * A list of custom converters (in addition to the defaults) to use when
   * converting properties for binding.
   * 
   * @param converters the converters to set
   */
  @Autowired(required = false)
  @ConfigurationPropertiesBinding
  public void setConverters(List<Converter<?, ?>> converters) {
    this.converters = converters;
  }

  /**
   * A list of custom converters (in addition to the defaults) to use when
   * converting properties for binding.
   * 
   * @param converters the converters to set
   */
  @Autowired(required = false)
  @ConfigurationPropertiesBinding
  public void setGenericConverters(List<GenericConverter> converters) {
    this.genericConverters = converters;
  }

  /**
   * Set the bean validator used to validate property fields.
   * 
   * @param validator the validator
   */
  public void setValidator(Validator validator) {
    this.validator = validator;
  }

  /**
   * Set the conversion service used to convert property values.
   * 
   * @param conversionService the conversion service
   */
  public void setConversionService(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.validator == null) {
      this.validator = getOptionalBean(VALIDATOR_BEAN_NAME, Validator.class);
    }
    if (this.conversionService == null) {
      this.conversionService = getOptionalBean(ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME,
          ConversionService.class);
    }
    ConfigurationBeanFactoryMetaData beans = (ConfigurationBeanFactoryMetaData) applicationContext
        .getBean(ApolloRefreshConfigurationPropertiesProcessorRegistry.METADATA_BEAN_NAME);

    this.beanFactoryMetadata = new ApolloSpringBeanFactoryMetadata(beans);
  }

  @Override
  protected ApplicationContext getApplicationContext() {
    return this.applicationContext;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
    
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }


  @Override
  public void refreshBinding(Object bean, String beanName, Properties properties) {
    Object target = bean;
    if (bean instanceof String) {
      target = this.applicationContext.getBean(beanName);
    }
    Class<?> clazz = target.getClass();
    ConfigurationProperties annotation = ApolloRefreshUtil.findConfigurationPropertiesAnnotation(clazz, beanName,
        this.beanFactoryMetadata);
    if (annotation != null) {
      binding(target, beanName, annotation,
          ApolloRefreshUtil.findAutoRefreshAnnotation(clazz, beanName, this.beanFactoryMetadata),
          ApolloRefreshUtil.findRefreshEnabledAnnotation(clazz, beanName, this.beanFactoryMetadata), properties);
    }

  }
  
  @Override
  public void refreshBinding(Object bean, String beanName) {
    Object target = bean;
    if (bean instanceof String) {
      target = this.applicationContext.getBean(beanName);
    }
    Class<?> clazz = target.getClass();
    ConfigurationProperties annotation = ApolloRefreshUtil.findConfigurationPropertiesAnnotation(clazz, beanName,
        this.beanFactoryMetadata);
    if (annotation != null) {
      binding(target, beanName, annotation,
          ApolloRefreshUtil.findAutoRefreshAnnotation(clazz, beanName, this.beanFactoryMetadata),
          ApolloRefreshUtil.findRefreshEnabledAnnotation(clazz, beanName, this.beanFactoryMetadata), null);
    }

  }

  void binding(Object bean, String beanName, ConfigurationProperties annotation, AutoRefresh autoRefresh,
      RefreshEnabled refreshEnabled, Properties properties) {
    if (annotation == null) {
      return;
    }

    List<Annotation> annotations = new LinkedList<Annotation>();
    annotations.add(annotation);
    if (refreshEnabled != null) {
      annotations.add(refreshEnabled);
    }
    if (autoRefresh != null) {
      annotations.add(autoRefresh);
    }
    binding(bean, beanName, annotation, annotations.toArray(new Annotation[annotations.size()]), properties);

  }
  
  void binding(Object bean, String beanName, ConfigurationProperties annotation, Annotation[] annotations) {
    if (annotation == null) {
      return;
    }
    binding(bean, beanName, annotation, annotations, null);

  }

  @SuppressWarnings("deprecation")
  void binding(Object bean, String beanName, ConfigurationProperties annotation,
      Annotation[] annotations, Properties properties) {
    Object target = bean;
    ApolloRefreshPropertiesConfigurationFactory<Object> factory = new ApolloRefreshPropertiesConfigurationFactory<Object>(target);

    factory.setPropertySources(this.deducePropertySources(properties));
    factory.setApplicationContext(this.applicationContext);
    factory.setValidator(determineValidator(bean));
    // If no explicit conversion service is provided we add one so that (at least)
    // comma-separated arrays of convertibles can be bound automatically
    factory
        .setConversionService(this.conversionService != null ? this.conversionService : getDefaultConversionService());
    factory.setIgnoreInvalidFields(annotation.ignoreInvalidFields());
    factory.setIgnoreUnknownFields(annotation.ignoreUnknownFields());
    factory.setExceptionIfInvalid(annotation.exceptionIfInvalid());
    factory.setIgnoreNestedProperties(annotation.ignoreNestedProperties());
    factory.setAnnotations(annotations);
    if (StringUtils.hasLength(annotation.prefix())) {
      factory.setTargetName(annotation.prefix());
    }
    try {
      factory.bindPropertiesToTarget();
    } catch (Exception ex) {
      String targetClass = ClassUtils.getShortName(bean.getClass());
      throw new BeanCreationException(beanName,
          "Could not bind properties to " + targetClass + " (" + getAnnotationDetails(annotation) + ")", ex);
    }
  }

  private <T> T getOptionalBean(String name, Class<T> type) {
    try {
      return this.beanFactory.getBean(name, type);
    } catch (NoSuchBeanDefinitionException ex) {
      return null;
    }
  }

  private StandardEnvironment getPropertiesEnvironment(Properties properties) {
    StandardEnvironment environment = new StandardEnvironment();
    environment.getPropertySources().addFirst(new PropertiesPropertySource("APOLLO_UPDATE_PROPERTIES", properties));
    return environment;
  }

  private PropertySources deducePropertySources(Properties properties) {
    if (properties != null) {
      return ((ConfigurableEnvironment) getPropertiesEnvironment(properties)).getPropertySources();
    }

    if (this.environment instanceof ConfigurableEnvironment) {
      MutablePropertySources propertySources = ((ConfigurableEnvironment) this.environment).getPropertySources();
      return new FlatPropertySources(propertySources);
    }

    return new MutablePropertySources();
  }

  private String getAnnotationDetails(ConfigurationProperties annotation) {
    if (annotation == null) {
      return "";
    }
    StringBuilder details = new StringBuilder();
    details.append("prefix=").append(annotation.prefix());
    details.append(", ignoreInvalidFields=").append(annotation.ignoreInvalidFields());
    details.append(", ignoreUnknownFields=").append(annotation.ignoreUnknownFields());
    details.append(", ignoreNestedProperties=").append(annotation.ignoreNestedProperties());
    return details.toString();
  }

  private Validator determineValidator(Object bean) {
    Validator validator = getValidator();
    boolean supportsBean = (validator != null && validator.supports(bean.getClass()));
    if (ClassUtils.isAssignable(Validator.class, bean.getClass())) {
      if (supportsBean) {
        return new ChainingValidator(validator, (Validator) bean);
      }
      return (Validator) bean;
    }
    return (supportsBean ? validator : null);
  }

  private Validator getValidator() {
    if (this.validator != null) {
      return this.validator;
    }
    if (this.localValidator == null && isJsr303Present()) {
      this.localValidator = new ValidatedLocalValidatorFactoryBean(this.applicationContext);
    }
    return this.localValidator;
  }

  private boolean isJsr303Present() {
    for (String validatorClass : VALIDATOR_CLASSES) {
      if (!ClassUtils.isPresent(validatorClass, this.applicationContext.getClassLoader())) {
        return false;
      }
    }
    return true;
  }

  private ConversionService getDefaultConversionService() {
    if (this.defaultConversionService == null) {
      DefaultConversionService conversionService = new DefaultConversionService();
      this.applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
      for (Converter<?, ?> converter : this.converters) {
        conversionService.addConverter(converter);
      }
      for (GenericConverter genericConverter : this.genericConverters) {
        conversionService.addConverter(genericConverter);
      }
      this.defaultConversionService = conversionService;
    }

    return this.defaultConversionService;
  }

  /**
   * {@link LocalValidatorFactoryBean} supports classes annotated with
   * {@link Validated @Validated}.
   */
  private static class ValidatedLocalValidatorFactoryBean extends LocalValidatorFactoryBean {

    private static final String HAS_ORG_SPRINGFRAMEWORK_BOOT = "org.springframework.boot";
    private static final Log logger = LogFactory.getLog(ConfigurationPropertiesBindingPostProcessor.class);

    ValidatedLocalValidatorFactoryBean(ApplicationContext applicationContext) {
      setApplicationContext(applicationContext);
      setMessageInterpolator(new MessageInterpolatorFactory().getObject());
      afterPropertiesSet();
    }

    @Override
    public boolean supports(Class<?> type) {
      if (!super.supports(type)) {
        return false;
      }
      if (AnnotatedElementUtils.hasAnnotation(type, Validated.class)) {
        return true;
      }
      if (type.getPackage() != null && type.getPackage().getName().startsWith(HAS_ORG_SPRINGFRAMEWORK_BOOT)) {
        return false;
      }
      if (getConstraintsForClass(type).isBeanConstrained()) {
        logger.warn("The @ConfigurationProperties bean " + type
            + " contains validation constraints but had not been annotated " + "with @Validated.");
      }
      return true;
    }

  }

  /**
   * {@link Validator} implementation that wraps {@link Validator} instances and
   * chains their execution.
   */
  private static class ChainingValidator implements Validator {

    private Validator[] validators;

    ChainingValidator(Validator... validators) {
      Assert.notNull(validators, "Validators must not be null");
      this.validators = validators;
    }

    @Override
    public boolean supports(Class<?> clazz) {
      for (Validator validator : this.validators) {
        if (validator.supports(clazz)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
      for (Validator validator : this.validators) {
        if (validator.supports(target.getClass())) {
          validator.validate(target, errors);
        }
      }
    }

  }

  /**
   * Convenience class to flatten out a tree of property sources without losing
   * the reference to the backing data (which can therefore be updated in the
   * background).
   */
  private static class FlatPropertySources implements PropertySources {

    private PropertySources propertySources;

    FlatPropertySources(PropertySources propertySources) {
      this.propertySources = propertySources;
    }

    @Override
    public Iterator<PropertySource<?>> iterator() {
      MutablePropertySources result = getFlattened();
      return result.iterator();
    }

    @Override
    public boolean contains(String name) {
      return get(name) != null;
    }

    @Override
    public PropertySource<?> get(String name) {
      return getFlattened().get(name);
    }

    private MutablePropertySources getFlattened() {
      MutablePropertySources result = new MutablePropertySources();
      for (PropertySource<?> propertySource : this.propertySources) {
        flattenPropertySources(propertySource, result);
      }
      return result;
    }

    private void flattenPropertySources(PropertySource<?> propertySource, MutablePropertySources result) {
      Object source = propertySource.getSource();
      if (source instanceof ConfigurableEnvironment) {
        ConfigurableEnvironment environment = (ConfigurableEnvironment) source;
        for (PropertySource<?> childSource : environment.getPropertySources()) {
          flattenPropertySources(childSource, result);
        }
      } else {
        result.addLast(propertySource);
      }
    }

  }
}
