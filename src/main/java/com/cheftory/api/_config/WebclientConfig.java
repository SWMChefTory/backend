package com.cheftory.api._config;

import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api._common.region.MarketHeaders;
import io.micrometer.observation.ObservationRegistry;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
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
    public ExchangeFilterFunction marketHeaderPropagator() {
        return (request, next) -> {
            var info = MarketContext.currentOrNull();
            if (info == null) return next.exchange(request);

            ClientRequest filtered = ClientRequest.from(request)
                    .header(MarketHeaders.COUNTRY_CODE, info.countryCode())
                    .build();

            return next.exchange(filtered);
        };
    }

    @Bean
    @Qualifier("recipeCreateClient")
    public WebClient webClientForRecipeServer(ExchangeFilterFunction marketHeaderPropagator) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMinutes(5));

        return WebClient.builder()
                .baseUrl(recipeServerUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .observationRegistry(observationRegistry)
                .filter(marketHeaderPropagator)
                .build();
    }

    @Bean
    @Qualifier("youtubeClient")
    public WebClient webClientForYoutube() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(20));

        return WebClient.builder()
                .baseUrl("https://www.googleapis.com/youtube/v3")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .observationRegistry(observationRegistry)
                .build();
    }

    @Bean
    @Qualifier("coupangClient")
    public WebClient webClientForCoupang() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory("https://api-gateway.coupang.com");
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(30));

        return WebClient.builder()
                .uriBuilderFactory(factory)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .observationRegistry(observationRegistry)
                .build();
    }

    @Bean
    @Qualifier("appleClient")
    public WebClient webClientForApple() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10));

        return WebClient.builder()
                .baseUrl("https://appleid.apple.com")
                .observationRegistry(observationRegistry)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    @Qualifier("googleClient")
    public WebClient webClientForGoogle() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10));

        return WebClient.builder()
                .baseUrl("https://oauth2.googleapis.com")
                .observationRegistry(observationRegistry)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
