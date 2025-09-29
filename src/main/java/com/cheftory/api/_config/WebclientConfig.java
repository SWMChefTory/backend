package com.cheftory.api._config;

import io.micrometer.observation.ObservationRegistry;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebclientConfig {
  @Value("${ai-recipe-summary.url}")
  private String recipeServerUrl;

  private final ObservationRegistry observationRegistry;

  public WebclientConfig(ObservationRegistry observationRegistry) {
    this.observationRegistry = observationRegistry;
  }

  @Bean
  @Qualifier("recipeCreateClient")
  public WebClient webClientForRecipeServer() {

    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofSeconds(60));

    return WebClient.builder()
        .baseUrl(recipeServerUrl)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .observationRegistry(observationRegistry)
        .build();
  }

  @Bean
  @Qualifier("youtubeClient")
  public WebClient webClientForGoogle() {

    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofSeconds(20));

    return WebClient.builder()
        .baseUrl("https://www.googleapis.com/youtube/v3")
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .observationRegistry(observationRegistry)
        .build();
  }
}
