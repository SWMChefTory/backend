package com.cheftory.api.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebclientConfig {
    @Value("${ai-recipe-summary.url}")
    private String recipeServerUrl;
    @Bean
    @Qualifier("recipeCreateClient")
    public WebClient webClientForRecipeServer() {
        return WebClient.builder()
                .baseUrl(recipeServerUrl)
                .build();
    }

    @Bean
    @Qualifier("youtubeClient")
    public WebClient webClientForGoogle() {
        return WebClient
                .builder()
                .baseUrl("https://www.googleapis.com/youtube/v3")
                .build();
    }
}
