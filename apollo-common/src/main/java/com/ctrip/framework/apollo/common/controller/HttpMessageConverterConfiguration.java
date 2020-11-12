package com.ctrip.framework.apollo.common.controller;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;

/**
 * 消息转换器配置
 *
 * @author Jason  5/11/16.
 */
@Configuration
public class HttpMessageConverterConfiguration {

  @Bean
  public HttpMessageConverters messageConverters() {
    GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
    gsonHttpMessageConverter.setGson(
        new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create());
    // 转换器列表,Gson、String、字符数组消息转换器，AllEncompassingFormHttpMessageConverter的作用是
    // 从类加载器去查找相关的类，只有这些转换器需要的类（jar包被你引入了）存在，那么转换器才会被加载进去
    final List<HttpMessageConverter<?>> converters = Lists.newArrayList(
        new ByteArrayHttpMessageConverter(), new StringHttpMessageConverter(),
        new AllEncompassingFormHttpMessageConverter(), gsonHttpMessageConverter);
    return new HttpMessageConverters() {
      @Override
      public List<HttpMessageConverter<?>> getConverters() {
        return Collections.unmodifiableList(converters);
      }
    };
  }
}
