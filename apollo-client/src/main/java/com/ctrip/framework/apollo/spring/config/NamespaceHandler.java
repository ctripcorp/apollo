package com.ctrip.framework.apollo.spring.config;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.core.Ordered;
import org.w3c.dom.Element;

/**
 * Apollo 的 XML Namespace 的处理器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class NamespaceHandler extends NamespaceHandlerSupport {

  private static final Splitter NAMESPACE_SPLITTER = Splitter.on(",").omitEmptyStrings()
      .trimResults();

  @Override
  public void init() {
    // 设置Bean定义的解析器
    registerBeanDefinitionParser("config", new BeanParser());
  }

  /**
   * bean解析器
   */
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
      // 解析 `namespaces` 属性，默认为 `"application"`
      String namespaces = element.getAttribute("namespaces");
      //default to application
      if (Strings.isNullOrEmpty(namespaces)) {
        namespaces = ConfigConsts.NAMESPACE_APPLICATION;
      }

      // 解析 `order` 属性，默认为 Ordered.LOWEST_PRECEDENCE;
      int order = Ordered.LOWEST_PRECEDENCE;
      String orderAttribute = element.getAttribute("order");

      if (!Strings.isNullOrEmpty(orderAttribute)) {
        try {
          order = Integer.parseInt(orderAttribute);
        } catch (Throwable ex) {
          throw new IllegalArgumentException(
              String.format("Invalid order: %s for namespaces: %s", orderAttribute, namespaces));
        }
      }
      // 添加到 PropertySourcesProcessor
      PropertySourcesProcessor.addNamespaces(NAMESPACE_SPLITTER.splitToList(namespaces), order);
    }
  }
}
