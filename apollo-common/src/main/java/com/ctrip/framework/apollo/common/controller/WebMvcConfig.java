package com.ctrip.framework.apollo.common.controller;

import java.util.List;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Mvc配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer,
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    // 分页处理器方法参数解析器
    PageableHandlerMethodArgumentResolver pageResolver =
        new PageableHandlerMethodArgumentResolver();
    pageResolver.setFallbackPageable(PageRequest.of(0, 10));

    // 添加分页参数解析器
    argumentResolvers.add(pageResolver);
  }

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    // 不通过请求Url的扩展名来决定media type
    configurer.favorPathExtension(false);
  }

  @Override
  public void customize(TomcatServletWebServerFactory factory) {
    // 添加html的mime类型配置
    MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
    mappings.add("html", "text/html;charset=utf-8");
    factory.setMimeMappings(mappings);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 配置静态访问资源
    // 10 days
    addCacheControl(registry, "img", 864000);
    addCacheControl(registry, "vendor", 864000);
    addCacheControl(registry, "scripts", 864000);
    addCacheControl(registry, "styles", 864000);
    // 1 day
    addCacheControl(registry, "views", 86400);
    addCacheControl(registry, "i18n", 86400);
  }

  /**
   * 添加缓存控制
   *
   * @param registry    资源处理注册器
   * @param folder      文件夹名称
   * @param cachePeriod 缓存周期
   */
  private void addCacheControl(ResourceHandlerRegistry registry, String folder, int cachePeriod) {
    registry.addResourceHandler(String.format("/%s/**", folder))
        .addResourceLocations(String.format("classpath:/static/%s/", folder))
        .setCachePeriod(cachePeriod);
  }
}
