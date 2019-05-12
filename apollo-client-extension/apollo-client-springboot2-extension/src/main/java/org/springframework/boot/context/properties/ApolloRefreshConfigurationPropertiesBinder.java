package org.springframework.boot.context.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.boot.context.properties.bind.ApolloBinder;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.bind.handler.IgnoreTopLevelConverterNotFoundBindHandler;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;
import org.springframework.boot.context.properties.bind.validation.ValidationBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.UnboundElementsSourceFilter;
import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.PropertySources;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * {@link ConfigurationProperties} The specific implementation of automatic update update implementation
 * 
 * @author wangbo(wangle_r@163.com)
 */
public class ApolloRefreshConfigurationPropertiesBinder{

  private static final String[] VALIDATOR_CLASSES = { "javax.validation.Validator",
      "javax.validation.ValidatorFactory",
      "javax.validation.bootstrap.GenericBootstrap" };
  
  private ApplicationContext applicationContext;

  private Validator configurationPropertiesValidator;

  private boolean jsr303Present;

  private Validator jsr303Validator;
  
  private ConversionService conversionService;
  
  public ApolloRefreshConfigurationPropertiesBinder(ApplicationContext applicationContext, String validatorBeanName) {
    this.applicationContext = applicationContext;
    this.configurationPropertiesValidator = getConfigurationPropertiesValidator(validatorBeanName);
    this.jsr303Present = isJsr303Present();
    this.conversionService = getConversionService();
  }
  
  public boolean isJsr303Present() {
    ClassLoader classLoader = this.applicationContext.getClassLoader();
    for (String validatorClass : VALIDATOR_CLASSES) {
      if (!ClassUtils.isPresent(validatorClass, classLoader)) {
        return false;
      }
    }
    return true;
  }

  public void bind(Bindable<?> target, PropertySources propertySources) {
    ConfigurationProperties annotation = target
        .getAnnotation(ConfigurationProperties.class);
    Assert.state(annotation != null,
        () -> "Missing @ConfigurationProperties on " + target);
    List<Validator> validators = getValidators(target);
    BindHandler bindHandler = getBindHandler(annotation, validators);
    getBinder(propertySources).bind(annotation.prefix(), target, bindHandler);
  }

  
  private Validator getConfigurationPropertiesValidator(String validatorBeanName) {
    if (this.applicationContext.containsBean(validatorBeanName)) {
      return this.applicationContext.getBean(validatorBeanName, Validator.class);
    }
    return null;
  }

  private List<Validator> getValidators(Bindable<?> target) {
    List<Validator> validators = new ArrayList<>(3);
    if (this.configurationPropertiesValidator != null) {
      validators.add(this.configurationPropertiesValidator);
    }
    if (this.jsr303Present && target.getAnnotation(Validated.class) != null) {
      validators.add(getJsr303Validator());
    }
    if (target.getValue() != null && target.getValue().get() instanceof Validator) {
      validators.add((Validator) target.getValue().get());
    }
    return validators;
  }

  private Validator getJsr303Validator() {
    if (this.jsr303Validator == null) {
      this.jsr303Validator = new Jsr303Validator(
          this.applicationContext);
    }
    return this.jsr303Validator;
  }

  private BindHandler getBindHandler(ConfigurationProperties annotation,
      List<Validator> validators) {
    BindHandler handler = new IgnoreTopLevelConverterNotFoundBindHandler();
    if (annotation.ignoreInvalidFields()) {
      handler = new IgnoreErrorsBindHandler(handler);
    }
    if (!annotation.ignoreUnknownFields()) {
      UnboundElementsSourceFilter filter = new UnboundElementsSourceFilter();
      handler = new NoUnboundElementsBindHandler(handler, filter);
    }
    if (!validators.isEmpty()) {
      handler = new ValidationBindHandler(handler,
          validators.toArray(new Validator[0]));
    }
    return handler;
  }

  private ApolloBinder getBinder(PropertySources propertySources) {
      return new ApolloBinder(getConfigurationPropertySources(propertySources),
          getPropertySourcesPlaceholdersResolver(propertySources), conversionService,
          getPropertyEditorInitializer());
  }

  private Iterable<ConfigurationPropertySource> getConfigurationPropertySources(PropertySources propertySources) {
    return ConfigurationPropertySources.from(propertySources);
  }

  private PropertySourcesPlaceholdersResolver getPropertySourcesPlaceholdersResolver(PropertySources propertySources) {
    return new PropertySourcesPlaceholdersResolver(propertySources);
  }

  private ConversionService getConversionService() {
    
    return new ConversionServiceDeducer(this.applicationContext).getConversionService();
  }

  private Consumer<PropertyEditorRegistry> getPropertyEditorInitializer() {
    if (this.applicationContext instanceof ConfigurableApplicationContext) {
      return ((ConfigurableApplicationContext) this.applicationContext)
          .getBeanFactory()::copyRegisteredEditorsTo;
    }
    return null;
  }

  
  private  final class Jsr303Validator implements Validator {

    private final Delegate delegate;

    Jsr303Validator(ApplicationContext applicationContext) {
      this.delegate = new Delegate(applicationContext);
    }

    @Override
    public boolean supports(Class<?> type) {
      return this.delegate.supports(type);
    }

    @Override
    public void validate(Object target, Errors errors) {
      this.delegate.validate(target, errors);
    }

  }
  private static class Delegate extends LocalValidatorFactoryBean {
    
    Delegate(ApplicationContext applicationContext) {
      setApplicationContext(applicationContext);
      setMessageInterpolator(new MessageInterpolatorFactory().getObject());
      afterPropertiesSet();
    }
    
  }
}
