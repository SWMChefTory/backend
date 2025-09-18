package com.cheftory.api._config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public GroupedOpenApi boardGroupedOpenApi() {
    return GroupedOpenApi
        .builder()
        .group("v1") // group 설정 (API들을 그룹화시켜 그룹에 속한 API들만 확인할 수 있도록 도와줌)
        .pathsToMatch("/**")
        .addOpenApiCustomizer(
            openApi ->
                openApi
                    .setInfo(
                        new Info()
                            .title("chieftory api")
                            .version("1.0.0")
                    )
        )
        .build();
  }
}
