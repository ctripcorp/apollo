package com.ctrip.framework.apollo.spring.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.xml.sax.InputSource;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.spring.property.ValueMappingOriginValue;

/**
 * The value mapping parser for the property value of Spring XML bean definition
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingXmlBeanParser extends AbstractValueMappingParser {

  @Override
  public Object doParse(ValueMappingOriginValue value) {
    Object origValue = value.getOriginValue();
    String valStr = origValue.toString();
    if (StringUtils.isBlank(valStr)) {
      return null;
    }
    
    Class<?> type = value.getType();
    DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
    XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
    reader.setValidating(false);
    reader.setNamespaceAware(false);
    reader.setBeanClassLoader(type.getClassLoader());
    
    InputStream inputStream;
    try {
      inputStream = new ByteArrayInputStream(valStr.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    InputSource inputSource = new InputSource(inputStream);
    reader.loadBeanDefinitions(inputSource);

    return factory.getBean(type);
  }

}
