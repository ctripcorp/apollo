package com.ctrip.framework.apollo.spring.boot.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.spring.annotation.ApolloProcessor;
import com.ctrip.framework.apollo.spring.annotation.AutoRefresh;
import com.ctrip.framework.apollo.spring.annotation.RefreshEnabled;
import com.ctrip.framework.apollo.spring.boot.BeanFactoryMetadata;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.ctrip.framework.apollo.spring.util.ApolloRefreshUtil;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.base.Strings;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationBeanFactoryMetadata;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

/**
 * The automatic refresh of SpringBoot ConfigurationProperties is implemented
 * 
 * @author wangbo(wangle_r@163.com)
 */
public class ApolloRefreshConfigurationPropertiesProcessor extends ApolloProcessor
    implements ApplicationContextAware, EnvironmentAware, InitializingBean {

  private ConfigUtil                 configUtil;

  private BeanFactoryMetadata beanFactoryMetadata;

  private Environment                      environment;

  private ApplicationContext               applicationContext;

  private String[]                         namespaces;

  private final Set<Object>                registryListeners                 = new HashSet<>();

  private boolean                          autoUpdateConfigurationProperties = false;

  private String[]                         autoUpdateConfigurationPropertiesBasePackages;
  
  /**
   * The bean name that this post-processor is registered with.
   */
  public static final String BEAN_NAME = ApolloRefreshConfigurationPropertiesProcessor.class
      .getName();

  /**
   * The bean name of the configuration properties validator.
   */
  public static final String VALIDATOR_BEAN_NAME = "configurationPropertiesValidator";

  public ApolloRefreshConfigurationPropertiesProcessor() {
    
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    /**
     * The Listener is registered when Apollo automatic update is enabled
     */
    if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled() && !registryListeners.contains(bean)) {
      registryListener(bean, beanName);
    }
    return bean;
  }

  /**
   * registry Listener
   * 
   * @param bean
   * @param beanName
   */
  private void registryListener(Object bean, String beanName) {
    Class<?> clazz = bean.getClass();
    ConfigurationProperties annotation = ApolloRefreshUtil.findConfigurationPropertiesAnnotation(clazz, beanName, this.beanFactoryMetadata);
    if (annotation != null) {
      RefreshEnabled refreshEnabled = ApolloRefreshUtil.findRefreshEnabledAnnotation(clazz, beanName, this.beanFactoryMetadata);
      AutoRefresh autoRefresh = ApolloRefreshUtil.findAutoRefreshAnnotation(bean.getClass(), beanName, this.beanFactoryMetadata);
      boolean enabled = false;
      if (refreshEnabled != null || autoRefresh != null) {
        enabled = true;
      } else if (ApolloRefreshUtil.findRefreshDisabledAnnotation(clazz, beanName, this.beanFactoryMetadata) != null) {
        enabled = false;
      } else {
        enabled = isAutoUpdateConfigurationPropertiesEnabled(clazz, beanName);
      }
  
      if (enabled) {
      
        registryListener(bean, beanName, annotation, refreshEnabled, autoRefresh);
      }
    }
  }

  private void registryListener(Object bean, String beanName, ConfigurationProperties annotation,
                                RefreshEnabled refreshEnabled, AutoRefresh autoRefresh) {

    //Record registration
    registryListeners.add(bean);
    //Handle listener parameters
    Set<String> interestedKeyPrefixes = new LinkedHashSet<>();
    Set<String> interestedKeys = new LinkedHashSet<>();
    interestedKeyPrefixes.add(annotation.prefix());
    String[] targetnamespaces = getNamespaces();
    if (refreshEnabled != null) {
      for (String interestedKeyPrefixe : refreshEnabled.interestedKeyPrefixes()) {
        interestedKeyPrefixes.add(interestedKeyPrefixe);
      }

      for (String interestedKey : refreshEnabled.interestedKeys()) {
        interestedKeys.add(interestedKey);
      }
      targetnamespaces = refreshEnabled.namespaces();
      if (targetnamespaces.length == 0) {
        targetnamespaces = getNamespaces();
      }
    } else {
      /*
       * In springboot @configurationproperties, get the parameters
       * in @autorefresh if @autorefresh exists
       */
      if (autoRefresh != null) {
        for (String interestedKeyPrefixe : autoRefresh.interestedKeyPrefixes()) {
          interestedKeyPrefixes.add(interestedKeyPrefixe);
        }

        for (String interestedKey : autoRefresh.interestedKeys()) {
          interestedKeys.add(interestedKey);
        }
        targetnamespaces = autoRefresh.namespaces();
        if (targetnamespaces.length == 0) {
          targetnamespaces = getNamespaces();
        }
      }
    }
    List<Annotation> annotations = new LinkedList<Annotation>();
    annotations.add(annotation);
    if(refreshEnabled != null) {
      annotations.add(refreshEnabled);
    }
    if(autoRefresh != null) {
      annotations.add(autoRefresh);
    }
    //registry Listener
    ConfigChangeListener configChangeListener = new ApolloRefreshConfigurationConfigChangeListener(bean, beanName,
        annotation, annotations.toArray(new Annotation[annotations.size()]), applicationContext);
    for (String namespace : targetnamespaces) {
      Config config = ConfigService.getConfig(namespace);
      config.addChangeListener(configChangeListener, interestedKeys, interestedKeyPrefixes);
    }
  }

  /**
   * Determines whether the automatic update configuration is enabled
   * 
   * @param clazz
   * @param beanName
   * @return
   */
  private boolean isAutoUpdateConfigurationPropertiesEnabled(Class<?> clazz, String beanName) {
    boolean enabled = isAutoUpdateConfigurationPropertiesEnabled();
    if (enabled) {
      String packageName = ClassUtils.getPackageName(clazz);
      enabled = isAutoUpdateConfigurationPropertiesBasePackages(packageName);
      if (!enabled) {
        String qualifiedName = packageName + "." + ClassUtils.getShortName(clazz);
        enabled = isAutoUpdateConfigurationPropertiesBasePackages(qualifiedName);
      }
    }
    return enabled;
  }

  public String[] getNamespaces() {
    return namespaces;
  }

  @Override
  protected void processField(Object bean, String beanName, Field field) {

  }

  @Override
  protected void processMethod(Object bean, String beanName, Method method) {

  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;

  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // We can't use constructor injection of the application context because
    // it causes eager factory bean initialization
    this.beanFactoryMetadata = new ApolloSpringBeanFactoryMetadata(this.applicationContext.getBean(
        ConfigurationBeanFactoryMetadata.BEAN_NAME,
        ConfigurationBeanFactoryMetadata.class));
    
    this.initialize();
    
    this.configUtil = ApolloInjector.getInstance(ConfigUtil.class);
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
    this.initialize();
  }

  private void initialize() {
    if (namespaces == null) {
      initNamespaces();
    }
    
    if (autoUpdateConfigurationPropertiesBasePackages == null) {
      initAutoUpdateConfigurationProperties(environment);
    }
  }

  private void initAutoUpdateConfigurationProperties(Environment environment) {
    autoUpdateConfigurationPropertiesBasePackages = new String[]{};
    /**
     * Deal with whether to enable AutoUpdateConfigurationProperties
     */
    String value = environment.getProperty(PropertySourcesConstants.APOLLO_AUTO_REFRESH_CONFIGURATION_PROPERTIE);
    if (Strings.isNullOrEmpty(value)) {
      autoUpdateConfigurationProperties = false;
    } else if ("true".equalsIgnoreCase(value)) {
      autoUpdateConfigurationProperties = true;
    } else if (!Strings.isNullOrEmpty(value) && !"false".equalsIgnoreCase(value)) {
      /**
       * When an unpassed value is not false, the passed package or class name
       * is indicated
       */
      autoUpdateConfigurationProperties = true;
      autoUpdateConfigurationPropertiesBasePackages = value.split(",");
      for (int i = 0; i < autoUpdateConfigurationPropertiesBasePackages.length; i++) {
        autoUpdateConfigurationPropertiesBasePackages[i] = autoUpdateConfigurationPropertiesBasePackages[i].trim();
      }
    } else {
      autoUpdateConfigurationProperties = false;
    }
  }

  private void initNamespaces() {
    String namespaceStr = environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES);
    if (Strings.isNullOrEmpty(namespaceStr)) {
      namespaceStr = ConfigConsts.NAMESPACE_APPLICATION;
    }
    if (!Strings.isNullOrEmpty(namespaceStr)) {
      namespaces = namespaceStr.split(",");
    }
  }

  /**
   * Determines whether springboot ConfigurationProperties is enabled to
   * automatically update the property values
   * 
   * @return
   */
  private boolean isAutoUpdateConfigurationPropertiesEnabled() {
    return autoUpdateConfigurationProperties;
  }

  /**
   * Determines whether the specified package (packageName) contains the
   * specified BasePackages
   * 
   * @param packageName
   * @return
   */
  private boolean isAutoUpdateConfigurationPropertiesBasePackages(String packageName) {
    if (autoUpdateConfigurationPropertiesBasePackages == null) {
      initAutoUpdateConfigurationProperties(environment);
    }
    if (autoUpdateConfigurationPropertiesBasePackages.length > 0) {
      for (String string : autoUpdateConfigurationPropertiesBasePackages) {
        if (string.equals(packageName)) {
          return true;
        }
        //Is it subpackage
        if (packageName.startsWith(string)) {
          return true;
        }
      }
      return false;
    } else {
      //Returns true if no package is configured
      return true;
    }
  }
}
