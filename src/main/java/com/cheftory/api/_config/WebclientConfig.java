package com.cheftory.api._config;

import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api._common.region.MarketHeaders;
import com.cheftory.api.affiliate.coupang.CoupangHttpApi;
import com.cheftory.api.auth.verifier.client.AppleTokenHttpApi;
import com.cheftory.api.auth.verifier.client.GoogleTokenHttpApi;
import com.cheftory.api.recipe.content.briefing.client.BriefingHttpApi;
import com.cheftory.api.recipe.content.detail.client.RecipeDetailHttpApi;
import com.cheftory.api.recipe.content.step.client.RecipeStepHttpApi;
import com.cheftory.api.recipe.content.verify.client.RecipeVerifyHttpApi;
import com.cheftory.api.recipe.content.youtubemeta.client.YoutubeMetaHttpApi;
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
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
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
    public RecipeStepHttpApi recipeStepHttpApi(@Qualifier("recipeCreateClient") WebClient recipeCreateClient) {
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(recipeCreateClient))
                .build()
                .createClient(RecipeStepHttpApi.class);
    }

    @Bean
    public RecipeVerifyHttpApi recipeVerifyHttpApi(@Qualifier("recipeCreateClient") WebClient recipeCreateClient) {
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(recipeCreateClient))
                .build()
                .createClient(RecipeVerifyHttpApi.class);
    }

    @Bean
    public RecipeDetailHttpApi recipeDetailHttpApi(@Qualifier("recipeCreateClient") WebClient recipeCreateClient) {
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(recipeCreateClient))
                .build()
                .createClient(RecipeDetailHttpApi.class);
    }

    @Bean
    public BriefingHttpApi briefingHttpApi(@Qualifier("recipeCreateClient") WebClient recipeCreateClient) {
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(recipeCreateClient))
                .build()
                .createClient(BriefingHttpApi.class);
    }

    @Bean
    public YoutubeMetaHttpApi youtubeMetaHttpApi() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(20));

        WebClient youtubeClient = WebClient.builder()
                .baseUrl("https://www.googleapis.com/youtube/v3")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .observationRegistry(observationRegistry)
                .build();
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(youtubeClient))
                .build()
                .createClient(YoutubeMetaHttpApi.class);
    }

    @Bean
    public CoupangHttpApi coupangHttpApi() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory("https://api-gateway.coupang.com");
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(30));

        WebClient coupangClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .observationRegistry(observationRegistry)
                .build();
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(
                        WebClientAdapter.create(coupangClient))
                .build();
        return proxyFactory.createClient(CoupangHttpApi.class);
    }

    @Bean
    public AppleTokenHttpApi appleTokenHttpApi() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10));

        WebClient appleClient = WebClient.builder()
                .baseUrl("https://appleid.apple.com")
                .observationRegistry(observationRegistry)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(appleClient))
                .build();
        return factory.createClient(AppleTokenHttpApi.class);
    }

    @Bean
    public GoogleTokenHttpApi googleTokenHttpApi() {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10));

        WebClient client = WebClient.builder()
                .baseUrl("https://oauth2.googleapis.com")
                .observationRegistry(observationRegistry)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(client))
                .build()
                .createClient(GoogleTokenHttpApi.class);
    }
}
