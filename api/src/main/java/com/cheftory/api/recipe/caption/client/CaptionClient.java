package com.cheftory.api.recipe.caption.client;

import com.cheftory.api.recipe.caption.client.dto.ClientCaptionRequest;
import com.cheftory.api.recipe.caption.client.dto.ClientCaptionResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CaptionClient {
    public CaptionClient(@Qualifier("recipeCreateClient") WebClient webClient){
        this.webClient = webClient;
    }
    private final WebClient webClient;

    public ClientCaptionResponse fetchCaption(String videoId){
        ClientCaptionRequest request = ClientCaptionRequest
                .from(videoId,"youtube");
        return webClient.post().uri(uriBuilder -> uriBuilder
                .path("/captions")
                .build()
        ).bodyValue(request)
                .retrieve()
                .bodyToMono(ClientCaptionResponse.class)
                .block();
    }
}
