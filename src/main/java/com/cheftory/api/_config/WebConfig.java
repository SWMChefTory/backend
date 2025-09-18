package com.cheftory.api._config;

import com.cheftory.api._common.security.UserArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final UserArgumentResolver userArgumentResolver;

  public WebConfig(UserArgumentResolver userArgumentResolver) {
    this.userArgumentResolver = userArgumentResolver;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/docs/**")
        .addResourceLocations("classpath:/static/docs/");
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(userArgumentResolver);
  }
}