package org.springframework.boot.context.properties.bind;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.bind.ApolloBinder.ApolloContext;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName.Form;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.IterableConfigurationPropertySource;
import org.springframework.core.ResolvableType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

abstract class ApolloIndexedElementsBinder<T> extends ApolloAggregateBinder<T> {

  private static final String INDEX_ZERO = "[0]";

  ApolloIndexedElementsBinder(ApolloContext context) {
    super(context);
  }

  @Override
  protected boolean isAllowRecursiveBinding(ConfigurationPropertySource source) {
    return source == null || source instanceof IterableConfigurationPropertySource;
  }

  /**
   * Bind indexed elements to the supplied collection.
   * @param name the name of the property to bind
   * @param target the target bindable
   * @param elementBinder the binder to use for elements
   * @param aggregateType the aggregate type, may be a collection or an array
   * @param elementType the element type
   * @param result the destination for results
   */
  protected final void bindIndexed(ConfigurationPropertyName name, Bindable<?> target,
      AggregateElementBinder elementBinder, ResolvableType aggregateType,
      ResolvableType elementType, ApolloIndexedCollectionSupplier result) {
    for (ConfigurationPropertySource source : getContext().getSources()) {
      bindIndexed(source, name, target, elementBinder, result, aggregateType,
          elementType);
      if (result.wasSupplied() && result.get() != null) {
        return;
      }
    }
  }

  private void bindIndexed(ConfigurationPropertySource source,
      ConfigurationPropertyName root, Bindable<?> target,
      AggregateElementBinder elementBinder, ApolloIndexedCollectionSupplier collection,
      ResolvableType aggregateType, ResolvableType elementType) {
    ConfigurationProperty property = source.getConfigurationProperty(root);
    if (property != null) {
      bindValue(target, collection.get(), aggregateType, elementType,
          property.getValue());
    }
    else {
      bindIndexed(source, root, elementBinder, collection, elementType);
    }
  }

  private void bindValue(Bindable<?> target, Collection<Object> collection,
      ResolvableType aggregateType, ResolvableType elementType, Object value) {
    if (value instanceof String && !StringUtils.hasText((String) value)) {
      return;
    }
    Object aggregate = convert(value, aggregateType, target.getAnnotations());
    ResolvableType collectionType = ResolvableType
        .forClassWithGenerics(collection.getClass(), elementType);
    Collection<Object> elements = convert(aggregate, collectionType);
    collection.addAll(elements);
  }

  private void bindIndexed(ConfigurationPropertySource source,
      ConfigurationPropertyName root, AggregateElementBinder elementBinder,
      ApolloIndexedCollectionSupplier collection, ResolvableType elementType) {
    MultiValueMap<String, ConfigurationProperty> knownIndexedChildren = getKnownIndexedChildren(
        source, root);
    for (int i = 0; i < Integer.MAX_VALUE; i++) {
      ConfigurationPropertyName name = root
          .append((i != 0) ? "[" + i + "]" : INDEX_ZERO);
      Object value = elementBinder.bind(name, Bindable.of(elementType), source);
      if (value == null) {
        break;
      }
      knownIndexedChildren.remove(name.getLastElement(Form.UNIFORM));
      collection.get().add(value);
    }
    assertNoUnboundChildren(knownIndexedChildren);
  }

  private MultiValueMap<String, ConfigurationProperty> getKnownIndexedChildren(
      ConfigurationPropertySource source, ConfigurationPropertyName root) {
    MultiValueMap<String, ConfigurationProperty> children = new LinkedMultiValueMap<>();
    if (!(source instanceof IterableConfigurationPropertySource)) {
      return children;
    }
    for (ConfigurationPropertyName name : (IterableConfigurationPropertySource) source
        .filter(root::isAncestorOf)) {
      ConfigurationPropertyName choppedName = name
          .chop(root.getNumberOfElements() + 1);
      if (choppedName.isLastElementIndexed()) {
        String key = choppedName.getLastElement(Form.UNIFORM);
        ConfigurationProperty value = source.getConfigurationProperty(name);
        children.add(key, value);
      }
    }
    return children;
  }

  private void assertNoUnboundChildren(
      MultiValueMap<String, ConfigurationProperty> children) {
    if (!children.isEmpty()) {
      throw new UnboundConfigurationPropertiesException(
          children.values().stream().flatMap(List::stream)
              .collect(Collectors.toCollection(TreeSet::new)));
    }
  }

  private <C> C convert(Object value, ResolvableType type, Annotation... annotations) {
    value = getContext().getPlaceholdersResolver().resolvePlaceholders(value);
    return getContext().getConverter().convert(value, type, annotations);
  }

  /**
   * {@link AggregateBinder.AggregateSupplier AggregateSupplier} for an indexed
   * collection.
   */
  protected static class ApolloIndexedCollectionSupplier
      extends ApolloAggregateSupplier<Collection<Object>> {

    public ApolloIndexedCollectionSupplier(Supplier<Collection<Object>> supplier) {
      super(supplier);
    }

  }

}

