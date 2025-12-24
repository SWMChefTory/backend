package com.cheftory.api._config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public GroupedOpenApi boardGroupedOpenApi() {
    return GroupedOpenApi.builder()
        .group("v1")
        .pathsToMatch("/**")
        .addOpenApiCustomizer(
            openApi -> openApi.setInfo(new Info().title("chieftory api").version("1.0.0")))
        .build();
  }
}
