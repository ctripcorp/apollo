package com.ctrip.framework.apollo.spring.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.core.Ordered;
import org.w3c.dom.Element;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class NamespaceHandler extends NamespaceHandlerSupport {

  private static final Splitter NAMESPACE_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

  @Override
  public void init() {
    registerBeanDefinitionParser("config", new BeanParser());
  }

  static class BeanParser extends AbstractSingleBeanDefinitionParser {
    @Override
    protected Class<?> getBeanClass(Element element) {
      return ConfigPropertySourcesProcessor.class;
    }

    @Override
    protected boolean shouldGenerateId() {
      return true;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
      List<String> namespaces;
      String[] configuredNamespaces = null;
      String namespacesValue = element.getAttribute("namespaces");
      //default to application
      if (Strings.isNullOrEmpty(namespacesValue)) {
        namespacesValue = ConfigConsts.NAMESPACE_APPLICATION;
        namespaces = Arrays.asList(ConfigConsts.NAMESPACE_APPLICATION);
      }else{
        namespaces = NAMESPACE_SPLITTER.splitToList(namespacesValue);
        configuredNamespaces = namespaces.toArray(new String[0]);
      }
      builder.addConstructorArgValue(configuredNamespaces);

      int order = Ordered.LOWEST_PRECEDENCE;
      String orderAttribute = element.getAttribute("order");
      if (!Strings.isNullOrEmpty(orderAttribute)) {
        try {
          order = Integer.parseInt(orderAttribute);
        } catch (Throwable ex) {
          throw new IllegalArgumentException(
              String.format("Invalid order: %s for namespaces: %s", orderAttribute, namespacesValue));
        }
      }
      PropertySourcesProcessor.addNamespaces(namespaces, order);
    }
  }

}