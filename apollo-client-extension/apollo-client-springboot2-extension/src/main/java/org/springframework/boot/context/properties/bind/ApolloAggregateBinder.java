package org.springframework.boot.context.properties.bind;

import java.util.function.Supplier;

import org.springframework.boot.context.properties.bind.AggregateElementBinder;
import org.springframework.boot.context.properties.bind.ApolloBinder.ApolloContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;

public abstract class ApolloAggregateBinder<T> {

  private final ApolloContext context;

  ApolloAggregateBinder(ApolloContext context) {
    this.context = context;
  }

  /**
   * Determine if recursive binding is supported.
   * @param source the configuration property source or {@code null} for all sources.
   * @return if recursive binding is supported
   */
  protected abstract boolean isAllowRecursiveBinding(
      ConfigurationPropertySource source);

  /**
   * Perform binding for the aggregate.
   * @param name the configuration property name to bind
   * @param target the target to bind
   * @param elementBinder an element binder
   * @return the bound aggregate or null
   */
  @SuppressWarnings("unchecked")
  public final Object bind(ConfigurationPropertyName name, Bindable<?> target,
      AggregateElementBinder elementBinder) {
    Object result = bindAggregate(name, target, elementBinder);
    Supplier<?> value = target.getValue();
    if (result == null || value == null) {
      return result;
    }
    return merge(value, (T) result);
  }

  /**
   * Perform the actual aggregate binding.
   * @param name the configuration property name to bind
   * @param target the target to bind
   * @param elementBinder an element binder
   * @return the bound result
   */
  protected abstract Object bindAggregate(ConfigurationPropertyName name,
      Bindable<?> target, AggregateElementBinder elementBinder);

  /**
   * Merge any additional elements into the existing aggregate.
   * @param existing the supplier for the existing value
   * @param additional the additional elements to merge
   * @return the merged result
   */
  protected abstract T merge(Supplier<?> existing, T additional);

  /**
   * Return the context being used by this binder.
   * @return the context
   */
  protected final ApolloContext getContext() {
    return this.context;
  }

  /**
   * Internal class used to supply the aggregate and cache the value.
   *
   * @param <T> the aggregate type
   */
  protected static class ApolloAggregateSupplier<T> {

    private final Supplier<T> supplier;

    private T supplied;

    public ApolloAggregateSupplier(Supplier<T> supplier) {
      this.supplier = supplier;
    }

    public T get() {
      if (this.supplied == null) {
        this.supplied = this.supplier.get();
      }
      return this.supplied;
    }

    public boolean wasSupplied() {
      return this.supplied != null;
    }

  }

}