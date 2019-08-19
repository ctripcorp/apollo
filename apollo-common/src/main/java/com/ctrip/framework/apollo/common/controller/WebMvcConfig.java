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

@Configuration
public class WebMvcConfig implements WebMvcConfigurer, WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    PageableHandlerMethodArgumentResolver pageResolver =
            new PageableHandlerMethodArgumentResolver();
    pageResolver.setFallbackPageable(PageRequest.of(0, 10));

    argumentResolvers.add(pageResolver);
  }

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.favorPathExtension(false);
  }

  @Override
  public void customize(TomcatServletWebServerFactory factory) {
    MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
    mappings.add("html", "text/html;charset=utf-8");
    factory.setMimeMappings(mappings );
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 10 days
    addCacheControl(registry, "img", 864000);
    addCacheControl(registry, "vendor", 864000);
    // 1 day
    addCacheControl(registry, "scripts", 86400);
    addCacheControl(registry, "styles", 86400);
    addCacheControl(registry, "views", 86400);
  }

  private void addCacheControl(ResourceHandlerRegistry registry, String folder, int cachePeriod) {
    registry.addResourceHandler(String.format("/%s/**", folder))
        .addResourceLocations(String.format("classpath:/static/%s/", folder))
        .setCachePeriod(cachePeriod);
  }
}
