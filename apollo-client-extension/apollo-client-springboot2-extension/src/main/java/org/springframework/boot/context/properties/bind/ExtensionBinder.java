package org.springframework.boot.context.properties.bind;

import org.springframework.boot.context.properties.bind.ApolloBinder.ApolloContext;
import org.springframework.boot.context.properties.bind.BeanBinder;
import org.springframework.boot.context.properties.bind.BeanPropertyBinder;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

/**
 * 
 * @author wangbo(wangle_r@163.com)
 *
 */
public interface ExtensionBinder {

  /**
   * Return a bound bean instance or {@code null} if the {@link BeanBinder} does not
   * support the specified {@link Bindable}.
   * @param name the name being bound
   * @param target the bindable to bind
   * @param context the bind context
   * @param propertyBinder property binder
   * @param <T> the source type
   * @return a bound instance or {@code null}
   */
  <T> T bind(ConfigurationPropertyName name, Bindable<T> target, ApolloContext context,
      BeanPropertyBinder propertyBinder);
}
